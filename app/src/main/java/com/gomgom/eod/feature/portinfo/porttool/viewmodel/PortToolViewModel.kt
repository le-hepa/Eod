package com.gomgom.eod.feature.portinfo.porttool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gomgom.eod.feature.portinfo.porttool.entity.PortAttachmentEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortConditionEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortLocationEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortOperationEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordBundle
import com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortToolType
import com.gomgom.eod.feature.portinfo.porttool.repository.PortToolRepository
import com.gomgom.eod.feature.portinfo.porttool.repository.PortUnlocodeEntry
import com.gomgom.eod.feature.portinfo.porttool.repository.PortUnlocodeLookupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PortToolUiState(
    val activeSource: PortToolSource = PortToolSource.LOCAL,
    val searchMode: PortToolSearchMode = PortToolSearchMode.COUNTRY_PORT,
    val countryKeyword: String = "",
    val portKeyword: String = "",
    val searchResults: List<PortRecordEntity> = emptyList(),
    val countrySuggestions: List<PortUnlocodeEntry> = emptyList(),
    val portSuggestions: List<PortUnlocodeEntry> = emptyList(),
    val isDataManageDialogVisible: Boolean = false,
    val isLiveSearchPromptVisible: Boolean = false,
    val isLiveSearchEnabled: Boolean = false,
    val isLiveSearchHeaderVisible: Boolean = false,
    val pendingLiveSearchCount: Int = 0
)

data class PortToolDbState(
    val records: List<PortRecordEntity> = emptyList(),
    val selectedRecord: PortRecordBundle? = null
)

data class PortToolTempState(
    val editingRecordId: Long? = null,
    val hasPendingChanges: Boolean = false,
    val liveSearchRequested: Boolean = false,
    val editorBundle: PortRecordBundle? = null,
    val isVesselReportingExpanded: Boolean = true,
    val isAnchorageExpanded: Boolean = true,
    val isBerthExpanded: Boolean = true
)

enum class PortToolSource { LOCAL, SHARED }
enum class PortToolSearchMode { COUNTRY_PORT, FULL_TEXT }

private data class PortSearchInput(
    val country: String = "",
    val port: String = "",
    val mode: PortToolSearchMode = PortToolSearchMode.COUNTRY_PORT,
    val liveEnabled: Boolean = false
)

class PortToolViewModel(
    private val repository: PortToolRepository,
    private val unlocodeRepository: PortUnlocodeLookupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortToolUiState())
    val uiState: StateFlow<PortToolUiState> = _uiState.asStateFlow()

    private val _dbState = MutableStateFlow(PortToolDbState())
    val dbState: StateFlow<PortToolDbState> = _dbState.asStateFlow()

    private val _tempState = MutableStateFlow(PortToolTempState())
    val tempState: StateFlow<PortToolTempState> = _tempState.asStateFlow()

    private val searchInput = MutableStateFlow(PortSearchInput())

    init {
        viewModelScope.launch {
            repository.observeRecords().collect { records ->
                _dbState.update { it.copy(records = records) }
            }
        }
        viewModelScope.launch {
            searchInput
                .debounce(350)
                .collectLatest { input ->
                    performSearch(input)
                }
        }
    }

    fun selectSource(source: PortToolSource) {
        _uiState.update { it.copy(activeSource = source) }
    }

    fun updateCountryKeyword(keyword: String) {
        _uiState.update { state -> state.copy(countryKeyword = keyword) }
        triggerSearch()
    }

    fun updatePortKeyword(keyword: String) {
        _uiState.update { state -> state.copy(portKeyword = keyword) }
        triggerSearch()
    }

    fun updateSearchMode(mode: PortToolSearchMode) {
        _uiState.update { it.copy(searchMode = mode) }
        triggerSearch()
    }

    fun loadRecord(recordId: Long) {
        viewModelScope.launch {
            val bundle = repository.getRecordBundle(recordId)
            _dbState.update { it.copy(selectedRecord = bundle) }
            _tempState.update { it.copy(editingRecordId = recordId, hasPendingChanges = false) }
        }
    }

    fun search() {
        viewModelScope.launch {
            performSearch(currentSearchInput())
        }
    }

    fun save(bundle: PortRecordBundle) {
        viewModelScope.launch {
            if (_uiState.value.activeSource == PortToolSource.SHARED) return@launch
            repository.saveRecordBundle(bundle)
            _dbState.update { it.copy(selectedRecord = bundle) }
            _tempState.update { it.copy(editingRecordId = bundle.record.id, hasPendingChanges = false, editorBundle = bundle) }
        }
    }

    fun delete(recordId: Long) {
        viewModelScope.launch {
            repository.deleteRecord(recordId)
            _dbState.update { it.copy(selectedRecord = null) }
            _tempState.update { PortToolTempState() }
        }
    }

    fun exportJson(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            onComplete(repository.exportJson())
        }
    }

    fun exportCsv(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            onComplete(repository.exportCsv())
        }
    }

    fun importJson(json: String, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            runCatching { repository.importJson(json) }
                .also(onComplete)
        }
    }

    fun enableLiveSearch() {
        _uiState.update {
            it.copy(
                isLiveSearchPromptVisible = false,
                isLiveSearchEnabled = true,
                isLiveSearchHeaderVisible = false
            )
        }
        refreshPendingLiveSearch()
    }

    fun dismissLiveSearchPrompt() {
        _uiState.update {
            it.copy(
                isLiveSearchPromptVisible = false,
                isLiveSearchHeaderVisible = it.pendingLiveSearchCount > 0 && !it.isLiveSearchEnabled
            )
        }
        refreshPendingLiveSearch()
    }

    fun dismissLiveSearchHeader() {
        _uiState.update { it.copy(isLiveSearchHeaderVisible = false) }
    }

    fun openRecord(recordId: Long?) {
        viewModelScope.launch {
            if (recordId == null) {
                val empty = createEmptyBundle()
                _dbState.update { it.copy(selectedRecord = null) }
                _tempState.update {
                    it.copy(
                        editingRecordId = null,
                        hasPendingChanges = false,
                        editorBundle = empty
                    )
                }
            } else {
                val bundle = repository.getRecordBundle(recordId)?.withDefaults()
                _dbState.update { it.copy(selectedRecord = bundle) }
                _tempState.update {
                    it.copy(
                        editingRecordId = recordId,
                        hasPendingChanges = false,
                        editorBundle = bundle
                    )
                }
            }
        }
    }

    fun updateEditorBundle(bundle: PortRecordBundle) {
        val normalizedBundle = bundle.withFormattedFields()
        _tempState.update {
            it.copy(
                editorBundle = normalizedBundle,
                hasPendingChanges = true
            )
        }
        val record = normalizedBundle.record
        val shouldPrompt = record.countryName.isNotBlank() &&
            record.portName.isNotBlank() &&
            !_uiState.value.isLiveSearchEnabled
        if (shouldPrompt) {
            _uiState.update { it.copy(isLiveSearchPromptVisible = true, isLiveSearchHeaderVisible = false) }
        }
        viewModelScope.launch {
            val mappedBundle = applyAutoMapping(normalizedBundle)
            refreshLookupSuggestions(mappedBundle)
            if (mappedBundle != normalizedBundle) {
                _tempState.update {
                    it.copy(
                        editorBundle = mappedBundle,
                        hasPendingChanges = true
                    )
                }
            }
            refreshPendingLiveSearch()
        }
    }

    fun saveCurrent() {
        val bundle = _tempState.value.editorBundle ?: return
        save(bundle)
    }

    fun toggleVesselReporting() {
        _tempState.update { it.copy(isVesselReportingExpanded = !it.isVesselReportingExpanded) }
    }

    fun toggleAnchorage() {
        _tempState.update { it.copy(isAnchorageExpanded = !it.isAnchorageExpanded) }
    }

    fun toggleBerth() {
        _tempState.update { it.copy(isBerthExpanded = !it.isBerthExpanded) }
    }

    fun addAttachments(items: List<PortAttachmentEntity>) {
        if (_uiState.value.activeSource == PortToolSource.SHARED) return
        val bundle = _tempState.value.editorBundle ?: return
        val merged = bundle.attachments + items
        updateEditorBundle(
            bundle.copy(
                record = bundle.record.copy(attachmentCount = merged.size),
                attachments = merged
            )
        )
    }

    fun removeAttachment(attachmentId: Long) {
        if (_uiState.value.activeSource == PortToolSource.SHARED) return
        val bundle = _tempState.value.editorBundle ?: return
        val remaining = bundle.attachments.filterNot { it.id == attachmentId }
        updateEditorBundle(
            bundle.copy(
                record = bundle.record.copy(attachmentCount = remaining.size),
                attachments = remaining
            )
        )
    }

    fun applyCountrySuggestion(suggestion: PortUnlocodeEntry) {
        val bundle = _tempState.value.editorBundle ?: return
        updateEditorBundle(
            bundle.copy(
                record = bundle.record.copy(
                    countryCode = suggestion.countryCode,
                    countryName = suggestion.countryName
                )
            )
        )
    }

    fun applyPortSuggestion(suggestion: PortUnlocodeEntry) {
        val bundle = _tempState.value.editorBundle ?: return
        updateEditorBundle(
            bundle.copy(
                record = bundle.record.copy(
                    countryCode = suggestion.countryCode,
                    countryName = suggestion.countryName,
                    portName = suggestion.portName,
                    unlocode = suggestion.locode
                )
            )
        )
    }

    private fun createEmptyBundle(): PortRecordBundle {
        val now = System.currentTimeMillis()
        val recordId = now
        return PortRecordBundle(
            record = PortRecordEntity(
                id = recordId,
                createdAt = now,
                updatedAt = now
            ),
            operations = defaultOperations(recordId, now),
            locations = defaultLocations(recordId, now)
        )
    }

    private fun PortRecordBundle.withDefaults(): PortRecordBundle {
        val now = System.currentTimeMillis()
        val currentRecordId = record.id
        val currentOperations = defaultOperations(currentRecordId, now).associateBy { it.operationType }.toMutableMap()
        operations.forEach { currentOperations[it.operationType] = it }
        val currentLocations = defaultLocations(currentRecordId, now).associateBy { it.locationType }.toMutableMap()
        locations.forEach { currentLocations[it.locationType] = it }
        return copy(
            operations = currentOperations.values.toList(),
            locations = currentLocations.values.toList()
        )
    }

    private fun defaultOperations(recordId: Long, now: Long): List<PortOperationEntity> = listOf(
        PortOperationEntity(id = recordId * 10 + 1, recordId = recordId, operationType = PortToolType.VTS, createdAt = now, updatedAt = now),
        PortOperationEntity(id = recordId * 10 + 2, recordId = recordId, operationType = PortToolType.PILOT, createdAt = now, updatedAt = now),
        PortOperationEntity(id = recordId * 10 + 3, recordId = recordId, operationType = PortToolType.TUG, createdAt = now, updatedAt = now),
        PortOperationEntity(id = recordId * 10 + 4, recordId = recordId, operationType = PortToolType.CIQ, createdAt = now, updatedAt = now)
    )

    private fun defaultLocations(recordId: Long, now: Long): List<PortLocationEntity> = listOf(
        PortLocationEntity(id = recordId * 10 + 5, recordId = recordId, locationType = PortToolType.ANCHORAGE, createdAt = now, updatedAt = now),
        PortLocationEntity(id = recordId * 10 + 6, recordId = recordId, locationType = PortToolType.BERTH, createdAt = now, updatedAt = now)
    )

    private fun triggerSearch() {
        searchInput.value = currentSearchInput()
    }

    private fun currentSearchInput(): PortSearchInput {
        val state = _uiState.value
        return PortSearchInput(
            country = state.countryKeyword.trim(),
            port = state.portKeyword.trim(),
            mode = state.searchMode,
            liveEnabled = state.isLiveSearchEnabled
        )
    }

    private suspend fun performSearch(input: PortSearchInput) {
        val results = when {
            input.country.isBlank() && input.port.isBlank() -> emptyList()
            input.mode == PortToolSearchMode.COUNTRY_PORT ->
                repository.searchCountryPort(input.country, input.port)
            else -> repository.searchAll(input.country, input.port)
        }
        _uiState.update {
            it.copy(
                searchResults = results,
                pendingLiveSearchCount = results.size
            )
        }
    }

    private suspend fun applyAutoMapping(bundle: PortRecordBundle): PortRecordBundle {
        val record = bundle.record
        if (record.unlocode.isNotBlank()) {
            val unlocodeMatched = unlocodeRepository.findByUnlocode(record.unlocode.trim())
            val recordMatched = repository.searchByUnlocode(record.unlocode.trim()).firstOrNull()
            val countryCode = unlocodeMatched?.countryCode ?: recordMatched?.countryCode
            val countryName = unlocodeMatched?.countryName ?: recordMatched?.countryName
            val portName = unlocodeMatched?.portName ?: recordMatched?.portName
            val unlocode = unlocodeMatched?.locode ?: recordMatched?.unlocode
            if (countryCode == null || countryName == null || portName == null || unlocode == null) return bundle
            return bundle.copy(
                record = record.copy(
                    countryCode = countryCode,
                    countryName = countryName,
                    portName = portName,
                    unlocode = unlocode
                )
            )
        }

        if (record.countryName.isBlank() && record.portName.isBlank()) return bundle

        val unlocodeMatched = unlocodeRepository.resolve(record.countryName, record.portName)
        val recordMatched = repository
            .searchCountryPort(record.countryName, record.portName)
            .firstOrNull {
                (record.countryName.isBlank() || it.countryName.equals(record.countryName, ignoreCase = true) || it.countryCode.equals(record.countryName, ignoreCase = true)) &&
                    (record.portName.isBlank() || it.portName.equals(record.portName, ignoreCase = true))
            }
        val countryCode = unlocodeMatched?.countryCode ?: recordMatched?.countryCode
        val countryName = unlocodeMatched?.countryName ?: recordMatched?.countryName
        val portName = unlocodeMatched?.portName ?: recordMatched?.portName
        val unlocode = unlocodeMatched?.locode ?: recordMatched?.unlocode
        if (countryCode == null || countryName == null || portName == null || unlocode == null) return bundle
        return bundle.copy(
            record = record.copy(
                countryCode = if (record.countryCode.isBlank()) countryCode else record.countryCode,
                countryName = if (record.countryName.isBlank()) countryName else record.countryName,
                portName = if (record.portName.isBlank()) portName else record.portName,
                unlocode = unlocode
            )
        )
    }

    private fun refreshPendingLiveSearch() {
        viewModelScope.launch {
            val currentRecord = _tempState.value.editorBundle?.record ?: return@launch
            if (currentRecord.countryName.isBlank() || currentRecord.portName.isBlank()) {
                _uiState.update { it.copy(pendingLiveSearchCount = 0, isLiveSearchHeaderVisible = false) }
                return@launch
            }
            val results = repository.searchCountryPort(currentRecord.countryName, currentRecord.portName)
            _uiState.update {
                it.copy(
                    pendingLiveSearchCount = results.size,
                    isLiveSearchHeaderVisible = results.isNotEmpty() && !it.isLiveSearchEnabled && !it.isLiveSearchPromptVisible
                )
            }
        }
    }

    private suspend fun refreshLookupSuggestions(bundle: PortRecordBundle) {
        val record = bundle.record
        val countries = unlocodeRepository.searchCountries(record.countryName)
        val ports = unlocodeRepository.searchPorts(record.portName, record.countryName.ifBlank { record.countryCode })
        _uiState.update {
            it.copy(
                countrySuggestions = countries,
                portSuggestions = ports
            )
        }
    }
}

private fun PortRecordBundle.withFormattedFields(): PortRecordBundle {
    val anchorage = locations.firstOrNull { it.locationType == PortToolType.ANCHORAGE }
    val berth = locations.firstOrNull { it.locationType == PortToolType.BERTH }
    val formattedRecord = record.copy(
        transferRate = record.transferRate.filter { it.isDigit() || it == '.' },
        manifoldSize = record.manifoldSize.filter { it.isDigit() }.take(4)
    )
    val formattedLocations = locations.map { location ->
        when (location.locationType) {
            PortToolType.BERTH -> location.copy(
                berthLengthMeters = formatMeters(location.berthLengthMeters, keepDecimals = false),
                depthMeters = formatMeters(location.depthMeters, keepDecimals = true),
                mooringFore = location.mooringFore.removePrefix("선수 : ").trim(),
                mooringAft = location.mooringAft.removePrefix("선미 : ").trim()
            )
            else -> location
        }
    }
    return copy(
        record = formattedRecord.copy(
            anchorageName = anchorage?.name ?: formattedRecord.anchorageName,
            berthName = berth?.name ?: formattedRecord.berthName,
            company = berth?.company ?: formattedRecord.company
        ),
        locations = formattedLocations
    )
}

private fun formatMeters(raw: String, keepDecimals: Boolean): String {
    val cleaned = raw.replace("m", "", ignoreCase = true).trim()
    if (cleaned.isBlank()) return ""
    return cleaned.toDoubleOrNull()?.let {
        if (keepDecimals) String.format("%.2fm", it) else "${it.toInt()}m"
    } ?: raw
}
