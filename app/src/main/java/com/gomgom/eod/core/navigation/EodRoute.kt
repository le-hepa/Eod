package com.gomgom.eod.core.navigation

object EodRoute {
    const val HOME = "home"
    const val HOME_LANGUAGE = "home_language"
    const val HOME_APP_INFO = "home_app_info"
    const val HOME_GUIDE = "home_guide"
    const val HOME_CONTACT = "home_contact"

    const val TASK_TOP = "task_top"
    const val TASK_PRESET = "task_preset"
    const val TASK_PRESET_ADD = "task_preset_add"
    const val TASK_PRESET_DETAIL = "task_preset_detail/{presetId}"
    const val TASK_PRESET_WORK_ADD = "task_preset_work_add/{presetId}"
    const val TASK_PRESET_WORK_DETAIL = "task_preset_work_detail/{presetId}/{workId}"
    const val TASK_ALARM = "task_alarm"
    const val TASK_VESSEL_ADD = "task_vessel_add"
    const val TASK_DATA_MANAGE = "task_data_manage"
    const val TASK_VESSEL_DETAIL = "task_vessel_detail/{vesselId}"

    const val PORTINFO_TOP = "portinfo_top"
    const val PORTINFO_RECORD_DETAIL = "portinfo_record_detail/{recordId}"
    const val PORTINFO_RECORD_EDITOR = "portinfo_record_editor/{recordId}"

    const val CARGOINFO_TOP = "cargoinfo_top"

    fun taskPresetDetail(presetId: Long): String = "task_preset_detail/$presetId"
    fun taskPresetWorkAdd(presetId: Long): String = "task_preset_work_add/$presetId"
    fun taskPresetWorkDetail(presetId: Long, workId: Long): String = "task_preset_work_detail/$presetId/$workId"
    fun taskVesselDetail(vesselId: Long): String = "task_vessel_detail/$vesselId"
    fun portInfoRecordDetail(recordId: Long): String = "portinfo_record_detail/$recordId"
    fun portInfoRecordEditor(recordId: Long): String = "portinfo_record_editor/$recordId"
}