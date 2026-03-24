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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PortToolUiState(
    val activeSource: PortToolSource = PortToolSource.LOCAL,
    val searchMode: PortToolSearchMode = PortToolSearchMode.COUNTRY_PORT,
    val topSearchQuery: String = "",
    val searchQuery: String = "",
    val activeSearchSource: PortSearchSourceField = PortSearchSourceField.TOP,
    val searchSourceField: PortSearchSourceField = PortSearchSourceField.TOP,
    val searchFieldKey: String = "",
    val searchResults: List<PortSearchResultItem> = emptyList(),
    val isSearchResultVisible: Boolean = false,
    val searchDisplayMode: PortSearchDisplayMode = PortSearchDisplayMode.PUSH,
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
    val focusedField: PortEditorFocusField? = null,
    val isVesselReportingExpanded: Boolean = true,
    val isAnchorageExpanded: Boolean = true,
    val isBerthExpanded: Boolean = true
)

enum class PortToolSource { LOCAL, SHARED }
enum class PortToolSearchMode { COUNTRY_PORT, FULL_TEXT }
enum class PortEditorFocusField { COUNTRY, PORT, UNLOCODE }
enum class PortSearchSourceField { TOP, COUNTRY, PORT, CODE, FORM }
enum class PortSearchDisplayMode { PUSH, FLOAT }

data class PortSearchResultItem(
    val key: String,
    val countryCode: String,
    val country: String,
    val port: String,
    val unlocode: String,
    val previewFieldKey: String = "",
    val preview: String = "",
    val recordId: Long? = null
)

private data class PortToolDraftSnapshot(
    val source: PortToolSource,
    val bundle: PortRecordBundle?,
    val editingRecordId: Long?,
    val hasPendingChanges: Boolean,
    val focusedField: PortEditorFocusField?,
    val isVesselReportingExpanded: Boolean,
    val isAnchorageExpanded: Boolean,
    val isBerthExpanded: Boolean
)

private object PortToolDraftStore {
    private val drafts = mutableMapOf<PortToolSource, PortToolDraftSnapshot>()

    @Synchronized
    fun save(snapshot: PortToolDraftSnapshot) {
        drafts[snapshot.source] = snapshot
    }

    @Synchronized
    fun get(source: PortToolSource): PortToolDraftSnapshot? = drafts[source]

    @Synchronized
    fun clear(source: PortToolSource) {
        drafts.remove(source)
    }
}

private data class PortSearchInput(
    val query: String = "",
    val sourceField: PortSearchSourceField = PortSearchSourceField.TOP,
    val fieldKey: String = "",
    val mode: PortToolSearchMode = PortToolSearchMode.COUNTRY_PORT,
    val liveEnabled: Boolean = false,
    val countryHint: String = "",
    val portHint: String = ""
)

private data class PortEditorProcessingInput(
    val bundle: PortRecordBundle,
    val activeSource: PortToolSource
)

@OptIn(FlowPreview::class)
class PortToolViewModel(
    private val repository: PortToolRepository,
    private val unlocodeRepository: PortUnlocodeLookupRepository
) : ViewModel() {

    companion object {
        const val SEARCH_FIELD_COUNTRY_NAME = "countryName"
        const val SEARCH_FIELD_PORT_NAME = "portName"
        const val SEARCH_FIELD_UNLOCODE = "unlocode"
    }

    private val _uiState = MutableStateFlow(PortToolUiState())
    val uiState: StateFlow<PortToolUiState> = _uiState.asStateFlow()

    private val _dbState = MutableStateFlow(PortToolDbState())
    val dbState: StateFlow<PortToolDbState> = _dbState.asStateFlow()

    private val _tempState = MutableStateFlow(PortToolTempState())
    val tempState: StateFlow<PortToolTempState> = _tempState.asStateFlow()

    private val searchInput = MutableStateFlow(PortSearchInput())
    private val editorProcessingInput = MutableStateFlow<PortEditorProcessingInput?>(null)
    private var lastEditorProcessingKey: String? = null

    init {
        viewModelScope.launch {
            repository.observeRecords().collect { records ->
                _dbState.update { it.copy(records = records) }
            }
        }
        viewModelScope.launch {
            searchInput
                .debounce(350)
                .distinctUntilChanged()
                .collectLatest { input ->
                    performSearch(input)
                }
        }
        viewModelScope.launch {
            editorProcessingInput
                .filterNotNull()
                .debounce(350)
                .distinctUntilChangedBy { editorProcessingKey(it.bundle, it.activeSource, _tempState.value.focusedField) }
                .collectLatest { input ->
                    processEditorInput(input)
                }
        }
    }

    fun selectSource(source: PortToolSource) {
        _uiState.update { it.copy(activeSource = source) }
    }

    fun restoreDraftOrOpenEmpty() {
        val source = _uiState.value.activeSource
        val draft = PortToolDraftStore.get(source)
        if (draft?.bundle != null) {
            _dbState.update { it.copy(selectedRecord = draft.bundle) }
            _tempState.update {
                it.copy(
                    editingRecordId = draft.editingRecordId,
                    hasPendingChanges = draft.hasPendingChanges,
                    editorBundle = draft.bundle,
                    focusedField = draft.focusedField,
                    isVesselReportingExpanded = draft.isVesselReportingExpanded,
                    isAnchorageExpanded = draft.isAnchorageExpanded,
                    isBerthExpanded = draft.isBerthExpanded
                )
            }
            editorProcessingInput.value = PortEditorProcessingInput(draft.bundle, source)
            return
        }
        openRecord(null)
    }

    fun hasPendingNewDraftForActiveSource(): Boolean {
        val source = _uiState.value.activeSource
        val draft = PortToolDraftStore.get(source) ?: return false
        return draft.bundle != null && draft.editingRecordId == null && draft.hasPendingChanges
    }

    fun restorePendingNewDraftOrOpenEmpty() {
        val source = _uiState.value.activeSource
        val draft = PortToolDraftStore.get(source)
        if (draft?.bundle != null && draft.editingRecordId == null && draft.hasPendingChanges) {
            _dbState.update { it.copy(selectedRecord = null) }
            _tempState.update {
                it.copy(
                    editingRecordId = null,
                    hasPendingChanges = draft.hasPendingChanges,
                    editorBundle = draft.bundle,
                    focusedField = draft.focusedField,
                    isVesselReportingExpanded = draft.isVesselReportingExpanded,
                    isAnchorageExpanded = draft.isAnchorageExpanded,
                    isBerthExpanded = draft.isBerthExpanded
                )
            }
            editorProcessingInput.value = PortEditorProcessingInput(draft.bundle, source)
            return
        }
        openRecord(null)
    }

    fun clearEditorState() {
        _dbState.update { it.copy(selectedRecord = null) }
        _tempState.update {
            it.copy(
                editingRecordId = null,
                hasPendingChanges = false,
                editorBundle = null,
                focusedField = null
            )
        }
    }

    fun updateSearchQuery(keyword: String) {
        _uiState.update { state ->
            state.copy(
                topSearchQuery = keyword,
                searchQuery = keyword,
                activeSearchSource = PortSearchSourceField.TOP,
                searchSourceField = PortSearchSourceField.TOP,
                searchFieldKey = "",
                isSearchResultVisible = keyword.isNotBlank(),
                searchResults = if (keyword.isBlank()) emptyList() else state.searchResults
            )
        }
        triggerSearch()
    }

    fun updateFieldSearchInput(field: PortSearchSourceField, keyword: String) {
        if (_uiState.value.searchMode == PortToolSearchMode.FULL_TEXT) {
            val fieldKey = when (field) {
                PortSearchSourceField.COUNTRY -> SEARCH_FIELD_COUNTRY_NAME
                PortSearchSourceField.PORT -> SEARCH_FIELD_PORT_NAME
                PortSearchSourceField.CODE -> SEARCH_FIELD_UNLOCODE
                else -> ""
            }
            _uiState.update { state ->
                state.copy(
                    topSearchQuery = "",
                    searchQuery = keyword,
                    activeSearchSource = field,
                    searchSourceField = field,
                    searchFieldKey = fieldKey,
                    isSearchResultVisible = keyword.isNotBlank(),
                    searchResults = if (keyword.isBlank()) emptyList() else state.searchResults
                )
            }
            triggerSearch()
            return
        }
        _uiState.update { state ->
            state.copy(
                topSearchQuery = "",
                searchQuery = keyword,
                activeSearchSource = field,
                searchSourceField = field,
                searchFieldKey = "",
                isSearchResultVisible = keyword.isNotBlank(),
                searchResults = if (keyword.isBlank()) emptyList() else state.searchResults
            )
        }
        triggerSearch()
    }

    fun updateFormFieldSearchInput(fieldKey: String, keyword: String) {
        if (_uiState.value.searchMode != PortToolSearchMode.FULL_TEXT) {
            _uiState.update {
                it.copy(
                    searchQuery = "",
                    searchFieldKey = "",
                    isSearchResultVisible = false,
                    searchResults = emptyList()
                )
            }
            return
        }
        _uiState.update { state ->
            state.copy(
                topSearchQuery = "",
                searchQuery = keyword,
                activeSearchSource = PortSearchSourceField.FORM,
                searchSourceField = PortSearchSourceField.FORM,
                searchFieldKey = fieldKey,
                isSearchResultVisible = keyword.isNotBlank(),
                searchResults = if (keyword.isBlank()) emptyList() else state.searchResults
            )
        }
        triggerSearch()
    }

    fun updateSearchMode(mode: PortToolSearchMode) {
        _uiState.update { current ->
            if (current.searchMode == mode) current else current.copy(searchMode = mode)
        }
        triggerSearch()
    }

    fun prepareListMode() {
        _uiState.update {
            it.copy(
                searchMode = PortToolSearchMode.FULL_TEXT,
                topSearchQuery = "",
                searchQuery = "",
                activeSearchSource = PortSearchSourceField.TOP,
                searchSourceField = PortSearchSourceField.TOP,
                searchFieldKey = "",
                pendingLiveSearchCount = 0,
                isSearchResultVisible = false,
                searchResults = emptyList()
            )
        }
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

    fun updateSearchDisplayMode(displayMode: PortSearchDisplayMode) {
        _uiState.update { current ->
            if (current.searchDisplayMode == displayMode) current else current.copy(searchDisplayMode = displayMode)
        }
    }

    fun selectSearchResult(item: PortSearchResultItem) {
        if (item.recordId != null) {
            openRecord(item.recordId)
            _uiState.update {
                it.copy(
                    topSearchQuery = "",
                    searchQuery = "",
                    isSearchResultVisible = false,
                    searchResults = emptyList()
                )
            }
            return
        }
        val bundle = _tempState.value.editorBundle ?: createEmptyBundle()
        updateEditorBundle(
            bundle.copy(
                record = bundle.record.copy(
                    countryCode = item.countryCode,
                    countryName = item.country,
                    portName = item.port.takeIf { it != "-" }.orEmpty(),
                    unlocode = item.unlocode
                )
            )
        )
        _uiState.update {
            it.copy(
                topSearchQuery = "",
                searchQuery = "",
                isSearchResultVisible = false,
                searchResults = emptyList()
            )
        }
    }

    fun save(bundle: PortRecordBundle) {
        viewModelScope.launch {
            if (_uiState.value.activeSource == PortToolSource.SHARED) return@launch
            repository.saveRecordBundle(bundle)
            _dbState.update { it.copy(selectedRecord = bundle) }
            _tempState.update { current ->
                current.copy(editingRecordId = bundle.record.id, hasPendingChanges = false, editorBundle = bundle)
            }
            persistDraft()
        }
    }

    fun delete(recordId: Long) {
        viewModelScope.launch {
            repository.deleteRecord(recordId)
            _dbState.update { it.copy(selectedRecord = null) }
            _tempState.update { PortToolTempState() }
            PortToolDraftStore.clear(_uiState.value.activeSource)
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
                isLiveSearchHeaderVisible = false
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
                persistDraft()
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
                persistDraft()
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
        persistDraft()
        editorProcessingInput.value = PortEditorProcessingInput(normalizedBundle, _uiState.value.activeSource)
    }

    fun updateFieldFocus(field: PortEditorFocusField, isFocused: Boolean) {
        _tempState.update { current ->
            current.copy(
                focusedField = when {
                    isFocused -> field
                    current.focusedField == field -> null
                    else -> current.focusedField
                }
            )
        }
        val currentBundle = _tempState.value.editorBundle ?: return
        editorProcessingInput.value = PortEditorProcessingInput(currentBundle, _uiState.value.activeSource)
        persistDraft()
    }

    fun saveCurrent() {
        val bundle = _tempState.value.editorBundle ?: return
        save(bundle)
    }

    fun toggleVesselReporting() {
        _tempState.update { it.copy(isVesselReportingExpanded = !it.isVesselReportingExpanded) }
        persistDraft()
    }

    fun toggleAnchorage() {
        _tempState.update { it.copy(isAnchorageExpanded = !it.isAnchorageExpanded) }
        persistDraft()
    }

    fun toggleBerth() {
        _tempState.update { it.copy(isBerthExpanded = !it.isBerthExpanded) }
        persistDraft()
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
        val next = currentSearchInput()
        if (searchInput.value != next) {
            searchInput.value = next
        }
    }

    private fun currentSearchInput(): PortSearchInput {
        val state = _uiState.value
        val record = _tempState.value.editorBundle?.record
        return PortSearchInput(
            query = state.searchQuery.trim(),
            sourceField = state.searchSourceField,
            fieldKey = state.searchFieldKey,
            mode = state.searchMode,
            liveEnabled = state.isLiveSearchEnabled,
            countryHint = record?.countryName.orEmpty().ifBlank { record?.countryCode.orEmpty() },
            portHint = record?.portName.orEmpty()
        )
    }

    private suspend fun performSearch(input: PortSearchInput) {
        val results = when {
            input.query.isBlank() -> emptyList()
            input.sourceField == PortSearchSourceField.TOP && input.mode == PortToolSearchMode.COUNTRY_PORT ->
                unlocodeRepository.searchGeneral(input.query, limit = 5)
                    .map { it.toSearchResultItem() }
            input.sourceField == PortSearchSourceField.TOP && input.mode == PortToolSearchMode.FULL_TEXT ->
                dbState.value.records
                    .searchSavedRecords(input.query)
                    .take(50)
                    .map { it.toSavedRecordSearchResultItem(input.query) }
            input.sourceField == PortSearchSourceField.COUNTRY ->
                if (input.mode == PortToolSearchMode.FULL_TEXT) {
                    dbState.value.records
                        .searchByFormField(SEARCH_FIELD_COUNTRY_NAME, input.query)
                        .take(50)
                        .map { it.toSearchResultItem() }
                } else {
                    unlocodeRepository.searchCountries(input.query, limit = 5)
                        .map { it.toSearchResultItem() }
                }
            input.sourceField == PortSearchSourceField.PORT ->
                if (input.mode == PortToolSearchMode.FULL_TEXT) {
                    dbState.value.records
                        .searchByFormField(SEARCH_FIELD_PORT_NAME, input.query)
                        .take(50)
                        .map { it.toSearchResultItem() }
                } else {
                    unlocodeRepository.searchPortsByName(input.query, input.countryHint, limit = 5)
                        .map { it.toSearchResultItem() }
                }
            input.sourceField == PortSearchSourceField.CODE && input.query.normalizeSearchQuery().length <= 2 ->
                if (input.mode == PortToolSearchMode.FULL_TEXT) {
                    dbState.value.records
                        .searchByFormField(SEARCH_FIELD_UNLOCODE, input.query)
                        .take(50)
                        .map { it.toSearchResultItem() }
                } else {
                    unlocodeRepository.searchCountriesByCode(input.query, limit = 5)
                        .map { it.toCountryCodeSearchResultItem() }
                }
            input.sourceField == PortSearchSourceField.CODE ->
                if (input.mode == PortToolSearchMode.FULL_TEXT) {
                    dbState.value.records
                        .searchByFormField(SEARCH_FIELD_UNLOCODE, input.query)
                        .take(50)
                        .map { it.toSearchResultItem() }
                } else {
                    unlocodeRepository.searchByCode(input.query, limit = 5)
                        .map { it.toSearchResultItem() }
                }
            input.sourceField == PortSearchSourceField.FORM ->
                dbState.value.records
                    .searchByFormField(input.fieldKey, input.query)
                    .take(50)
                    .map { it.toSearchResultItem() }
            input.mode == PortToolSearchMode.FULL_TEXT ->
                dbState.value.records
                    .searchFullRecords(input.query)
                    .take(50)
                    .map { it.toSearchResultItem() }
            else ->
                dbState.value.records
                    .searchCountryPortRecords(input.query)
                    .take(50)
                    .map { it.toSearchResultItem() }
        }
        _uiState.update {
            it.copy(
                searchResults = results,
                pendingLiveSearchCount = results.size,
                isSearchResultVisible = input.query.isNotBlank() && results.isNotEmpty()
            )
        }
    }

    private fun refreshPendingLiveSearch() {
        val currentBundle = _tempState.value.editorBundle ?: return
        editorProcessingInput.value = PortEditorProcessingInput(currentBundle, _uiState.value.activeSource)
    }

    private suspend fun processEditorInput(input: PortEditorProcessingInput) {
        val focusedField = _tempState.value.focusedField
        val processingKey = editorProcessingKey(input.bundle, input.activeSource, focusedField)
        if (processingKey == lastEditorProcessingKey) return

        val mappedBundle = input.bundle
        val record = mappedBundle.record

        val liveSearchDeferred = viewModelScope.async {
            if (record.countryName.isBlank() || record.portName.isBlank()) {
                0
            } else {
                repository.searchCountryPort(record.countryName, record.portName).size
            }
        }

        val pendingCount = liveSearchDeferred.await()

        lastEditorProcessingKey = processingKey

        _uiState.update {
            it.copy(
                pendingLiveSearchCount = pendingCount,
                isLiveSearchHeaderVisible = false,
                isLiveSearchPromptVisible = false
            )
        }
    }

    private fun persistDraft() {
        val source = _uiState.value.activeSource
        val temp = _tempState.value
        PortToolDraftStore.save(
            PortToolDraftSnapshot(
                source = source,
                bundle = temp.editorBundle,
                editingRecordId = temp.editingRecordId,
                hasPendingChanges = temp.hasPendingChanges,
                focusedField = temp.focusedField,
                isVesselReportingExpanded = temp.isVesselReportingExpanded,
                isAnchorageExpanded = temp.isAnchorageExpanded,
                isBerthExpanded = temp.isBerthExpanded
            )
        )
    }
}

private fun editorProcessingKey(
    bundle: PortRecordBundle,
    activeSource: PortToolSource,
    focusedField: PortEditorFocusField?
): String {
    val record = bundle.record
    return listOf(
        activeSource.name,
        record.countryCode,
        record.countryName,
        record.portName,
        record.unlocode,
        focusedField?.name.orEmpty()
    ).joinToString("|")
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

private fun PortRecordEntity.toSearchResultItem(): PortSearchResultItem =
    PortSearchResultItem(
        key = "record-$id",
        countryCode = countryCode,
        country = countryName.ifBlank { countryCode },
        port = portName,
        unlocode = unlocode.formatDisplayUnlocode(),
        recordId = id
    )

private fun PortUnlocodeEntry.toSearchResultItem(): PortSearchResultItem =
    PortSearchResultItem(
        key = "lookup-${countryCode}-${locode}-${portName}",
        countryCode = countryCode,
        country = countryName.ifBlank { countryCode },
        port = portName,
        unlocode = locode.formatDisplayUnlocode()
    )

private fun PortUnlocodeEntry.toCountryCodeSearchResultItem(): PortSearchResultItem =
    PortSearchResultItem(
        key = "country-${countryCode}",
        countryCode = countryCode,
        country = countryName.ifBlank { countryCode },
        port = "-",
        unlocode = countryCode,
        preview = ""
    )

private fun List<PortRecordEntity>.searchCountryPortRecords(query: String): List<PortRecordEntity> {
    val normalizedQuery = query.normalizeSearchQuery()
    return asSequence()
        .mapNotNull { record ->
            val country = record.countryName.ifBlank { record.countryCode }
            val port = record.portName
            val countryNorm = country.normalizeSearchQuery()
            val portNorm = port.normalizeSearchQuery()
            val score = bestMatchScore(normalizedQuery, listOf(countryNorm, portNorm))
            if (score == null) null else record to score
        }
        .sortedWith(
            compareBy<Pair<PortRecordEntity, MatchScore>>(
                { it.second.rank },
                { it.second.countryPriority },
                { it.second.lengthScore },
                { it.first.countryName.ifBlank { it.first.countryCode } },
                { it.first.portName }
            )
        )
        .map { it.first }
        .toList()
}

private fun List<PortRecordEntity>.searchFullRecords(query: String): List<PortRecordEntity> {
    val normalizedQuery = query.normalizeSearchQuery()
    return asSequence()
        .mapNotNull { record ->
            val fields = listOf(
                record.berthName,
                record.company,
                record.anchorageName,
                record.berthInfo,
                record.anchorageInfo,
                record.cargoName,
                record.generalRemark,
                record.caution,
                record.supplyRemark,
                record.wasteRemark,
                record.crewChangeRemark,
                record.cargoRemark,
                record.dischargeInfo,
                record.manifoldRemark
            )
            val score = bestMatchScore(normalizedQuery, fields.map { it.normalizeSearchQuery() }, "")
            if (score == null) null else record to score
        }
        .sortedWith(
            compareBy<Pair<PortRecordEntity, MatchScore>>(
                { it.second.rank },
                { it.second.countryPriority },
                { it.second.lengthScore },
                { it.first.countryName.ifBlank { it.first.countryCode } },
                { it.first.portName }
            )
        )
        .map { it.first }
        .toList()
}

private fun List<PortRecordEntity>.searchSavedRecords(query: String): List<PortRecordEntity> {
    val normalizedQuery = query.normalizeSearchQuery()
    return asSequence()
        .mapNotNull { record ->
            val fields = listOf(
                record.countryName.ifBlank { record.countryCode },
                record.portName,
                record.unlocode,
                record.berthName,
                record.company,
                record.anchorageName,
                record.berthInfo,
                record.anchorageInfo,
                record.cargoName,
                record.generalRemark,
                record.caution,
                record.supplyRemark,
                record.wasteRemark,
                record.crewChangeRemark,
                record.cargoRemark,
                record.dischargeInfo,
                record.manifoldRemark
            )
            val score = bestMatchScore(normalizedQuery, fields.map { it.normalizeSearchQuery() }, record.countryName.ifBlank { record.countryCode }.normalizeSearchQuery())
            if (score == null) null else record to score
        }
        .sortedWith(
            compareBy<Pair<PortRecordEntity, MatchScore>>(
                { it.second.rank },
                { it.second.countryPriority },
                { it.second.lengthScore },
                { it.first.countryName.ifBlank { it.first.countryCode } },
                { it.first.portName },
                { it.first.berthName }
            )
        )
        .map { it.first }
        .toList()
}

private fun PortRecordEntity.toSavedRecordSearchResultItem(query: String): PortSearchResultItem {
    val previewMatch = buildPreviewMatch(query)
    return PortSearchResultItem(
        key = "record-$id",
        countryCode = countryCode,
        country = countryName.ifBlank { countryCode },
        port = portName,
        unlocode = unlocode.formatDisplayUnlocode(),
        previewFieldKey = previewMatch?.first.orEmpty(),
        preview = previewMatch?.second.orEmpty(),
        recordId = id
    )
}

private fun List<PortRecordEntity>.searchByFormField(fieldKey: String, query: String): List<PortRecordEntity> {
    val normalizedQuery = query.normalizeSearchQuery()
    if (normalizedQuery.isBlank()) return emptyList()
    return asSequence()
        .mapNotNull { record ->
            val fieldValue = when (fieldKey) {
                "supplyStatus" -> record.supplyStatus
                "freshWaterStatus" -> record.freshWaterStatus
                "storeSpareStatus" -> record.storeSpareStatus
                "provisionsStatus" -> record.provisionsStatus
                "wasteStatus" -> record.wasteStatus
                "slopStatus" -> record.slopStatus
                "crewChangeStatus" -> record.crewChangeStatus
                "externalAuditStatus" -> record.externalAuditStatus
                "countryName" -> record.countryName.ifBlank { record.countryCode }
                "portName" -> record.portName
                "unlocode" -> record.unlocode
                "cargoName" -> record.cargoName
                "operatorName" -> record.operatorName
                "safetyOfficerName" -> record.safetyOfficerName
                "surveyorName" -> record.surveyorName
                "company" -> record.company
                "berthName" -> record.berthName
                "anchorageName" -> record.anchorageName
                "berthInfo" -> record.berthInfo
                "anchorageInfo" -> record.anchorageInfo
                "caution" -> record.caution
                "dischargeInfo" -> record.dischargeInfo
                "cargoRemark" -> record.cargoRemark
                "supplyRemark" -> record.supplyRemark
                "wasteRemark" -> record.wasteRemark
                "crewChangeRemark" -> record.crewChangeRemark
                else -> ""
            }.normalizeSearchQuery()
            val score = bestMatchScore(normalizedQuery, listOf(fieldValue), "")
            if (score == null) null else record to score
        }
        .sortedWith(
            compareBy<Pair<PortRecordEntity, MatchScore>>(
                { it.second.rank },
                { it.second.lengthScore },
                { it.first.countryName.ifBlank { it.first.countryCode } },
                { it.first.portName }
            )
        )
        .map { it.first }
        .toList()
}

private fun PortRecordEntity.buildPreviewMatch(query: String): Pair<String, String>? {
    val normalizedQuery = query.normalizeSearchQuery()
    if (normalizedQuery.isBlank()) return null
    val candidates = listOf(
        "company" to company,
        "berthName" to berthName,
        "anchorageName" to anchorageName,
        "cargoName" to cargoName,
        "berthInfo" to berthInfo,
        "anchorageInfo" to anchorageInfo,
        "generalRemark" to generalRemark,
        "caution" to caution,
        "supplyRemark" to supplyRemark,
        "wasteRemark" to wasteRemark,
        "crewChangeRemark" to crewChangeRemark,
        "cargoRemark" to cargoRemark,
        "dischargeInfo" to dischargeInfo,
        "manifoldRemark" to manifoldRemark
    )
    val match = candidates.firstOrNull { (_, value) ->
        value.normalizeSearchQuery().contains(normalizedQuery)
    } ?: return null
    return match.first to match.second.previewAround(normalizedQuery)
}

private fun String.previewAround(normalizedQuery: String): String {
    if (isBlank()) return ""
    val source = this
    val normalized = source.normalizeSearchQuery()
    val matchIndex = normalized.indexOf(normalizedQuery)
    if (matchIndex < 0) return source.take(12)

    val start = (matchIndex - 3).coerceAtLeast(0)
    val end = (matchIndex + normalizedQuery.length + 5).coerceAtMost(source.length)
    val prefix = if (start > 0) "..." else ""
    val suffix = if (end < source.length) "..." else ""
    return prefix + source.substring(start, end) + suffix
}

private data class MatchScore(
    val rank: Int,
    val countryPriority: Int,
    val lengthScore: Int
)

private fun bestMatchScore(
    query: String,
    fields: List<String>,
    countryField: String = fields.firstOrNull().orEmpty()
): MatchScore? {
    if (query.isBlank()) return null
    var bestRank: Int? = null
    var bestLength = Int.MAX_VALUE
    fields.forEach { field ->
        if (field.isBlank()) return@forEach
        val rank = matchRank(query, field) ?: return@forEach
        if (bestRank == null || rank < bestRank!! || (rank == bestRank && field.length < bestLength)) {
            bestRank = rank
            bestLength = field.length
        }
    }
    val finalRank = bestRank ?: return null
    return MatchScore(
        rank = finalRank,
        countryPriority = if (countryField == query || countryField.startsWith(query)) 0 else 1,
        lengthScore = bestLength
    )
}

private fun matchRank(query: String, field: String): Int? {
    if (query.isBlank() || field.isBlank()) return null
    return when {
        field == query -> 0
        field.startsWith(query) -> 1
        field.splitWordStarts().any { it.startsWith(query) } -> 2
        field.contains(query) -> 3
        else -> null
    }
}

private fun String.splitWordStarts(): List<String> =
    lowercase()
        .replace("-", " ")
        .split(" ")
        .map { it.trim() }
        .filter { it.isNotBlank() }

private fun String.normalizeSearchQuery(): String =
    lowercase().replace(" ", "").replace("-", "").trim()

private fun String.formatDisplayUnlocode(): String {
    val cleaned = uppercase().replace("-", "").trim()
    return if (cleaned.length == 5) "${cleaned.take(2)}-${cleaned.drop(2)}" else uppercase()
}
