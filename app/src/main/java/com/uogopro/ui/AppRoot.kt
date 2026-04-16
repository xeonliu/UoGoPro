package com.uogopro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.uogopro.domain.CameraModel
import com.uogopro.domain.CameraState
import com.uogopro.domain.CaptureMode
import com.uogopro.domain.EvCompensation
import com.uogopro.domain.FieldOfView
import com.uogopro.domain.Hero4Compatibility
import com.uogopro.domain.IsoLimit
import com.uogopro.domain.Sharpness
import com.uogopro.domain.WhiteBalance
import com.uogopro.viewmodel.CameraUiState
import com.uogopro.viewmodel.CameraViewModel

private enum class AppTab(val label: String, val icon: ImageVector) {
    Connection("Connect", Icons.Default.Wifi),
    Control("Control", Icons.Default.Videocam),
    Settings("Settings", Icons.Default.Settings),
}

@Composable
fun AppRoot(viewModel: CameraViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(AppTab.Connection) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error, uiState.message) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
        uiState.message?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (uiState.busy) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                when (selectedTab) {
                    AppTab.Connection -> ConnectionScreen(uiState, viewModel)
                    AppTab.Control -> ControlScreen(uiState, viewModel)
                    AppTab.Settings -> SettingsScreen(uiState, viewModel)
                }
            }
        }
    }
}

@Composable
private fun ConnectionScreen(
    uiState: CameraUiState,
    viewModel: CameraViewModel,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Header(
                title = "UoGoPro",
                subtitle = "HERO4 Black/Silver Wi-Fi control",
            )
        }
        item {
            Section(title = "Camera") {
                OutlinedTextField(
                    value = uiState.host,
                    onValueChange = viewModel::updateHost,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Host") },
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = viewModel::refreshStatus,
                        enabled = !uiState.busy,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Refresh")
                    }
                    FilledTonalButton(
                        onClick = { viewModel.updateHost("10.5.5.9") },
                        enabled = !uiState.busy,
                    ) {
                        Text("Default")
                    }
                }
            }
        }
        item {
            Section(title = "Pairing") {
                OutlinedTextField(
                    value = uiState.pairingPin,
                    onValueChange = viewModel::updatePairingPin,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("4-digit PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = viewModel::pair,
                    enabled = !uiState.busy && uiState.pairingPin.length == 4,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Pair HERO4")
                }
            }
        }
        item {
            StatusSummary(uiState.cameraState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ControlScreen(
    uiState: CameraUiState,
    viewModel: CameraViewModel,
) {
    val camera = uiState.cameraState
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Header(
                title = "Control",
                subtitle = if (camera.connected) "${camera.mode.label} ready" else "Connect to camera Wi-Fi first",
            )
        }
        item {
            PreviewPanel(uiState, viewModel)
        }
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                CaptureMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = camera.mode == mode,
                        onClick = { viewModel.setMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = CaptureMode.entries.size),
                        enabled = !uiState.busy,
                    ) {
                        Text(mode.label)
                    }
                }
            }
        }
        item {
            Button(
                onClick = viewModel::shutter,
                enabled = !uiState.busy,
                modifier = Modifier
                    .width(184.dp)
                    .height(184.dp),
                shape = CircleShape,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (camera.isRecording) "Stop" else "Shutter",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }
        item {
            StatusSummary(camera)
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ElevatedButton(
                    onClick = viewModel::tagMoment,
                    enabled = !uiState.busy,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Tag")
                }
                ElevatedButton(
                    onClick = viewModel::toggleLocate,
                    enabled = !uiState.busy,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (uiState.locating) "Locate Off" else "Locate On")
                }
            }
        }
        item {
            TextButton(
                onClick = viewModel::powerOff,
                enabled = !uiState.busy,
            ) {
                Text("Power Off")
            }
        }
    }
}

@Composable
private fun PreviewPanel(
    uiState: CameraUiState,
    viewModel: CameraViewModel,
) {
    Section(title = "Preview") {
        if (uiState.previewActive && uiState.previewUri.isNotBlank()) {
            GoProPreviewPlayer(
                uri = uiState.previewUri,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = viewModel::startPreview,
                enabled = !uiState.busy && !uiState.previewActive && !uiState.previewProbe.active,
                modifier = Modifier.weight(1f),
            ) {
                Text("Start Preview")
            }
            FilledTonalButton(
                onClick = viewModel::stopPreview,
                enabled = !uiState.busy && uiState.previewActive,
                modifier = Modifier.weight(1f),
            ) {
                Text("Stop")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ElevatedButton(
                onClick = viewModel::startPreviewProbe,
                enabled = !uiState.busy && !uiState.previewActive && !uiState.previewProbe.active,
                modifier = Modifier.weight(1f),
            ) {
                Text("Start Probe")
            }
            ElevatedButton(
                onClick = viewModel::stopPreviewProbe,
                enabled = !uiState.busy && uiState.previewProbe.active,
                modifier = Modifier.weight(1f),
            ) {
                Text("Stop Probe")
            }
        }
        if (uiState.previewActive) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.previewUri,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
            )
        }
        if (uiState.previewProbe.active || uiState.previewProbe.totalPackets > 0 || uiState.previewProbe.lastError != null) {
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow("Probe", if (uiState.previewProbe.active) "Listening on UDP 8554" else "Stopped")
            InfoRow("Packets/sec", uiState.previewProbe.packetsPerSecond.toString())
            InfoRow("KB/sec", "%.1f".format(uiState.previewProbe.bytesPerSecond / 1024.0))
            InfoRow("Total packets", uiState.previewProbe.totalPackets.toString())
            InfoRow("Total MB", "%.2f".format(uiState.previewProbe.totalBytes / 1024.0 / 1024.0))
            uiState.previewProbe.lastError?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun GoProPreviewPlayer(
    uri: String,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { viewContext -> FfmpegPreviewSurfaceView(viewContext).apply { start(uri) } },
        update = { view ->
            if (view.streamUri != uri) {
                view.start(uri)
            }
        },
    )
}

@Composable
private fun SettingsScreen(
    uiState: CameraUiState,
    viewModel: CameraViewModel,
) {
    val settings = Hero4Compatibility.coerceSettings(uiState.model, uiState.cameraState.videoSettings)
    val resolutions = Hero4Compatibility.supportedResolutions(uiState.model)
    val frameRates = Hero4Compatibility.frameRatesFor(uiState.model, settings.resolution)
    val fovs = Hero4Compatibility.fovsFor(uiState.model, settings.resolution)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Header(
                title = "Settings",
                subtitle = "HERO4 Black/Silver video controls",
            )
        }
        item {
            Section(title = "Model") {
                OptionMenu(
                    label = "Camera model",
                    selected = uiState.model,
                    options = CameraModel.entries,
                    optionLabel = { it.label },
                    onSelected = viewModel::updateModel,
                )
            }
        }
        item {
            Section(title = "Video") {
                OptionMenu(
                    label = "Resolution",
                    selected = settings.resolution,
                    options = resolutions,
                    optionLabel = { "${it.label}  ${it.dimensions}" },
                    onSelected = viewModel::setResolution,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OptionMenu(
                    label = "Frame rate",
                    selected = settings.frameRate,
                    options = frameRates,
                    optionLabel = { it.label },
                    onSelected = viewModel::setFrameRate,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OptionMenu(
                    label = "FOV",
                    selected = settings.fov,
                    options = fovs,
                    optionLabel = { it.label },
                    onSelected = viewModel::setFov,
                )
            }
        }
        item {
            Section(title = "Exposure") {
                ToggleRow("Low light", settings.lowLight, viewModel::setLowLight)
                ToggleRow("Spot meter", settings.spotMeter, viewModel::setSpotMeter)
                ToggleRow("Protune", settings.protune, viewModel::setProtune)
                Spacer(modifier = Modifier.height(12.dp))
                OptionMenu(
                    label = "White balance",
                    selected = settings.whiteBalance,
                    options = WhiteBalance.entries,
                    optionLabel = { it.label },
                    onSelected = viewModel::setWhiteBalance,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OptionMenu(
                    label = "ISO limit",
                    selected = settings.isoLimit,
                    options = IsoLimit.entries,
                    optionLabel = { it.label },
                    onSelected = viewModel::setIsoLimit,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OptionMenu(
                    label = "Sharpness",
                    selected = settings.sharpness,
                    options = Sharpness.entries,
                    optionLabel = { it.label },
                    onSelected = viewModel::setSharpness,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OptionMenu(
                    label = "EV comp",
                    selected = settings.evCompensation,
                    options = EvCompensation.entries,
                    optionLabel = { it.label },
                    onSelected = viewModel::setEvCompensation,
                )
            }
        }
    }
}

@Composable
private fun Header(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
        )
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun StatusSummary(camera: CameraState) {
    Section(title = "Status") {
        InfoRow("Connected", if (camera.connected) "Yes" else "No")
        InfoRow("Battery", if (camera.batteryAvailable) camera.batteryLevel.label else "Unavailable")
        InfoRow("SD card", camera.sdCardState.label)
        InfoRow("Mode", camera.mode.label)
        InfoRow("Recording", if (camera.isRecording) "Yes" else "No")
        InfoRow("Duration", formatSeconds(camera.recordingDurationSeconds))
        InfoRow("Photos left", camera.remainingPhotos?.toString() ?: "-")
        InfoRow("Video left", camera.remainingVideoSeconds?.let(::formatSeconds) ?: "-")
        InfoRow("Free space", camera.freeSpaceBytes?.let(::formatBytes) ?: "-")
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun <T> OptionMenu(
    label: String,
    selected: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
        )
        Spacer(modifier = Modifier.height(6.dp))
        FilledTonalButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = optionLabel(selected),
                modifier = Modifier.weight(1f),
            )
            Text("Change")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        expanded = false
                        if (option != selected) {
                            onSelected(option)
                        }
                    },
                )
            }
        }
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}

private fun formatBytes(bytes: Long): String {
    val gib = bytes / 1024.0 / 1024.0 / 1024.0
    return "%.1f GB".format(gib)
}
