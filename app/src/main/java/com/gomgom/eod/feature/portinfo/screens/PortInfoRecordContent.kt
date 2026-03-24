package com.gomgom.eod.feature.portinfo.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import com.gomgom.eod.R
import com.gomgom.eod.feature.portinfo.porttool.entity.PortAttachmentEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortLocationEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortOperationEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordBundle
import com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortToolType
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortEditorFocusField
import com.gomgom.eod.feature.portinfo.porttool.viewmodel.PortSearchSourceField
import java.util.Calendar

@Composable
fun PortInfoRecordContent(
    editable: Boolean,
    searchCardEditable: Boolean,
    record: PortRecordEntity,
    operations: List<PortOperationEntity>,
    locations: List<PortLocationEntity>,
    attachments: List<PortAttachmentEntity>,
    isVesselReportingExpanded: Boolean,
    isAnchorageExpanded: Boolean,
    isBerthExpanded: Boolean,
    onToggleVesselReporting: () -> Unit,
    onToggleAnchorage: () -> Unit,
    onToggleBerth: () -> Unit,
    onBundleChange: (PortRecordBundle) -> Unit,
    onEditorFieldFocusChange: (PortEditorFocusField, Boolean) -> Unit,
    onFieldSearchInputChange: (PortSearchSourceField, String) -> Unit,
    onFormFieldSearchInputChange: (String, String) -> Unit,
    onAddAttachmentClick: () -> Unit,
    onDeleteAttachmentClick: (Long) -> Unit
) {
    val standardAnsi = stringResource(R.string.port_info_standard_ansi)
    val standardJis = stringResource(R.string.port_info_standard_jis)
    val standardDin = stringResource(R.string.port_info_standard_din)
    val standardCustom = stringResource(R.string.port_info_standard_custom)
    val channelVhf = stringResource(R.string.port_info_channel_vhf)
    val channelMfhf = stringResource(R.string.port_info_channel_mfhf)
    val channelCustom = stringResource(R.string.port_info_channel_custom)
    val channelTel = stringResource(R.string.port_info_channel_tel)
    val statusAllowed = stringResource(R.string.port_info_status_allowed)
    val statusDenied = stringResource(R.string.port_info_status_denied)
    val statusConditional = stringResource(R.string.port_info_status_conditional)
    val sidePort = stringResource(R.string.port_info_side_port)
    val sideStbd = stringResource(R.string.port_info_side_stbd)
    val sideBowIn = stringResource(R.string.port_info_side_bow_in)
    val sideSternTo = stringResource(R.string.port_info_side_stern_to)
    val connectionHose = stringResource(R.string.port_info_connection_hose)
    val connectionLoadingArm = stringResource(R.string.port_info_connection_loading_arm)

    val channelOptions = remember(channelVhf, channelMfhf, channelCustom) {
        listOf(channelVhf, channelMfhf, channelCustom)
    }
    val agentChannelOptions = remember(channelTel, channelVhf, channelMfhf, channelCustom) {
        listOf(channelTel, channelVhf, channelMfhf, channelCustom)
    }
    val extraStatusOptions = remember(statusAllowed, statusDenied, statusConditional) {
        listOf(statusAllowed, statusDenied, statusConditional)
    }
    val berthingSideOptions = remember(sidePort, sideStbd, sideBowIn, sideSternTo) {
        listOf(sidePort, sideStbd, sideBowIn, sideSternTo)
    }
    val manifoldConnectionOptions = remember(connectionHose, connectionLoadingArm) {
        listOf(connectionHose, connectionLoadingArm)
    }
    val manifoldStandardOptions = remember(standardAnsi, standardJis, standardDin, standardCustom) {
        listOf(standardAnsi, standardJis, standardDin, standardCustom)
    }
    val transferRateUnitOptions = remember { listOf("m3/h", "MT/h", "bbl/h") }

    val bundle = remember(record, operations, locations, attachments) {
        com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordBundle(
            record = record,
            operations = operations,
            locations = locations,
            attachments = attachments
        )
    }
    val vesselOps = remember(operations) { operations.associateBy { it.operationType } }
    val anchorage = remember(locations) { locations.firstOrNull { it.locationType == PortToolType.ANCHORAGE } }
    val berth = remember(locations) { locations.firstOrNull { it.locationType == PortToolType.BERTH } }

    val manifoldUnitOptions = remember(record.manifoldStandard) {
        when (record.manifoldStandard) {
            "ANSI" -> listOf("A")
            "JIS" -> listOf("mm")
            "DIN" -> listOf("DN")
            else -> emptyList()
        }
    }
    val manifoldClassOptions = remember(record.manifoldStandard) {
        when (record.manifoldStandard) {
            "ANSI" -> listOf("150#", "300#", "400#", "600#")
            "JIS" -> listOf("5K", "10K", "16K", "20K", "30K")
            "DIN" -> listOf("PN6", "PN10", "PN16", "PN25", "PN40", "PN63")
            else -> emptyList()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        VesselInfoBlock(
            editable = editable,
            searchCardEditable = searchCardEditable,
            record = record,
            onBundleChange = onBundleChange,
            onEditorFieldFocusChange = onEditorFieldFocusChange,
            onFieldSearchInputChange = onFieldSearchInputChange,
            bundle = bundle
        )
        VesselReportingBlock(
            editable = editable,
            record = record,
            vesselOps = vesselOps,
            channelOptions = channelOptions,
            agentChannelOptions = agentChannelOptions,
            isExpanded = isVesselReportingExpanded,
            onToggle = onToggleVesselReporting,
            onBundleChange = onBundleChange,
            bundle = bundle
        )
        AnchorageBlock(
            editable = editable,
            record = record,
            anchorage = anchorage,
            extraStatusOptions = extraStatusOptions,
            isExpanded = isAnchorageExpanded,
            onToggle = onToggleAnchorage,
            onBundleChange = onBundleChange,
            onFormFieldSearchInputChange = onFormFieldSearchInputChange,
            bundle = bundle
        )
        BerthBlock(
            editable = editable,
            record = record,
            berth = berth,
            berthingSideOptions = berthingSideOptions,
            extraStatusOptions = extraStatusOptions,
            isExpanded = isBerthExpanded,
            onToggle = onToggleBerth,
            onBundleChange = onBundleChange,
            onFormFieldSearchInputChange = onFormFieldSearchInputChange,
            bundle = bundle
        )
        CargoBlock(
            editable = editable,
            record = record,
            manifoldConnectionOptions = manifoldConnectionOptions,
            manifoldStandardOptions = manifoldStandardOptions,
            manifoldUnitOptions = manifoldUnitOptions,
            manifoldClassOptions = manifoldClassOptions,
            standardAnsi = standardAnsi,
            standardJis = standardJis,
            standardDin = standardDin,
            transferRateUnitOptions = transferRateUnitOptions,
            onBundleChange = onBundleChange,
            bundle = bundle
        )
        AttachmentSection(
            attachments = attachments,
            editable = editable,
            onAddAttachmentClick = onAddAttachmentClick,
            onDeleteAttachmentClick = onDeleteAttachmentClick
        )
    }
}

@Composable
private fun VesselInfoBlock(
    editable: Boolean,
    searchCardEditable: Boolean,
    record: PortRecordEntity,
    bundle: com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordBundle,
    onBundleChange: (com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordBundle) -> Unit,
    onEditorFieldFocusChange: (PortEditorFocusField, Boolean) -> Unit,
    onFieldSearchInputChange: (PortSearchSourceField, String) -> Unit
) {
    val searchEditable = searchCardEditable
    PortCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BasicRecordTextField(
                label = stringResource(R.string.port_info_field_country),
                value = record.countryName,
                editable = searchEditable,
                modifier = Modifier.weight(1.15f),
                fieldHeight = 40.dp,
                labelBold = true,
                onFocusChange = { onEditorFieldFocusChange(PortEditorFocusField.COUNTRY, it) }
            ) {
                onBundleChange(bundle.copy(record = record.copy(countryName = it)))
                onFieldSearchInputChange(PortSearchSourceField.COUNTRY, it)
            }
            RecordTextField(
                label = stringResource(R.string.port_info_field_unlocode),
                value = record.unlocode,
                editable = searchEditable,
                modifier = Modifier.width(92.dp),
                fieldHeight = 40.dp,
                labelBold = true,
                onFocusChange = { onEditorFieldFocusChange(PortEditorFocusField.UNLOCODE, it) },
                onFocusLostValue = ::formatDisplayedUnlocode
            ) {
                val upper = it.uppercase()
                onBundleChange(bundle.copy(record = record.copy(unlocode = upper)))
                onFieldSearchInputChange(PortSearchSourceField.CODE, upper)
            }
        }
        RecordTextField(
            label = stringResource(R.string.port_info_field_port),
            value = record.portName,
            editable = searchEditable,
            modifier = Modifier.fillMaxWidth(),
            fieldHeight = 40.dp,
            labelBold = true,
            onFocusChange = { onEditorFieldFocusChange(PortEditorFocusField.PORT, it) }
        ) {
            onBundleChange(bundle.copy(record = record.copy(portName = it)))
            onFieldSearchInputChange(PortSearchSourceField.PORT, it)
        }
    }
}

@Composable
private fun VesselReportingBlock(
    editable: Boolean,
    record: PortRecordEntity,
    vesselOps: Map<String, PortOperationEntity>,
    channelOptions: List<String>,
    agentChannelOptions: List<String>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    bundle: com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordBundle,
    onBundleChange: (com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordBundle) -> Unit
) {
    AccordionCard(stringResource(R.string.port_info_section_vessel_reporting), isExpanded, onToggle) {
        listOf(PortToolType.VTS, PortToolType.PILOT, PortToolType.TUG, PortToolType.CIQ, PortToolType.AGENT).forEach { type ->
            val operation = vesselOps[type] ?: PortOperationEntity(
                id = record.id * 100 + type.hashCode().toLong(),
                recordId = record.id,
                operationType = type,
                channelGroup = if (type == PortToolType.AGENT) stringResource(R.string.port_info_channel_tel) else "",
                createdAt = record.createdAt,
                updatedAt = record.updatedAt
            )
            OperationBlock(
                title = if (type == PortToolType.AGENT) stringResource(R.string.port_info_field_agent) else type,
                operation = operation,
                editable = editable,
                channelOptions = if (type == PortToolType.AGENT) agentChannelOptions else channelOptions,
                maxChannelLength = if (type == PortToolType.AGENT) 12 else 7
            ) { updated ->
                onBundleChange(bundle.copy(operations = bundle.operations.filterNot { it.operationType == type } + updated))
            }
        }
    }
}

@Composable
private fun AnchorageBlock(
    editable: Boolean,
    record: PortRecordEntity,
    anchorage: PortLocationEntity?,
    extraStatusOptions: List<String>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onFormFieldSearchInputChange: (String, String) -> Unit,
    bundle: com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordBundle,
    onBundleChange: (com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordBundle) -> Unit
) {
    AccordionCard(stringResource(R.string.port_info_section_anchorage), isExpanded, onToggle) {
        val current = anchorage ?: defaultLocation(record.id, PortToolType.ANCHORAGE, record.createdAt, record.updatedAt)
        RecordTextField(stringResource(R.string.port_info_field_anchorage_name), record.anchorageName, editable, expandable = true, labelBold = true) {
            onBundleChange(bundle.copy(record = record.copy(anchorageName = it), locations = bundle.locations.filterNot { location -> location.locationType == PortToolType.ANCHORAGE } + current.copy(name = it)))
        }
        RecordTextField(stringResource(R.string.port_info_field_info), record.anchorageInfo, editable, minLines = 2, expandable = true, labelBold = true) {
            onBundleChange(bundle.copy(record = record.copy(anchorageInfo = it), locations = bundle.locations.filterNot { location -> location.locationType == PortToolType.ANCHORAGE } + current.copy(info = it)))
        }
        Text(
            text = stringResource(R.string.port_info_field_supply_group),
            fontSize = 15.sp,
            color = Color(0xFF5D7598),
            fontWeight = FontWeight.Bold
        )
        ExtraWorkBlock(editable, bundle, extraStatusOptions, onBundleChange, onFormFieldSearchInputChange)
    }
}

@Composable
private fun BerthBlock(
    editable: Boolean,
    record: PortRecordEntity,
    berth: PortLocationEntity?,
    berthingSideOptions: List<String>,
    extraStatusOptions: List<String>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onFormFieldSearchInputChange: (String, String) -> Unit,
    bundle: com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordBundle,
    onBundleChange: (com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordBundle) -> Unit
) {
    AccordionCard(stringResource(R.string.port_info_section_berth), isExpanded, onToggle) {
        val current = berth ?: defaultLocation(record.id, PortToolType.BERTH, record.createdAt, record.updatedAt)
        RecordTextField(stringResource(R.string.port_info_field_berth_name), record.berthName, editable, expandable = true, labelBold = true) {
            onBundleChange(bundle.copy(record = record.copy(berthName = it), locations = bundle.locations.filterNot { location -> location.locationType == PortToolType.BERTH } + current.copy(name = it)))
        }
        RecordTextField(stringResource(R.string.port_info_field_company), record.company, editable, expandable = true, labelBold = true) {
            onBundleChange(bundle.copy(record = record.copy(company = it), locations = bundle.locations.filterNot { location -> location.locationType == PortToolType.BERTH } + current.copy(company = it)))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RecordTextField(stringResource(R.string.port_info_field_berth_length), current.berthLengthMeters, editable, Modifier.weight(1f), fieldHeight = 40.dp, expandable = true, labelBold = true) {
                onBundleChange(bundle.copy(locations = bundle.locations.filterNot { it.locationType == PortToolType.BERTH } + current.copy(berthLengthMeters = it)))
            }
            RecordTextField(stringResource(R.string.port_info_field_depth), current.depthMeters, editable, Modifier.weight(1f), fieldHeight = 40.dp, expandable = true, labelBold = true) {
                onBundleChange(bundle.copy(locations = bundle.locations.filterNot { it.locationType == PortToolType.BERTH } + current.copy(depthMeters = it)))
            }
            DropdownField(
                stringResource(R.string.port_info_field_berthing_side),
                current.berthingSide,
                editable,
                berthingSideOptions,
                modifier = Modifier.weight(1f),
                fieldHeight = 40.dp,
                labelBold = true
            ) { selected ->
                onBundleChange(bundle.copy(locations = bundle.locations.filterNot { it.locationType == PortToolType.BERTH } + current.copy(berthingSide = selected)))
            }
        }
        Text(
            text = stringResource(R.string.port_info_field_mooring_line),
            fontSize = 15.sp,
            color = Color(0xFF5D7598),
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RecordTextField("", current.mooringFore, editable, Modifier.weight(1f), expandable = true, prefixText = stringResource(R.string.port_info_field_mooring_fore)) {
                onBundleChange(bundle.copy(locations = bundle.locations.filterNot { it.locationType == PortToolType.BERTH } + current.copy(mooringFore = it.filter(Char::isDigit))))
            }
            RecordTextField("", current.mooringAft, editable, Modifier.weight(1f), expandable = true, prefixText = stringResource(R.string.port_info_field_mooring_aft)) {
                onBundleChange(bundle.copy(locations = bundle.locations.filterNot { it.locationType == PortToolType.BERTH } + current.copy(mooringAft = it.filter(Char::isDigit))))
            }
        }
        RecordTextField(stringResource(R.string.port_info_field_info), record.berthInfo, editable, minLines = 2, expandable = true, labelBold = true) {
            onBundleChange(bundle.copy(record = record.copy(berthInfo = it), locations = bundle.locations.filterNot { location -> location.locationType == PortToolType.BERTH } + current.copy(info = it)))
        }
        Text(
            text = stringResource(R.string.port_info_field_supply_group),
            fontSize = 15.sp,
            color = Color(0xFF5D7598),
            fontWeight = FontWeight.Bold
        )
        ExtraWorkBlock(editable, bundle, extraStatusOptions, onBundleChange, onFormFieldSearchInputChange)
    }
}

@Composable
private fun CargoBlock(
    editable: Boolean,
    record: PortRecordEntity,
    manifoldConnectionOptions: List<String>,
    manifoldStandardOptions: List<String>,
    manifoldUnitOptions: List<String>,
    manifoldClassOptions: List<String>,
    standardAnsi: String,
    standardJis: String,
    standardDin: String,
    transferRateUnitOptions: List<String>,
    bundle: com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordBundle,
    onBundleChange: (com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordBundle) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    AccordionCard(stringResource(R.string.port_info_section_cargo_discharge), isExpanded, { isExpanded = !isExpanded }) {
        RecordTextField(stringResource(R.string.port_info_field_cargo), record.cargoName, editable, expandable = true, labelBold = true) {
            onBundleChange(bundle.copy(record = record.copy(cargoName = it)))
        }
        Text(
            text = stringResource(R.string.port_info_field_manifold),
            fontSize = 15.sp,
            color = Color(0xFF5D7598),
            fontWeight = FontWeight.Bold
        )
        ManifoldInlineField(
            editable = editable,
            connectionValue = record.manifoldConnectionType,
            standardValue = record.manifoldStandard,
            sizeValue = record.manifoldSize,
            unitValue = record.manifoldUnit,
            classValue = record.manifoldClass,
            connectionOptions = manifoldConnectionOptions,
            standardOptions = manifoldStandardOptions,
            unitOptions = manifoldUnitOptions,
            classOptions = manifoldClassOptions,
            onConnectionChange = {
                onBundleChange(bundle.copy(record = record.copy(manifoldConnectionType = it)))
            },
            onStandardChange = { selected ->
                val unit = when (selected) {
                    standardAnsi -> "A"
                    standardJis -> "mm"
                    standardDin -> "DN"
                    else -> ""
                }
                val clazz = when (selected) {
                    standardAnsi -> "150#"
                    standardJis -> "5K"
                    standardDin -> "PN6"
                    else -> ""
                }
                onBundleChange(bundle.copy(record = record.copy(manifoldStandard = selected, manifoldUnit = unit, manifoldClass = clazz)))
            },
            onSizeChange = {
                onBundleChange(bundle.copy(record = record.copy(manifoldSize = it)))
            },
            onUnitChange = {
                onBundleChange(bundle.copy(record = record.copy(manifoldUnit = it)))
            },
            onClassChange = {
                onBundleChange(bundle.copy(record = record.copy(manifoldClass = it)))
            }
        )
        RecordTextField("", record.cargoRemark, editable, expandable = true) {
            onBundleChange(bundle.copy(record = record.copy(cargoRemark = it)))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.port_info_field_transfer_rate),
                fontSize = 15.sp,
                color = Color(0xFF5D7598),
                fontWeight = FontWeight.Bold
            )
            CompactNumericBox(
                value = record.transferRate,
                editable = editable,
                width = 82.dp,
                bracketed = false,
                onValueChange = { onBundleChange(bundle.copy(record = record.copy(transferRate = it.filter { ch -> ch.isDigit() || ch == '.' }))) }
            )
            InlineDropdownField(
                value = record.transferRateUnit,
                editable = editable,
                options = transferRateUnitOptions,
                modifier = Modifier.weight(1f)
            ) { selected ->
                onBundleChange(bundle.copy(record = record.copy(transferRateUnit = selected)))
            }
        }
        RecordTextField(stringResource(R.string.port_info_field_discharge_info), record.dischargeInfo, editable, minLines = 2, expandable = true, labelBold = true) {
            onBundleChange(bundle.copy(record = record.copy(dischargeInfo = it)))
        }
        RecordTextField(stringResource(R.string.port_info_field_operator), record.operatorName, editable, expandable = true, labelBold = true) {
            onBundleChange(bundle.copy(record = record.copy(operatorName = it)))
        }
        RecordTextField(stringResource(R.string.port_info_field_safety_officer), record.safetyOfficerName, editable, expandable = true, labelBold = true) {
            onBundleChange(bundle.copy(record = record.copy(safetyOfficerName = it)))
        }
        RecordTextField(stringResource(R.string.port_info_field_surveyor), record.surveyorName, editable, expandable = true, labelBold = true) {
            onBundleChange(bundle.copy(record = record.copy(surveyorName = it)))
        }
        RecordTextField(stringResource(R.string.port_info_field_caution), record.caution, editable, minLines = 2, expandable = true, labelBold = true) {
            onBundleChange(bundle.copy(record = record.copy(caution = it)))
        }
    }
}

@Composable
private fun ExtraWorkBlock(
    editable: Boolean,
    bundle: PortRecordBundle,
    options: List<String>,
    onChange: (PortRecordBundle) -> Unit,
    onFormFieldSearchInputChange: (String, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InlineStatusPairRow(
            leftLabel = stringResource(R.string.port_info_field_bunkering),
            leftValue = bundle.record.supplyStatus,
            rightLabel = stringResource(R.string.port_info_field_fresh_water),
            rightValue = bundle.record.freshWaterStatus,
            editable = editable,
            options = options,
            onLeftChange = {
                onChange(bundle.copy(record = bundle.record.copy(supplyStatus = it)))
                onFormFieldSearchInputChange("supplyStatus", it)
            },
            onRightChange = {
                onChange(bundle.copy(record = bundle.record.copy(freshWaterStatus = it)))
                onFormFieldSearchInputChange("freshWaterStatus", it)
            }
        )
        InlineStatusPairRow(
            leftLabel = stringResource(R.string.port_info_field_store_spare),
            leftValue = bundle.record.storeSpareStatus,
            rightLabel = stringResource(R.string.port_info_field_provisions),
            rightValue = bundle.record.provisionsStatus,
            editable = editable,
            options = options,
            onLeftChange = {
                onChange(bundle.copy(record = bundle.record.copy(storeSpareStatus = it)))
                onFormFieldSearchInputChange("storeSpareStatus", it)
            },
            onRightChange = {
                onChange(bundle.copy(record = bundle.record.copy(provisionsStatus = it)))
                onFormFieldSearchInputChange("provisionsStatus", it)
            }
        )
        RecordTextField("", bundle.record.supplyRemark, editable, expandable = true) {
            onChange(bundle.copy(record = bundle.record.copy(supplyRemark = it)))
        }
        InlineStatusPairRow(
            leftLabel = stringResource(R.string.port_info_field_garbage),
            leftValue = bundle.record.wasteStatus,
            rightLabel = stringResource(R.string.port_info_field_slop),
            rightValue = bundle.record.slopStatus,
            editable = editable,
            options = options,
            onLeftChange = {
                onChange(bundle.copy(record = bundle.record.copy(wasteStatus = it)))
                onFormFieldSearchInputChange("wasteStatus", it)
            },
            onRightChange = {
                onChange(bundle.copy(record = bundle.record.copy(slopStatus = it)))
                onFormFieldSearchInputChange("slopStatus", it)
            }
        )
        RecordTextField("", bundle.record.wasteRemark, editable, expandable = true) {
            onChange(bundle.copy(record = bundle.record.copy(wasteRemark = it)))
        }
        InlineStatusPairRow(
            leftLabel = stringResource(R.string.port_info_field_crew_change),
            leftValue = bundle.record.crewChangeStatus,
            rightLabel = stringResource(R.string.port_info_field_external_audit),
            rightValue = bundle.record.externalAuditStatus,
            editable = editable,
            options = options,
            onLeftChange = {
                onChange(bundle.copy(record = bundle.record.copy(crewChangeStatus = it)))
                onFormFieldSearchInputChange("crewChangeStatus", it)
            },
            onRightChange = {
                onChange(bundle.copy(record = bundle.record.copy(externalAuditStatus = it)))
                onFormFieldSearchInputChange("externalAuditStatus", it)
            }
        )
        RecordTextField("", bundle.record.crewChangeRemark, editable, expandable = true) {
            onChange(bundle.copy(record = bundle.record.copy(crewChangeRemark = it)))
        }
    }
}

@Composable
private fun InlineStatusPairRow(
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String,
    editable: Boolean,
    options: List<String>,
    onLeftChange: (String) -> Unit,
    onRightChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InlineStatusItem(
            label = leftLabel,
            value = leftValue,
            editable = editable,
            options = options,
            isLeftColumn = true,
            modifier = Modifier.weight(1f),
            onChange = onLeftChange
        )
        InlineStatusItem(
            label = rightLabel,
            value = rightValue,
            editable = editable,
            options = options,
            isLeftColumn = false,
            modifier = Modifier.weight(1f),
            onChange = onRightChange
        )
    }
}

@Composable
private fun InlineStatusItem(
    label: String,
    value: String,
    editable: Boolean,
    options: List<String>,
    isLeftColumn: Boolean,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit
) {
    Row(
        modifier = modifier.then(if (isLeftColumn) Modifier.offset(x = 30.dp) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF202B3B),
            fontSize = 15.sp,
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(12.dp))
        InlineDropdownField(
            value = value,
            editable = editable,
            options = options,
            modifier = Modifier.weight(1f),
            onSelect = onChange
        )
    }
}

@Composable
internal fun PortCard(title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            if (!title.isNullOrBlank()) {
                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF123A73),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.5.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
            }
        }
    }
}

@Composable
private fun AccordionCard(title: String, expanded: Boolean, onToggle: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    PortCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(32.dp))
            Text(
                text = title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF123A73),
                modifier = Modifier
                    .weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF3F5F93)
                )
            }
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(0.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
        }
    }
}

@Composable
private fun OperationBlock(
    title: String,
    operation: PortOperationEntity,
    editable: Boolean,
    channelOptions: List<String>,
    maxChannelLength: Int,
    onChange: (PortOperationEntity) -> Unit
) {
    val customOption = stringResource(R.string.port_info_channel_custom)
    val context = LocalContext.current
    val arrivalDateLabel = stringResource(R.string.port_info_field_arrival_date)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF123A73),
                fontSize = 18.sp,
                modifier = Modifier.width(92.dp)
            )
            Box(
                modifier = Modifier.width(132.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                InlineDropdownField(
                    value = operation.channelGroup.ifBlank { channelOptions.firstOrNull().orEmpty() },
                    editable = editable,
                    options = channelOptions,
                    customInputOption = customOption,
                    customMaxLength = 7
                ) {
                    onChange(operation.copy(channelGroup = it))
                }
            }
            CompactChannelField(
                value = operation.channelValue,
                editable = editable,
                modifier = Modifier.padding(end = 40.dp),
                maxLength = maxChannelLength
            ) {
                onChange(operation.copy(channelValue = it.filter { ch -> ch.isDigit() }))
            }
        }
        RecordTextField("", operation.remark, editable, minLines = 1, fieldHeight = 40.dp, expandable = true) {
            onChange(operation.copy(remark = it))
        }
        if (operation.operationType == PortToolType.AGENT) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = arrivalDateLabel,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF123A73),
                    fontSize = 18.sp,
                    maxLines = 1,
                    modifier = Modifier.width(140.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = editable) {
                            showArrivalDatePicker(
                                context = context,
                                currentValue = operation.arrivalDate
                            ) { selected ->
                                onChange(operation.copy(arrivalDate = selected))
                            }
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = operation.arrivalDate.ifBlank { "YYYY-MM-DD" },
                        color = if (operation.arrivalDate.isBlank()) Color(0xFF8A9AB3) else Color(0xFF123A73),
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun showArrivalDatePicker(
    context: android.content.Context,
    currentValue: String,
    onSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    parseArrivalDate(currentValue)?.let { (year, month, day) ->
        calendar.set(year, month - 1, day)
    }
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onSelected(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun parseArrivalDate(value: String): Triple<Int, Int, Int>? {
    val parts = value.split("-")
    if (parts.size != 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    if (month !in 1..12 || day !in 1..31) return null
    return Triple(year, month, day)
}

@Composable
private fun InlineDropdownField(
    value: String,
    editable: Boolean,
    options: List<String>,
    modifier: Modifier = Modifier,
    customInputOption: String? = null,
    customMaxLength: Int = 7,
    onSelect: (String) -> Unit
) {
    val isCustomEntry = customInputOption != null &&
        (value.isBlank() || value !in options)

    if (!editable) {
        Text(
            text = value,
            modifier = modifier,
            color = Color(0xFF123A73),
            fontSize = 16.sp,
            lineHeight = 20.sp,
            textDecoration = TextDecoration.Underline
        )
        return
    }
    var expanded by remember(value, options) { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (isCustomEntry) {
                BasicTextField(
                    value = value,
                    onValueChange = { onSelect(it.take(customMaxLength)) },
                    enabled = editable,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color(0xFF123A73),
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                    cursorBrush = SolidColor(Color(0xFF123A73)),
                    modifier = Modifier.widthIn(min = 36.dp)
                )
            } else {
                Text(
                    text = value,
                    modifier = Modifier.clickable { expanded = true },
                    color = Color(0xFF123A73),
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    textDecoration = TextDecoration.Underline
                )
            }
            Icon(
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = null,
                tint = Color(0xFF123A73),
                modifier = Modifier
                    .size(14.dp)
                    .clickable { expanded = true }
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(12.dp),
            containerColor = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    modifier = Modifier.height(28.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    text = {
                        Text(
                            text = option,
                            fontSize = 15.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF111111)
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelect(
                            if (customInputOption != null && option == customInputOption) ""
                            else option
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun CompactChannelField(
    value: String,
    editable: Boolean,
    modifier: Modifier = Modifier,
    maxLength: Int = 7,
    onValueChange: (String) -> Unit
) {
    val resolvedWidth = when {
        maxLength <= 7 -> 62.dp
        value.length <= 5 -> 62.dp
        else -> (62 + ((value.length - 5) * 12)).dp
    }
    CompactNumericField(
        label = null,
        value = value,
        editable = editable,
        modifier = modifier,
        width = resolvedWidth,
        bracketed = false,
        onValueChange = { onValueChange(it.take(maxLength)) }
    )
}

@Composable
private fun CompactNumericField(
    label: String?,
    value: String,
    editable: Boolean,
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = 62.dp,
    bracketed: Boolean = false,
    labelBold: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (!label.isNullOrBlank()) {
            Text(label, fontSize = 15.sp, color = Color(0xFF5D7598), fontWeight = if (labelBold) FontWeight.Bold else FontWeight.Normal)
        }
        CompactNumericBox(
            value = value,
            editable = editable,
            width = width,
            bracketed = bracketed,
            onValueChange = onValueChange
        )
    }
}

@Composable
private fun ManifoldInlineField(
    editable: Boolean,
    connectionValue: String,
    standardValue: String,
    sizeValue: String,
    unitValue: String,
    classValue: String,
    connectionOptions: List<String>,
    standardOptions: List<String>,
    unitOptions: List<String>,
    classOptions: List<String>,
    onConnectionChange: (String) -> Unit,
    onStandardChange: (String) -> Unit,
    onSizeChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onClassChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        InlineDropdownField(
            value = connectionValue,
            editable = editable,
            options = connectionOptions
        ) { onConnectionChange(it) }
        InlineDropdownField(
            value = standardValue,
            editable = editable,
            options = standardOptions
        ) { onStandardChange(it) }
        CompactNumericBox(
            value = sizeValue,
            editable = editable,
            width = 60.dp,
            bracketed = false,
            onValueChange = onSizeChange
        )
        if (unitOptions.isEmpty()) {
            InlinePlainField(
                value = unitValue,
                editable = editable,
                width = 38.dp,
                onValueChange = onUnitChange
            )
        } else {
            InlineDropdownField(
                value = unitValue,
                editable = editable,
                options = unitOptions
            ) { onUnitChange(it) }
        }
        if (classOptions.isEmpty()) {
            InlinePlainField(
                value = classValue,
                editable = editable,
                width = 54.dp,
                onValueChange = onClassChange
            )
        } else {
            InlineDropdownField(
                value = classValue,
                editable = editable,
                options = classOptions,
                modifier = Modifier.weight(1f)
            ) { onClassChange(it) }
        }
    }
}

@Composable
private fun CompactNumericBox(
    value: String,
    editable: Boolean,
    width: androidx.compose.ui.unit.Dp,
    bracketed: Boolean,
    onValueChange: (String) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (bracketed) {
            Text(text = "[", color = Color(0xFF123A73), fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
        if (editable) {
            Box(
                modifier = Modifier
                    .size(width = width, height = 34.dp)
                    .border(
                        width = 1.dp,
                        color = if (value.isBlank()) Color(0xFFC9D4E3) else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFF123A73),
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.Center) {
                            innerTextField()
                        }
                    }
                )
            }
        } else {
            Text(
                text = value.ifBlank { "-" },
                color = Color(0xFF123A73),
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        if (bracketed) {
            Text(text = "]", color = Color(0xFF123A73), fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun InlinePlainField(
    value: String,
    editable: Boolean,
    width: androidx.compose.ui.unit.Dp,
    onValueChange: (String) -> Unit
) {
    if (!editable) {
        Text(
            text = value,
            color = Color(0xFF123A73),
            fontSize = 16.sp,
            lineHeight = 20.sp
        )
        return
    }
    Box(
        modifier = Modifier
            .width(width)
            .height(34.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = Color(0xFF123A73),
                fontSize = 16.sp,
                lineHeight = 20.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            cursorBrush = SolidColor(Color(0xFF123A73)),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DropdownField(label: String, value: String, editable: Boolean, options: List<String>, modifier: Modifier = Modifier.fillMaxWidth(), fieldHeight: androidx.compose.ui.unit.Dp = 56.dp, labelBold: Boolean = false, onSelect: (String) -> Unit) {
    if (!editable) {
        ReadOnlyField(label, value, modifier, fieldHeight, labelBold)
        return
    }
    var expanded by remember(value, options) { mutableStateOf(false) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 15.sp, color = Color(0xFF5D7598), fontWeight = if (labelBold) FontWeight.Bold else FontWeight.Normal)
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fieldHeight)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFC9D4E3), RoundedCornerShape(16.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF202B3B),
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    tint = Color(0xFF5D7598),
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White.copy(alpha = 0.6f),
                tonalElevation = 0.dp,
                shadowElevation = 8.dp
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        modifier = Modifier.height(28.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        text = { Text(option, fontSize = 15.sp, lineHeight = 18.sp) },
                        onClick = {
                            expanded = false
                            onSelect(option)
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun BasicRecordTextField(
    label: String,
    value: String,
    editable: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth(),
    minLines: Int = 1,
    fieldHeight: androidx.compose.ui.unit.Dp? = null,
    expandable: Boolean = false,
    prefixText: String? = null,
    labelBold: Boolean = false,
    onFocusChange: (Boolean) -> Unit = {},
    onFocusLostValue: ((String) -> String)? = null,
    onValueChange: (String) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                fontSize = 15.sp,
                color = Color(0xFF5D7598),
                fontWeight = if (labelBold) FontWeight.Bold else FontWeight.Normal
            )
        }
        val focusRequester = remember { FocusRequester() }
        var isFocused by remember { mutableStateOf(false) }
        val effectiveHeight = fieldHeight ?: if (minLines <= 1) 40.dp else (24.dp * minLines + 24.dp)
        val sizeModifier = if (expandable) {
            Modifier
                .fillMaxWidth()
                .heightIn(min = effectiveHeight)
        } else {
            Modifier
                .fillMaxWidth()
                .height(effectiveHeight)
        }
        val boxModifier = sizeModifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = if (isFocused) Color(0xFF6E8DC0) else Color(0xFFC9D4E3),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp)
        if (editable) {
            Box(
                modifier = boxModifier.clickable { focusRequester.requestFocus() },
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = minLines == 1 && !expandable,
                    minLines = if (expandable) minLines else 1,
                    maxLines = if (expandable) Int.MAX_VALUE else 1,
                    textStyle = TextStyle(
                        color = Color(0xFF202B3B),
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    keyboardOptions = KeyboardOptions.Default,
                    cursorBrush = SolidColor(Color(0xFF123A73)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            isFocused = it.isFocused
                            if (!it.isFocused && onFocusLostValue != null) {
                                val adjusted = onFocusLostValue(value)
                                if (adjusted != value) onValueChange(adjusted)
                            }
                            onFocusChange(it.isFocused)
                        },
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = if (minLines == 1 && !expandable) 4.dp else 8.dp),
                            verticalAlignment = if (minLines == 1 && !expandable) Alignment.CenterVertically else Alignment.Top
                        ) {
                            if (!prefixText.isNullOrBlank()) {
                                Text(
                                    text = prefixText,
                                    modifier = Modifier.requiredWidthIn(min = 28.dp),
                                    color = Color(0xFF5D7598),
                                    fontSize = 15.sp,
                                    lineHeight = 20.sp,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            innerTextField()
                        }
                    }
                )
            }
        } else {
            Row(
                modifier = boxModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!prefixText.isNullOrBlank()) {
                    Text(
                        text = prefixText,
                        modifier = Modifier.requiredWidthIn(min = 28.dp),
                        color = Color(0xFF5D7598),
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = value.ifBlank { "-" },
                    color = Color(0xFF202B3B),
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    maxLines = if (expandable) Int.MAX_VALUE else if (minLines == 1) 1 else minLines
                )
            }
        }
    }
}

@Composable
internal fun RecordTextField(label: String, value: String, editable: Boolean, modifier: Modifier = Modifier.fillMaxWidth(), minLines: Int = 1, fieldHeight: androidx.compose.ui.unit.Dp? = null, expandable: Boolean = false, prefixText: String? = null, labelBold: Boolean = false, onFocusChange: (Boolean) -> Unit = {}, onFocusLostValue: ((String) -> String)? = null, onValueChange: (String) -> Unit) {
    BasicRecordTextField(
        label = label,
        value = value,
        editable = editable,
        modifier = modifier,
        minLines = minLines,
        fieldHeight = fieldHeight,
        expandable = expandable,
        prefixText = prefixText,
        labelBold = labelBold,
        onFocusChange = onFocusChange,
        onFocusLostValue = onFocusLostValue,
        onValueChange = onValueChange
    )
}

@Composable
private fun ReadOnlyField(label: String, value: String, modifier: Modifier = Modifier.fillMaxWidth(), fieldHeight: androidx.compose.ui.unit.Dp? = null, labelBold: Boolean = false) {
    BasicRecordTextField(
        label = label,
        value = value.ifBlank { "-" },
        editable = false,
        modifier = modifier,
        fieldHeight = fieldHeight,
        labelBold = labelBold,
        onValueChange = {}
    )
}

private fun defaultLocation(recordId: Long, type: String, createdAt: Long, updatedAt: Long): PortLocationEntity =
    PortLocationEntity(
        id = recordId * 10 + if (type == PortToolType.ANCHORAGE) 5 else 6,
        recordId = recordId,
        locationType = type,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

internal fun formatDisplayedUnlocode(raw: String): String {
    val cleaned = raw.uppercase().replace("-", "").trim()
    return if (cleaned.length == 5) "${cleaned.take(2)}-${cleaned.drop(2)}" else raw.uppercase()
}
