package com.gomgom.eod.feature.cargoinfo.cargotool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gomgom.eod.feature.cargoinfo.cargotool.entity.CargoRecordBundle
import com.gomgom.eod.feature.cargoinfo.cargotool.entity.CargoRecordEntity
import com.gomgom.eod.feature.cargoinfo.cargotool.repository.CargoToolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CargoToolUiState(
    val activeSource: CargoToolSource = CargoToolSource.LOCAL,
    val keyword: String = "",
    val searchResults: List<CargoRecordEntity> = emptyList()
)

data class CargoToolDbState(
    val records: List<CargoRecordEntity> = emptyList(),
    val selectedRecord: CargoRecordBundle? = null
)

data class CargoToolTempState(
    val editingRecordId: Long? = null,
    val hasPendingChanges: Boolean = false
)

enum class CargoToolSource { LOCAL, SHARED }

class CargoToolViewModel(
    private val repository: CargoToolRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CargoToolUiState())
    val uiState: StateFlow<CargoToolUiState> = _uiState.asStateFlow()

    private val _dbState = MutableStateFlow(CargoToolDbState())
    val dbState: StateFlow<CargoToolDbState> = _dbState.asStateFlow()

    private val _tempState = MutableStateFlow(CargoToolTempState())
    val tempState: StateFlow<CargoToolTempState> = _tempState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeRecords().collect { records ->
                _dbState.update { it.copy(records = records) }
            }
        }
    }

    fun selectSource(source: CargoToolSource) {
        _uiState.update { it.copy(activeSource = source) }
    }

    fun updateKeyword(keyword: String) {
        _uiState.update { it.copy(keyword = keyword) }
    }

    fun search() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(searchResults = repository.search(it.keyword))
            }
        }
    }

    fun loadRecord(recordId: Long) {
        viewModelScope.launch {
            _dbState.update { it.copy(selectedRecord = repository.getRecordBundle(recordId)) }
        }
    }

    fun save(bundle: CargoRecordBundle) {
        viewModelScope.launch {
            repository.saveRecordBundle(bundle)
            _dbState.update { it.copy(selectedRecord = bundle) }
            _tempState.update { it.copy(editingRecordId = bundle.record.id, hasPendingChanges = false) }
        }
    }
}
