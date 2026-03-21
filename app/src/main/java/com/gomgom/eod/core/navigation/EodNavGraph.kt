package com.gomgom.eod.core.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gomgom.eod.R
import com.gomgom.eod.core.common.AppLanguageManager
import com.gomgom.eod.feature.cargoinfo.screens.CargoInfoTopPlaceholderScreen
import com.gomgom.eod.feature.home.screens.EodHomeScreen
import com.gomgom.eod.feature.portinfo.screens.PortInfoRecordDetailScreen
import com.gomgom.eod.feature.portinfo.screens.PortInfoRecordEditorScreen
import com.gomgom.eod.feature.portinfo.wrapper.PortInfoToolWrapper
import com.gomgom.eod.feature.task.screens.TaskAlarmScreen
import com.gomgom.eod.feature.task.screens.TaskDataManageScreen
import com.gomgom.eod.feature.task.screens.TaskPresetAddScreen
import com.gomgom.eod.feature.task.screens.TaskPresetDetailScreen
import com.gomgom.eod.feature.task.screens.TaskPresetScreen
import com.gomgom.eod.feature.task.screens.TaskPresetWorkAddScreen
import com.gomgom.eod.feature.task.screens.TaskPresetWorkDetailScreen
import com.gomgom.eod.feature.task.screens.TaskGuideScreen
import com.gomgom.eod.feature.task.screens.TaskVesselAddScreen
import com.gomgom.eod.feature.task.screens.TaskVesselDetailScreen
import com.gomgom.eod.feature.task.wrapper.TaskToolWrapper

@Composable
fun EodNavGraph(
    onExitApp: () -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val alarmTarget by TaskAlarmNavigationBridge.target.collectAsState()

    val mailTo = stringResource(R.string.home_contact_mail_to)
    val mailSubject = stringResource(R.string.home_contact_mail_subject)
    val versionValue = stringResource(R.string.home_app_info_version_value)
    val openMailError = stringResource(R.string.home_contact_mail_open_error)

    val goHome: () -> Unit = {
        navController.navigate(EodRoute.HOME) {
            popUpTo(EodRoute.HOME) { inclusive = true }
        }
    }

    val applyKor: () -> Unit = {
        AppLanguageManager.applyKor(context)
        (context as? Activity)?.recreate()
    }

    val applyEng: () -> Unit = {
        AppLanguageManager.applyEng(context)
        (context as? Activity)?.recreate()
    }

    val openContactMail: () -> Unit = {
        val body = buildString {
            append("App Version: $versionValue\n")
            append("Android Version: ${Build.VERSION.RELEASE}\n")
            append("Device Model: ${Build.MANUFACTURER} ${Build.MODEL}\n")
            append("Issue:\n")
        }

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$mailTo")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(mailTo))
            putExtra(Intent.EXTRA_SUBJECT, mailSubject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, openMailError, Toast.LENGTH_SHORT).show()
        }
    }

    NavHost(
        navController = navController,
        startDestination = EodRoute.HOME
    ) {
        composable(EodRoute.HOME) {
            EodHomeScreen(
                onTaskClick = { navController.navigate(EodRoute.TASK_TOP) },
                onPortInfoClick = { navController.navigate(EodRoute.PORTINFO_TOP) },
                onCargoInfoClick = { navController.navigate(EodRoute.CARGOINFO_TOP) },
                onHomeClick = goHome,
                onKorClick = applyKor,
                onEngClick = applyEng,
                onGuideClick = { navController.navigate(EodRoute.TASK_GUIDE) },
                onContactClick = openContactMail,
                onExitClick = onExitApp
            )
        }

        composable(EodRoute.TASK_TOP) {
            TaskToolWrapper(
                onBackClick = { navController.popBackStack() },
                onPresetClick = { navController.navigate(EodRoute.TASK_PRESET) },
                onAlarmClick = { navController.navigate(EodRoute.TASK_ALARM) },
                onVesselClick = { vesselId ->
                    navController.navigate(EodRoute.taskVesselDetail(vesselId))
                },
                onAddVesselClick = { navController.navigate(EodRoute.TASK_VESSEL_ADD) },
                onDataManageClick = { navController.navigate(EodRoute.TASK_DATA_MANAGE) },
                onGuideClick = { navController.navigate(EodRoute.TASK_GUIDE) },
                onHomeClick = goHome,
                onKorClick = applyKor,
                onEngClick = applyEng,
                onContactClick = openContactMail,
                onExitClick = onExitApp
            )
        }

        composable(EodRoute.TASK_PRESET) {
            TaskPresetScreen(
                onBackClick = { navController.popBackStack() },
                onPresetAddClick = { navController.navigate(EodRoute.TASK_PRESET_ADD) },
                onPresetDetailClick = { presetId ->
                    navController.navigate(EodRoute.taskPresetDetail(presetId))
                },
                onHomeClick = goHome,
                onKorClick = applyKor,
                onEngClick = applyEng,
                onGuideClick = { navController.navigate(EodRoute.TASK_GUIDE) },
                onContactClick = openContactMail,
                onExitClick = onExitApp
            )
        }

        composable(EodRoute.TASK_PRESET_ADD) {
            TaskPresetAddScreen(
                onBackClick = { navController.popBackStack() },
                onPresetSaved = { navController.popBackStack() },
                onHomeClick = goHome,
                onKorClick = applyKor,
                onEngClick = applyEng,
                onGuideClick = { navController.navigate(EodRoute.TASK_GUIDE) },
                onContactClick = openContactMail,
                onExitClick = onExitApp
            )
        }

        composable(
            route = EodRoute.TASK_PRESET_DETAIL,
            arguments = listOf(
                navArgument("presetId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val presetId = backStackEntry.arguments?.getLong("presetId") ?: 0L

            TaskPresetDetailScreen(
                presetId = presetId,
                onBackClick = { navController.popBackStack() },
                onWorkAddClick = {
                    navController.navigate(EodRoute.taskPresetWorkAdd(presetId))
                },
                onWorkDetailClick = { clickedPresetId, workId ->
                    navController.navigate(EodRoute.taskPresetWorkDetail(clickedPresetId, workId))
                },
                onHomeClick = goHome,
                onKorClick = applyKor,
                onEngClick = applyEng,
                onGuideClick = { navController.navigate(EodRoute.TASK_GUIDE) },
                onContactClick = openContactMail,
                onExitClick = onExitApp
            )
        }

        composable(
            route = EodRoute.TASK_PRESET_WORK_ADD,
            arguments = listOf(
                navArgument("presetId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val presetId = backStackEntry.arguments?.getLong("presetId") ?: 0L

            TaskPresetWorkAddScreen(
                presetId = presetId,
                onBackClick = { navController.popBackStack() },
                onWorkSaved = { navController.popBackStack() },
                onHomeClick = goHome,
                onKorClick = applyKor,
                onEngClick = applyEng,
                onGuideClick = { navController.navigate(EodRoute.TASK_GUIDE) },
                onContactClick = openContactMail,
                onExitClick = onExitApp
            )
        }

        composable(
            route = EodRoute.TASK_PRESET_WORK_DETAIL,
            arguments = listOf(
                navArgument("presetId") { type = NavType.LongType },
                navArgument("workId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val presetId = backStackEntry.arguments?.getLong("presetId") ?: 0L
            val workId = backStackEntry.arguments?.getLong("workId") ?: 0L

            TaskPresetWorkDetailScreen(
                presetId = presetId,
                workId = workId,
                onBackClick = { navController.popBackStack() },
                onWorkSaved = { navController.popBackStack() },
                onWorkDeleted = { navController.popBackStack() },
                onHomeClick = goHome,
                onKorClick = applyKor,
                onEngClick = applyEng,
                onGuideClick = { navController.navigate(EodRoute.TASK_GUIDE) },
                onContactClick = openContactMail,
                onExitClick = onExitApp
            )
        }

        composable(EodRoute.TASK_ALARM) {
            TaskAlarmScreen(
                onBackClick = { navController.popBackStack() },
                onHomeClick = goHome,
                onKorClick = applyKor,
                onEngClick = applyEng,
                onGuideClick = { navController.navigate(EodRoute.TASK_GUIDE) },
                onContactClick = openContactMail,
                onExitClick = onExitApp
            )
        }

        composable(EodRoute.TASK_VESSEL_ADD) {
            TaskVesselAddScreen(
                onBackClick = { navController.popBackStack() },
                onGoPresetClick = { navController.navigate(EodRoute.TASK_PRESET) },
                onVesselSaved = { vesselId ->
                    navController.navigate(EodRoute.taskVesselDetail(vesselId)) {
                        popUpTo(EodRoute.TASK_VESSEL_ADD) { inclusive = true }
                    }
                }
            )
        }

        composable(EodRoute.TASK_DATA_MANAGE) {
            TaskDataManageScreen(
                onBackClick = { navController.popBackStack() },
                onHomeClick = goHome,
                onKorClick = applyKor,
                onEngClick = applyEng,
                onGuideClick = { navController.navigate(EodRoute.TASK_GUIDE) },
                onContactClick = openContactMail,
                onExitClick = onExitApp
            )
        }

        composable(EodRoute.TASK_GUIDE) {
            TaskGuideScreen(
                onBackClick = { navController.popBackStack() },
                onHomeClick = goHome,
                onKorClick = applyKor,
                onEngClick = applyEng,
                onContactClick = openContactMail,
                onExitClick = onExitApp
            )
        }

        composable(
            route = EodRoute.TASK_VESSEL_DETAIL,
            arguments = listOf(
                navArgument("vesselId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val vesselId = backStackEntry.arguments?.getLong("vesselId") ?: 0L

            TaskVesselDetailScreen(
                vesselId = vesselId,
                onBackClick = { navController.popBackStack() },
                onHomeClick = goHome,
                onKorClick = applyKor,
                onEngClick = applyEng,
                onGuideClick = { navController.navigate(EodRoute.TASK_GUIDE) },
                onContactClick = openContactMail,
                onExitClick = onExitApp
            )
        }

        composable(EodRoute.PORTINFO_TOP) {
            PortInfoToolWrapper(
                onBackClick = { navController.popBackStack() },
                onRecordClick = { recordId ->
                    navController.navigate(EodRoute.portInfoRecordDetail(recordId))
                },
                onAddRecordClick = {
                    navController.navigate(EodRoute.portInfoRecordEditor(0L))
                }
            )
        }

        composable(
            route = EodRoute.PORTINFO_RECORD_DETAIL,
            arguments = listOf(
                navArgument("recordId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getLong("recordId") ?: 0L

            PortInfoRecordDetailScreen(
                recordId = recordId,
                onBackClick = { navController.popBackStack() },
                onAugmentClick = {
                    navController.navigate(EodRoute.portInfoRecordEditor(recordId))
                }
            )
        }

        composable(
            route = EodRoute.PORTINFO_RECORD_EDITOR,
            arguments = listOf(
                navArgument("recordId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getLong("recordId") ?: 0L

            PortInfoRecordEditorScreen(
                recordId = recordId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(EodRoute.CARGOINFO_TOP) {
            CargoInfoTopPlaceholderScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }

    LaunchedEffect(alarmTarget) {
        val target = alarmTarget ?: return@LaunchedEffect
        navController.navigate(EodRoute.taskVesselDetail(target.vesselId)) {
            launchSingleTop = true
        }
    }
}
