package com.nimroddayan.clipit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.nimroddayan.clipit.data.gemini.DetectionMode
import com.nimroddayan.clipit.data.gemini.GeminiModel
import com.nimroddayan.clipit.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsScreen(
        viewModel: SettingsViewModel,
        onNavigateUp: () -> Unit,
) {
        val geminiApiKey by viewModel.geminiApiKey.collectAsState()
        var apiKey by remember(geminiApiKey) { mutableStateOf(geminiApiKey) }
        val coroutineScope = rememberCoroutineScope()
        val geminiModel by viewModel.geminiModel.collectAsState()
        var selectedModel by remember(geminiModel) { mutableStateOf(geminiModel) }
        var expanded by remember { mutableStateOf(false) }
        val geminiTemperature by viewModel.geminiTemperature.collectAsState()
        var temperature by remember(geminiTemperature) { mutableStateOf(geminiTemperature) }
        val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

        val detectionMode by viewModel.detectionMode.collectAsState()
        var selectedDetectionMode by remember(detectionMode) { mutableStateOf(detectionMode) }
        var detectionModeExpanded by remember { mutableStateOf(false) }

        Scaffold(
                snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
                topBar = {
                        CenterAlignedTopAppBar(
                                title = { Text("AI Settings") },
                                navigationIcon = {
                                        IconButton(onClick = onNavigateUp) {
                                                Icon(
                                                        imageVector =
                                                                Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Back"
                                                )
                                        }
                                },
                                colors =
                                        TopAppBarDefaults.centerAlignedTopAppBarColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme.background,
                                                titleContentColor =
                                                        MaterialTheme.colorScheme.primary,
                                                navigationIconContentColor =
                                                        MaterialTheme.colorScheme.primary
                                        )
                        )
                }
        ) { paddingValues ->
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(paddingValues)
                                        .background(MaterialTheme.colorScheme.background)
                ) {
                        Column(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .verticalScroll(rememberScrollState())
                                                .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                                // API Key Section
                                ElevatedCard(
                                        colors =
                                                CardDefaults.elevatedCardColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme.surface
                                                ),
                                        elevation = CardDefaults.elevatedCardElevation(2.dp)
                                ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                                Row(
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Icon(
                                                                imageVector = Icons.Default.Key,
                                                                contentDescription =
                                                                        "Gemini API Key",
                                                                tint =
                                                                        MaterialTheme.colorScheme
                                                                                .primary
                                                        )
                                                        Text(
                                                                text = "Gemini API Key",
                                                                modifier =
                                                                        Modifier.padding(
                                                                                start = 16.dp
                                                                        ),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .titleMedium
                                                        )
                                                }
                                                OutlinedTextField(
                                                        value = apiKey,
                                                        onValueChange = { apiKey = it },
                                                        label = { Text("API Key") },
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(top = 16.dp),
                                                        visualTransformation =
                                                                PasswordVisualTransformation(),
                                                        singleLine = true
                                                )
                                                Button(
                                                        onClick = {
                                                                coroutineScope.launch {
                                                                        viewModel.saveGeminiApiKey(
                                                                                apiKey
                                                                        )
                                                                        snackbarHostState
                                                                                .showSnackbar(
                                                                                        "API Key saved successfully"
                                                                                )
                                                                }
                                                        },
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(top = 16.dp)
                                                ) { Text("Save Key") }
                                        }
                                }

                                // Detection Mode Section
                                ElevatedCard(
                                        colors =
                                                CardDefaults.elevatedCardColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme.surface
                                                ),
                                        elevation = CardDefaults.elevatedCardElevation(2.dp)
                                ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                        "Detection Strategy",
                                                        style = MaterialTheme.typography.titleMedium
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))

                                                ExposedDropdownMenuBox(
                                                        expanded = detectionModeExpanded,
                                                        onExpandedChange = {
                                                                detectionModeExpanded =
                                                                        !detectionModeExpanded
                                                        },
                                                ) {
                                                        OutlinedTextField(
                                                                modifier =
                                                                        Modifier.menuAnchor()
                                                                                .fillMaxWidth(),
                                                                readOnly = true,
                                                                value =
                                                                        selectedDetectionMode
                                                                                .displayName,
                                                                onValueChange = {},
                                                                label = { Text("Mode") },
                                                                trailingIcon = {
                                                                        ExposedDropdownMenuDefaults
                                                                                .TrailingIcon(
                                                                                        expanded =
                                                                                                detectionModeExpanded
                                                                                )
                                                                },
                                                        )
                                                        ExposedDropdownMenu(
                                                                expanded = detectionModeExpanded,
                                                                onDismissRequest = {
                                                                        detectionModeExpanded =
                                                                                false
                                                                },
                                                        ) {
                                                                DetectionMode.values().forEach {
                                                                        mode ->
                                                                        DropdownMenuItem(
                                                                                text = {
                                                                                        Text(
                                                                                                mode.displayName
                                                                                        )
                                                                                },
                                                                                onClick = {
                                                                                        selectedDetectionMode =
                                                                                                mode
                                                                                        detectionModeExpanded =
                                                                                                false
                                                                                },
                                                                                contentPadding =
                                                                                        ExposedDropdownMenuDefaults
                                                                                                .ItemContentPadding,
                                                                        )
                                                                }
                                                        }
                                                }
                                                Button(
                                                        onClick = {
                                                                coroutineScope.launch {
                                                                        viewModel.saveDetectionMode(
                                                                                selectedDetectionMode
                                                                        )
                                                                        snackbarHostState
                                                                                .showSnackbar(
                                                                                        "Detection mode updated successfully"
                                                                                )
                                                                }
                                                        },
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(top = 16.dp)
                                                ) { Text("Update Mode") }
                                        }
                                }

                                // Model Selection Section
                                ElevatedCard(
                                        colors =
                                                CardDefaults.elevatedCardColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme.surface
                                                ),
                                        elevation = CardDefaults.elevatedCardElevation(2.dp)
                                ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                        "Model Configuration",
                                                        style = MaterialTheme.typography.titleMedium
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))

                                                ExposedDropdownMenuBox(
                                                        expanded = expanded,
                                                        onExpandedChange = { expanded = !expanded },
                                                ) {
                                                        OutlinedTextField(
                                                                modifier =
                                                                        Modifier.menuAnchor()
                                                                                .fillMaxWidth(),
                                                                readOnly = true,
                                                                value = selectedModel,
                                                                onValueChange = {},
                                                                label = { Text("Gemini Model") },
                                                                trailingIcon = {
                                                                        ExposedDropdownMenuDefaults
                                                                                .TrailingIcon(
                                                                                        expanded =
                                                                                                expanded
                                                                                )
                                                                },
                                                        )
                                                        ExposedDropdownMenu(
                                                                expanded = expanded,
                                                                onDismissRequest = {
                                                                        expanded = false
                                                                },
                                                        ) {
                                                                GeminiModel.entries.forEach { model ->
                                                                        DropdownMenuItem(
                                                                                text = { Text(model.modelName) },
                                                                                onClick = {
                                                                                        selectedModel = model.modelName
                                                                                        expanded = false
                                                                                },
                                                                                contentPadding =
                                                                                        ExposedDropdownMenuDefaults
                                                                                                .ItemContentPadding,
                                                                        )
                                                                }
                                                        }
                                                }
                                                Button(
                                                        onClick = {
                                                                coroutineScope.launch {
                                                                        viewModel.saveGeminiModel(
                                                                                selectedModel
                                                                        )
                                                                        snackbarHostState
                                                                                .showSnackbar(
                                                                                        "Model updated successfully"
                                                                                )
                                                                }
                                                        },
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(top = 16.dp)
                                                ) { Text("Update Model") }
                                        }
                                }

                                // Parameters Section
                                ElevatedCard(
                                        colors =
                                                CardDefaults.elevatedCardColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme.surface
                                                ),
                                        elevation = CardDefaults.elevatedCardElevation(2.dp)
                                ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                        "Parameters",
                                                        style = MaterialTheme.typography.titleMedium
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                        text =
                                                                "Temperature: %.2f".format(
                                                                        temperature
                                                                ),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                                )
                                                Slider(
                                                        value = temperature,
                                                        onValueChange = { temperature = it },
                                                        valueRange = 0f..1f,
                                                        steps = 19,
                                                        modifier = Modifier.padding(vertical = 8.dp)
                                                )
                                                Button(
                                                        onClick = {
                                                                coroutineScope.launch {
                                                                        viewModel
                                                                                .saveGeminiTemperature(
                                                                                        temperature
                                                                                )
                                                                        snackbarHostState
                                                                                .showSnackbar(
                                                                                        "Parameters saved successfully"
                                                                                )
                                                                }
                                                        },
                                                        modifier = Modifier.fillMaxWidth()
                                                ) { Text("Save Parameters") }
                                        }
                                }
                        }
                }
        }
}
