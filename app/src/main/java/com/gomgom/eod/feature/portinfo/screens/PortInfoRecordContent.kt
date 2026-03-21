package com.gomgom.eod.feature.portinfo.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gomgom.eod.R
import com.gomgom.eod.feature.portinfo.porttool.entity.PortAttachmentEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortLocationEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortOperationEntity
import com.gomgom.eod.feature.portinfo.porttool.entity.PortRecordBundle
import com.gomgom.eod.feature.portinfo.porttool.entity.PortToolType
import com.gomgom.eod.feature.portinfo.porttool.repository.PortUnlocodeEntry

@Composable
fun PortInfoRecordContent(
    editable: Boolean,
    bundle: PortRecordBundle,
    countrySuggestions: List<PortUnlocodeEntry>,
    portSuggestions: List<PortUnlocodeEntry>,
    isVesselReportingExpanded: Boolean,
    isAnchorageExpanded: Boolean,
    isBerthExpanded: Boolean,
    onToggleVesselReporting: () -> Unit,
    onToggleAnchorage: () -> Unit,
    onToggleBerth: () -> Unit,
    onBundleChange: (PortRecordBundle) -> Unit,
    onCountrySuggestionClick: (PortUnlocodeEntry) -> Unit,
    onPortSuggestionClick: (PortUnlocodeEntry) -> Unit,
    onAddAttachmentClick: () -> Unit,
    onDeleteAttachmentClick: (Long) -> Unit
) {
    val channelOptions = listOf(
        stringResource(R.string.port_info_channel_vhf),
        stringResource(R.string.port_info_channel_mfhf),
        stringResource(R.string.port_info_channel_custom)
    )
    val extraStatusOptions = listOf(
        stringResource(R.string.port_info_status_allowed),
        stringResource(R.string.port_info_status_denied),
        stringResource(R.string.port_info_status_conditional)
    )
    val berthingSideOptions = listOf(
        stringResource(R.string.port_info_side_port),
        stringResource(R.string.port_info_side_stbd),
        stringResource(R.string.port_info_side_bow_in),
        stringResource(R.string.port_info_side_stern_to)
    )
    val manifoldConnectionOptions = listOf(
        stringResource(R.string.port_info_connection_hose),
        stringResource(R.string.port_info_connection_loading_arm)
    )
    val standardAnsi = stringResource(R.string.port_info_standard_ansi)
    val standardJis = stringResource(R.string.port_info_standard_jis)
    val standardDin = stringResource(R.string.port_info_standard_din)
    val standardCustom = stringResource(R.string.port_info_standard_custom)
    val manifoldStandardOptions = listOf(
        standardAnsi,
        standardJis,
        standardDin,
        standardCustom
    )
    val transferRateUnitOptions = listOf("m3/h", "MT/h", "bbl/h")

    val vesselOps = remember(bundle.operations) { bundle.operations.associateBy { it.operationType } }
    val anchorage = remember(bundle.locations) { bundle.locations.firstOrNull { it.locationType == PortToolType.ANCHORAGE } }
    val berth = remember(bundle.locations) { bundle.locations.firstOrNull { it.locationType == PortToolType.BERTH } }

    val manifoldUnitOptions = remember(bundle.record.manifoldStandard) {
        when (bundle.record.manifoldStandard) {
            "ANSI" -> listOf("A")
            "JIS" -> listOf("mm")
            "DIN" -> listOf("DN")
            else -> emptyList()
        }
    }
    val manifoldClassOptions = remember(bundle.record.manifoldStandard) {
        when (bundle.record.manifoldStandard) {
            "ANSI" -> listOf("150#", "300#", "400#", "600#")
            "JIS" -> listOf("5K", "10K", "16K", "20K", "30K")
            "DIN" -> listOf("PN6", "PN10", "PN16", "PN25", "PN40", "PN63")
            else -> emptyList()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PortCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RecordTextField(
                    label = stringResource(R.string.port_info_field_country),
                    value = bundle.record.countryName,
                    editable = editable,
                    modifier = Modifier.weight(1f),
                    labelBold = true
                ) { onBundleChange(bundle.copy(record = bundle.record.copy(countryName = it))) }
                RecordTextField(
                    label = stringResource(R.string.port_info_field_port),
                    value = bundle.record.portName,
                    editable = editable,
                    modifier = Modifier.weight(1f),
                    labelBold = true
                ) { onBundleChange(bundle.copy(record = bundle.record.copy(portName = it))) }
            }
            if (editable && bundle.record.countryName.isNotBlank() && countrySuggestions.isNotEmpty()) {
                SuggestionPanel(countrySuggestions, { "${it.countryCode} ${it.countryName}" }, onCountrySuggestionClick)
            }
            if (editable && bundle.record.portName.isNotBlank() && portSuggestions.isNotEmpty()) {
                SuggestionPanel(portSuggestions, { "${it.countryCode} ${it.portName}" }, onPortSuggestionClick)
            }
            RecordTextField(
                label = stringResource(R.string.port_info_field_unlocode),
                value = bundle.record.unlocode,
                editable = editable,
                labelBold = true
            ) { onBundleChange(bundle.copy(record = bundle.record.copy(unlocode = it.uppercase()))) }
        }

        AccordionCard(stringResource(R.string.port_info_section_vessel_reporting), isVesselReportingExpanded, onToggleVesselReporting) {
            listOf(PortToolType.VTS, PortToolType.PILOT, PortToolType.TUG, PortToolType.CIQ).forEach { type ->
                val operation = vesselOps[type] ?: PortOperationEntity(
                    id = bundle.record.id * 100 + type.hashCode().toLong(),
                    recordId = bundle.record.id,
                    operationType = type,
                    createdAt = bundle.record.createdAt,
                    updatedAt = bundle.record.updatedAt
                )
                OperationBlock(type, operation, editable, channelOptions) { updated ->
                    onBundleChange(bundle.copy(operations = bundle.operations.filterNot { it.operationType == type } + updated))
                }
            }
        }

        AccordionCard(stringResource(R.string.port_info_section_anchorage), isAnchorageExpanded, onToggleAnchorage) {
            val current = anchorage ?: defaultLocation(bundle.record.id, PortToolType.ANCHORAGE, bundle.record.createdAt, bundle.record.updatedAt)
            RecordTextField(stringResource(R.string.port_info_field_anchorage_name), bundle.record.anchorageName, editable, labelBold = true) {
                onBundleChange(bundle.copy(record = bundle.record.copy(anchorageName = it), locations = bundle.locations.filterNot { location -> location.locationType == PortToolType.ANCHORAGE } + current.copy(name = it)))
            }
            RecordTextField(stringResource(R.string.port_info_field_info), bundle.record.anchorageInfo, editable, minLines = 2, labelBold = true) {
                onBundleChange(bundle.copy(record = bundle.record.copy(anchorageInfo = it), locations = bundle.locations.filterNot { location -> location.locationType == PortToolType.ANCHORAGE } + current.copy(info = it)))
            }
            ExtraWorkBlock(editable, bundle, extraStatusOptions, onBundleChange)
        }

        AccordionCard(stringResource(R.string.port_info_section_berth), isBerthExpanded, onToggleBerth) {
            val current = berth ?: defaultLocation(bundle.record.id, PortToolType.BERTH, bundle.record.createdAt, bundle.record.updatedAt)
            RecordTextField(stringResource(R.string.port_info_field_berth_name), bundle.record.berthName, editable, labelBold = true) {
                onBundleChange(bundle.copy(record = bundle.record.copy(berthName = it), locations = bundle.locations.filterNot { location -> location.locationType == PortToolType.BERTH } + current.copy(name = it)))
            }
            RecordTextField(stringResource(R.string.port_info_field_company), bundle.record.company, editable, labelBold = true) {
                onBundleChange(bundle.copy(record = bundle.record.copy(company = it), locations = bundle.locations.filterNot { location -> location.locationType == PortToolType.BERTH } + current.copy(company = it)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecordTextField(stringResource(R.string.port_info_field_berth_length), current.berthLengthMeters, editable, Modifier.weight(1f), labelBold = true) {
                    onBundleChange(bundle.copy(locations = bundle.locations.filterNot { it.locationType == PortToolType.BERTH } + current.copy(berthLengthMeters = it)))
                }
                RecordTextField(stringResource(R.string.port_info_field_depth), current.depthMeters, editable, Modifier.weight(1f), labelBold = true) {
                    onBundleChange(bundle.copy(locations = bundle.locations.filterNot { it.locationType == PortToolType.BERTH } + current.copy(depthMeters = it)))
                }
            }
            DropdownField(stringResource(R.string.port_info_field_berthing_side), current.berthingSide, editable, berthingSideOptions, labelBold = true) { selected ->
                onBundleChange(bundle.copy(locations = bundle.locations.filterNot { it.locationType == PortToolType.BERTH } + current.copy(berthingSide = selected)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecordTextField(stringResource(R.string.port_info_field_mooring_fore), current.mooringFore, editable, Modifier.weight(1f)) {
                    onBundleChange(bundle.copy(locations = bundle.locations.filterNot { it.locationType == PortToolType.BERTH } + current.copy(mooringFore = it)))
                }
                RecordTextField(stringResource(R.string.port_info_field_mooring_aft), current.mooringAft, editable, Modifier.weight(1f)) {
                    onBundleChange(bundle.copy(locations = bundle.locations.filterNot { it.locationType == PortToolType.BERTH } + current.copy(mooringAft = it)))
                }
            }
            RecordTextField(stringResource(R.string.port_info_field_info), bundle.record.berthInfo, editable, minLines = 2, labelBold = true) {
                onBundleChange(bundle.copy(record = bundle.record.copy(berthInfo = it), locations = bundle.locations.filterNot { location -> location.locationType == PortToolType.BERTH } + current.copy(info = it)))
            }
            ExtraWorkBlock(editable, bundle, extraStatusOptions, onBundleChange)
        }

        PortCard(title = stringResource(R.string.port_info_section_cargo_discharge)) {
            RecordTextField(stringResource(R.string.port_info_field_cargo), bundle.record.cargoName, editable, labelBold = true) {
                onBundleChange(bundle.copy(record = bundle.record.copy(cargoName = it)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DropdownField(stringResource(R.string.port_info_field_manifold_connection), bundle.record.manifoldConnectionType, editable, manifoldConnectionOptions, Modifier.weight(1f)) {
                    onBundleChange(bundle.copy(record = bundle.record.copy(manifoldConnectionType = it)))
                }
                DropdownField(stringResource(R.string.port_info_field_manifold_standard), bundle.record.manifoldStandard, editable, manifoldStandardOptions, Modifier.weight(1f)) { selected ->
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
                    onBundleChange(bundle.copy(record = bundle.record.copy(manifoldStandard = selected, manifoldUnit = unit, manifoldClass = clazz)))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecordTextField(stringResource(R.string.port_info_field_manifold_size), bundle.record.manifoldSize, editable, Modifier.weight(1f)) {
                    onBundleChange(bundle.copy(record = bundle.record.copy(manifoldSize = it)))
                }
                if (manifoldUnitOptions.isEmpty()) {
                    RecordTextField(stringResource(R.string.port_info_field_manifold_unit), bundle.record.manifoldUnit, editable, Modifier.weight(1f)) {
                        onBundleChange(bundle.copy(record = bundle.record.copy(manifoldUnit = it)))
                    }
                } else {
                    DropdownField(stringResource(R.string.port_info_field_manifold_unit), bundle.record.manifoldUnit, editable, manifoldUnitOptions, Modifier.weight(1f)) {
                        onBundleChange(bundle.copy(record = bundle.record.copy(manifoldUnit = it)))
                    }
                }
                if (manifoldClassOptions.isEmpty()) {
                    RecordTextField(stringResource(R.string.port_info_field_manifold_class), bundle.record.manifoldClass, editable, Modifier.weight(1f)) {
                        onBundleChange(bundle.copy(record = bundle.record.copy(manifoldClass = it)))
                    }
                } else {
                    DropdownField(stringResource(R.string.port_info_field_manifold_class), bundle.record.manifoldClass, editable, manifoldClassOptions, Modifier.weight(1f)) {
                        onBundleChange(bundle.copy(record = bundle.record.copy(manifoldClass = it)))
                    }
                }
            }
            RecordTextField("", bundle.record.cargoRemark, editable) {
                onBundleChange(bundle.copy(record = bundle.record.copy(cargoRemark = it)))
            }
            RecordTextField("", bundle.record.manifoldRemark, editable) {
                onBundleChange(bundle.copy(record = bundle.record.copy(manifoldRemark = it)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactNumericField(
                    label = stringResource(R.string.port_info_field_transfer_rate),
                    value = bundle.record.transferRate,
                    editable = editable,
                    modifier = Modifier.weight(1f),
                    width = 88.dp,
                    labelBold = true
                ) {
                    onBundleChange(bundle.copy(record = bundle.record.copy(transferRate = it.filter { ch -> ch.isDigit() || ch == '.' })))
                }
                DropdownField(stringResource(R.string.port_info_field_transfer_rate_unit), bundle.record.transferRateUnit, editable, transferRateUnitOptions, Modifier.weight(1f)) {
                    onBundleChange(bundle.copy(record = bundle.record.copy(transferRateUnit = it)))
                }
            }
            RecordTextField(stringResource(R.string.port_info_field_discharge_info), bundle.record.dischargeInfo, editable, minLines = 2, labelBold = true) {
                onBundleChange(bundle.copy(record = bundle.record.copy(dischargeInfo = it)))
            }
            RecordTextField(stringResource(R.string.port_info_field_operator), bundle.record.operatorName, editable, labelBold = true) {
                onBundleChange(bundle.copy(record = bundle.record.copy(operatorName = it)))
            }
            RecordTextField(stringResource(R.string.port_info_field_safety_officer), bundle.record.safetyOfficerName, editable, labelBold = true) {
                onBundleChange(bundle.copy(record = bundle.record.copy(safetyOfficerName = it)))
            }
            RecordTextField(stringResource(R.string.port_info_field_surveyor), bundle.record.surveyorName, editable, labelBold = true) {
                onBundleChange(bundle.copy(record = bundle.record.copy(surveyorName = it)))
            }
            RecordTextField(stringResource(R.string.port_info_field_caution), bundle.record.caution, editable, minLines = 2, labelBold = true) {
                onBundleChange(bundle.copy(record = bundle.record.copy(caution = it)))
            }
        }

        PortCard(title = stringResource(R.string.port_info_section_attachments)) {
            if (editable) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFEAF2FF), RoundedCornerShape(32.dp))
                        .clickable(onClick = onAddAttachmentClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.port_info_add_attachment),
                        tint = Color(0xFF123A73),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            if (bundle.attachments.isNotEmpty()) {
                AttachmentGrid(bundle.attachments, editable, onDeleteAttachmentClick)
            }
        }
    }
}

@Composable
private fun ExtraWorkBlock(editable: Boolean, bundle: PortRecordBundle, options: List<String>, onChange: (PortRecordBundle) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DropdownField(stringResource(R.string.port_info_field_supply), bundle.record.supplyStatus, editable, options, labelBold = true) {
            onChange(bundle.copy(record = bundle.record.copy(supplyStatus = it)))
        }
        RecordTextField("", bundle.record.supplyRemark, editable) {
            onChange(bundle.copy(record = bundle.record.copy(supplyRemark = it)))
        }
        DropdownField(stringResource(R.string.port_info_field_waste), bundle.record.wasteStatus, editable, options) {
            onChange(bundle.copy(record = bundle.record.copy(wasteStatus = it)))
        }
        RecordTextField("", bundle.record.wasteRemark, editable) {
            onChange(bundle.copy(record = bundle.record.copy(wasteRemark = it)))
        }
        DropdownField(stringResource(R.string.port_info_field_crew_change), bundle.record.crewChangeStatus, editable, options) {
            onChange(bundle.copy(record = bundle.record.copy(crewChangeStatus = it)))
        }
        RecordTextField("", bundle.record.crewChangeRemark, editable) {
            onChange(bundle.copy(record = bundle.record.copy(crewChangeRemark = it)))
        }
    }
}

@Composable
private fun SuggestionPanel(entries: List<PortUnlocodeEntry>, formatter: (PortUnlocodeEntry) -> String, onSelect: (PortUnlocodeEntry) -> Unit) {
    Card(shape = RoundedCornerShape(36.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FBFF))) {
        Column(modifier = Modifier.fillMaxWidth()) {
            entries.take(7).forEach { entry ->
                Text(
                    text = formatter(entry),
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(entry) }.padding(horizontal = 12.dp, vertical = 10.dp),
                    color = Color(0xFF123A73),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PortCard(title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!title.isNullOrBlank()) {
                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF123A73),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            content()
        }
    }
}

@Composable
private fun AccordionCard(title: String, expanded: Boolean, onToggle: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    PortCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp))
            Text(
                text = title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF123A73),
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(onClick = onToggle) {
                Icon(imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, tint = Color(0xFF123A73))
            }
        }
        if (expanded) content()
    }
}

@Composable
private fun OperationBlock(title: String, operation: PortOperationEntity, editable: Boolean, channelOptions: List<String>, onChange: (PortOperationEntity) -> Unit) {
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
                fontSize = 18.sp
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                InlineDropdownField(
                    value = operation.channelGroup.ifBlank { channelOptions.firstOrNull().orEmpty() },
                    editable = editable,
                    options = channelOptions
                ) {
                    onChange(operation.copy(channelGroup = it))
                }
            }
            CompactChannelField(
                value = operation.channelValue,
                editable = editable,
                modifier = Modifier.padding(end = 40.dp)
            ) {
                onChange(operation.copy(channelValue = it.filter { ch -> ch.isDigit() || ch == '/' || ch == '-' }))
            }
        }
        RecordTextField("", operation.remark, editable, minLines = 2) {
            onChange(operation.copy(remark = it))
        }
    }
}

@Composable
private fun InlineDropdownField(
    value: String,
    editable: Boolean,
    options: List<String>,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit
) {
    if (!editable) {
        Text(
            text = value,
            modifier = modifier,
            color = Color(0xFF123A73),
            fontSize = 16.sp,
            textDecoration = TextDecoration.Underline
        )
        return
    }
    var expanded by remember(value, options) { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier.clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                color = Color(0xFF123A73),
                fontSize = 16.sp,
                textDecoration = TextDecoration.Underline
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF123A73),
                modifier = Modifier.size(16.dp)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onSelect(option)
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
    onValueChange: (String) -> Unit
) {
    CompactNumericField(
        label = null,
        value = value,
        editable = editable,
        modifier = modifier,
        width = 62.dp,
        bracketed = true,
        onValueChange = onValueChange
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
                        color = if (value.isBlank()) Color(0xFFB8C6DA) else Color.Transparent,
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
                        fontSize = 16.sp
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
private fun DropdownField(label: String, value: String, editable: Boolean, options: List<String>, modifier: Modifier = Modifier.fillMaxWidth(), labelBold: Boolean = false, onSelect: (String) -> Unit) {
    if (!editable) {
        ReadOnlyField(label, value, modifier, labelBold)
        return
    }
    var expanded by remember(value, options) { mutableStateOf(false) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 15.sp, color = Color(0xFF5D7598), fontWeight = if (labelBold) FontWeight.Bold else FontWeight.Normal)
        Box {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
                    }
                }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth()) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
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
private fun RecordTextField(label: String, value: String, editable: Boolean, modifier: Modifier = Modifier.fillMaxWidth(), minLines: Int = 1, labelBold: Boolean = false, onValueChange: (String) -> Unit) {
    if (editable) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (label.isNotBlank()) {
                Text(label, fontSize = 15.sp, color = Color(0xFF5D7598), fontWeight = if (labelBold) FontWeight.Bold else FontWeight.Normal)
            }
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = minLines,
                shape = RoundedCornerShape(16.dp)
            )
        }
    } else {
        ReadOnlyField(label, value, modifier, labelBold)
    }
}

@Composable
private fun ReadOnlyField(label: String, value: String, modifier: Modifier = Modifier.fillMaxWidth(), labelBold: Boolean = false) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (label.isNotBlank()) {
            Text(label, fontSize = 15.sp, color = Color(0xFF5D7598), fontWeight = if (labelBold) FontWeight.Bold else FontWeight.Normal)
        }
        Text(text = value.ifBlank { "-" }, fontSize = 15.sp, color = Color(0xFF123A73))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AttachmentGrid(attachments: List<PortAttachmentEntity>, editable: Boolean, onDeleteAttachmentClick: (Long) -> Unit) {
    val context = LocalContext.current
    var deleteTargetId by remember { mutableStateOf<Long?>(null) }
    if (deleteTargetId != null) {
        AlertDialog(
            onDismissRequest = { deleteTargetId = null },
            title = { Text(stringResource(R.string.port_info_attachment_delete_title), color = Color(0xFF123A73), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.port_info_attachment_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteAttachmentClick(deleteTargetId!!)
                    deleteTargetId = null
                }) { Text(stringResource(R.string.common_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTargetId = null }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.fillMaxWidth().height((((attachments.size + 3) / 4) * 88).dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false
    ) {
        items(attachments, key = { it.id }) { attachment ->
            val itemModifier = if (editable) {
                Modifier.fillMaxWidth().combinedClickable(onClick = {}, onLongClick = { deleteTargetId = attachment.id })
            } else {
                Modifier.fillMaxWidth().combinedClickable(onClick = {}, onLongClick = {})
            }
            Column(
                modifier = itemModifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier.size(72.dp).background(Color(0xFFEAF2FF), RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (attachment.thumbnailPath.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(attachment.thumbnailPath).size(200).crossfade(true).build(),
                            contentDescription = attachment.displayName,
                            modifier = Modifier.size(72.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = when (attachment.attachmentType) {
                                PortToolType.IMAGE -> stringResource(R.string.port_info_attachment_type_image)
                                PortToolType.VIDEO -> stringResource(R.string.port_info_attachment_type_video)
                                else -> stringResource(R.string.port_info_attachment_type_file)
                            },
                            color = Color(0xFF123A73),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = attachment.displayName.ifBlank { attachment.filePath.substringAfterLast('/') },
                    fontSize = 11.sp,
                    color = Color(0xFF5D7598),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun defaultLocation(recordId: Long, type: String, createdAt: Long, updatedAt: Long): PortLocationEntity =
    PortLocationEntity(
        id = recordId * 10 + if (type == PortToolType.ANCHORAGE) 5 else 6,
        recordId = recordId,
        locationType = type,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
