package com.gomgom.eod.feature.task.viewmodel

import androidx.lifecycle.ViewModel
import com.gomgom.eod.feature.task.repository.TaskAlarmSettingsRepository
import com.gomgom.eod.feature.task.repository.TaskAlarmSettingsRepositoryProvider
import com.gomgom.eod.feature.task.repository.TaskTopRepository
import com.gomgom.eod.feature.task.repository.TaskTopRepositoryProvider
import com.gomgom.eod.feature.task.repository.TaskWorkRecordRepository
import com.gomgom.eod.feature.task.repository.TaskWorkRecordRepositoryProvider
import org.json.JSONArray
import org.json.JSONObject

class TaskDataManageViewModel(
    private val alarmSettingsRepository: TaskAlarmSettingsRepository = TaskAlarmSettingsRepositoryProvider.repository,
    private val topRepository: TaskTopRepository = TaskTopRepositoryProvider.repository,
    private val workRecordRepository: TaskWorkRecordRepository = TaskWorkRecordRepositoryProvider.repository
) : ViewModel() {

    fun presetGroups(): List<TaskPresetGroupItem> = TaskPresetStateStore.snapshot()

    fun exportFileName(selectedPresetIds: Set<Long>): String {
        val selectedGroups = TaskPresetStateStore.snapshot()
            .filter { it.id in selectedPresetIds }
            .sortedBy { it.id }

        val baseName = when {
            selectedGroups.isEmpty() -> "eod_task_presets"
            selectedGroups.size == 1 -> "eod_task_presets_${sanitizeFileName(selectedGroups.first().name)}"
            else -> "eod_task_presets_${sanitizeFileName(selectedGroups.first().name)}_plus_${selectedGroups.size - 1}"
        }
        return "$baseName.json"
    }

    fun exportPresetsJson(selectedPresetIds: Set<Long>): String? {
        if (selectedPresetIds.isEmpty()) return null

        val selectedGroups = TaskPresetStateStore.snapshot()
            .filter { it.id in selectedPresetIds }
            .sortedBy { it.id }
        if (selectedGroups.isEmpty()) return null

        val selectedWorks = TaskPresetWorkStateStore.snapshot()
            .filter { it.presetId in selectedPresetIds }
            .sortedWith(
                compareBy<TaskPresetWorkItem> { it.presetId }
                    .thenBy { it.id }
            )

        val root = JSONObject()
        root.put("version", 1)
        root.put(
            "topPresets",
            JSONArray().apply {
                selectedGroups.forEach { preset ->
                    put(
                        JSONObject().apply {
                            put("id", preset.id)
                            put("name", preset.name)
                            put("enabled", preset.enabled)
                        }
                    )
                }
            }
        )
        root.put(
            "childWorks",
            JSONArray().apply {
                selectedWorks.forEach { work ->
                    put(
                        JSONObject().apply {
                            put("id", work.id)
                            put("presetId", work.presetId)
                            put("name", work.name)
                            put("reference", work.reference)
                            put("cycleNumber", work.cycleNumber)
                            put("cycleUnit", work.cycleUnit.name)
                            put("alarmEnabled", work.alarmEnabled)
                        }
                    )
                }
            }
        )
        return root.toString(2)
    }

    fun importPresetsJson(json: String): Boolean {
        return runCatching {
            val root = JSONObject(json)
            val presetArray = root.optJSONArray("topPresets") ?: JSONArray()
            val workArray = root.optJSONArray("childWorks") ?: JSONArray()

            val parsedPresets = buildList {
                for (index in 0 until presetArray.length()) {
                    val item = presetArray.getJSONObject(index)
                    add(
                        TaskPresetGroupItem(
                            id = item.getLong("id"),
                            name = item.getString("name").trim(),
                            enabled = item.optBoolean("enabled", false)
                        )
                    )
                }
            }

            val parsedWorks = buildList {
                for (index in 0 until workArray.length()) {
                    val item = workArray.getJSONObject(index)
                    add(
                        TaskPresetWorkItem(
                            id = item.getLong("id"),
                            presetId = item.getLong("presetId"),
                            name = item.getString("name").trim(),
                            reference = item.optString("reference").trim(),
                            cycleNumber = item.getInt("cycleNumber"),
                            cycleUnit = CycleUnit.valueOf(item.getString("cycleUnit")),
                            alarmEnabled = item.optBoolean("alarmEnabled", true)
                        )
                    )
                }
            }

            val currentPresets = TaskPresetStateStore.snapshot()
            val currentWorks = TaskPresetWorkStateStore.snapshot()

            var nextPresetId = (currentPresets.maxOfOrNull { it.id } ?: 0L) + 1L
            var nextWorkId = (currentWorks.maxOfOrNull { it.id } ?: 0L) + 1L
            val usedNames = currentPresets.map { it.name.trim() }.toMutableSet()
            val presetIdMap = mutableMapOf<Long, Long>()

            val mergedPresets = currentPresets.toMutableList()
            parsedPresets.forEachIndexed { index, preset ->
                val uniqueName = makeUniquePresetName(
                    originalName = preset.name,
                    usedNames = usedNames
                )
                val newId = nextPresetId++
                presetIdMap[preset.id] = newId
                mergedPresets += TaskPresetGroupItem(
                    id = newId,
                    name = uniqueName,
                    enabled = currentPresets.isEmpty() && index == 0
                )
            }

            val mergedWorks = currentWorks.toMutableList()
            parsedWorks.forEach { work ->
                val mappedPresetId = presetIdMap[work.presetId] ?: return@forEach
                mergedWorks += work.copy(
                    id = nextWorkId++,
                    presetId = mappedPresetId
                )
            }

            TaskPresetStateStore.replaceAll(mergedPresets)
            TaskPresetWorkStateStore.replaceAll(mergedWorks)
        }.isSuccess
    }

    fun clearPresets() {
        TaskPresetStateStore.clearAll()
        TaskPresetWorkStateStore.clearAll()
    }

    fun clearWorkRecords() {
        workRecordRepository.clearAll()
    }

    fun clearTaskApp() {
        clearPresets()
        clearWorkRecords()
        alarmSettingsRepository.clearAll()
        topRepository.clearAll()
    }

    private fun sanitizeFileName(name: String): String {
        return name.trim()
            .replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_")
            .trim('_')
            .ifBlank { "preset" }
    }

    private fun makeUniquePresetName(
        originalName: String,
        usedNames: MutableSet<String>
    ): String {
        val trimmed = originalName.trim().ifBlank { "프리셋" }
        if (usedNames.add(trimmed)) return trimmed

        var suffix = 2
        while (true) {
            val candidate = "$trimmed($suffix)"
            if (usedNames.add(candidate)) return candidate
            suffix += 1
        }
    }
}
