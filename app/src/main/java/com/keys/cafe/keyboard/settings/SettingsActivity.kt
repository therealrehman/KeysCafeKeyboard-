package com.keys.cafe.keyboard.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.keys.cafe.keyboard.data.SettingsRepository
import com.keys.cafe.keyboard.model.KeyboardSettings
import com.keys.cafe.keyboard.theme.ThemeManager
import com.keys.cafe.keyboard.layout.LayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Main settings activity for KeysCafe Keyboard.
 */
class SettingsActivity : ComponentActivity() {

    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = SettingsRepository(applicationContext)

        setContent {
            var settings by remember { mutableStateOf(KeyboardSettings()) }

            LaunchedEffect(Unit) {
                settingsRepository.settingsFlow.collectLatest { newSettings ->
                    settings = newSettings
                }
            }

            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFFFAA00),
                    surface = Color(0xFF0D0D0D),
                    background = Color(0xFF000000)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen(
                        settings = settings,
                        onSettingsChange = { newSettings ->
                            lifecycleScope.launch {
                                settingsRepository.updateSettings(newSettings)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: KeyboardSettings,
    onSettingsChange: (KeyboardSettings) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Layout", "Theme", "Size", "Sound", "Vibration", "Accessibility")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KeysCafe Settings", color = Color(0xFFFFAA00)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D0D0D)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF0D0D0D),
                contentColor = Color(0xFFFFAA00)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Content
            when (selectedTab) {
                0 -> LayoutSettings(settings, onSettingsChange)
                1 -> ThemeSettings(settings, onSettingsChange)
                2 -> SizeSettings(settings, onSettingsChange)
                3 -> SoundSettings(settings, onSettingsChange)
                4 -> VibrationSettings(settings, onSettingsChange)
                5 -> AccessibilitySettings(settings, onSettingsChange)
            }
        }
    }
}

@Composable
fun LayoutSettings(
    settings: KeyboardSettings,
    onSettingsChange: (KeyboardSettings) -> Unit
) {
    val layouts = listOf("qwerty", "qwertz", "azerty", "dvorak", "colemak")
    val layoutNames = mapOf(
        "qwerty" to "QWERTY",
        "qwertz" to "QWERTZ (German)",
        "azerty" to "AZERTY (French)",
        "dvorak" to "Dvorak",
        "colemak" to "Colemak"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(layouts) { layoutId ->
            val isSelected = settings.currentLayout == layoutId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF1A1A1A) else Color(0xFF0D0D0D)
                ),
                border = if (isSelected) {
                    CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFAA00))
                    )
                } else null
            ) {
                ListItem(
                    headlineContent = { 
                        Text(
                            layoutNames[layoutId] ?: layoutId,
                            color = if (isSelected) Color(0xFFFFAA00) else Color.White
                        )
                    },
                    trailingContent = {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFFFFAA00)
                            )
                        }
                    },
                    modifier = Modifier.clickable {
                        onSettingsChange(settings.copy(currentLayout = layoutId))
                    }
                )
            }
        }
    }
}

@Composable
fun ThemeSettings(
    settings: KeyboardSettings,
    onSettingsChange: (KeyboardSettings) -> Unit
) {
    val themes = listOf("fire", "dark", "light", "amoled")
    val themeNames = mapOf(
        "fire" to "Fire Glow",
        "dark" to "Dark",
        "light" to "Light",
        "amoled" to "AMOLED Black"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(themes) { themeId ->
            val isSelected = settings.currentTheme == themeId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF1A1A1A) else Color(0xFF0D0D0D)
                )
            ) {
                ListItem(
                    headlineContent = { 
                        Text(
                            themeNames[themeId] ?: themeId,
                            color = if (isSelected) Color(0xFFFFAA00) else Color.White
                        )
                    },
                    trailingContent = {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFFFFAA00)
                            )
                        }
                    },
                    modifier = Modifier.clickable {
                        onSettingsChange(settings.copy(currentTheme = themeId))
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Glow Effect", color = Color(0xFFFFAA00), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            SwitchSetting(
                title = "Enable Glow",
                checked = settings.glowEnabled,
                onCheckedChange = { onSettingsChange(settings.copy(glowEnabled = it)) }
            )
        }
    }
}

@Composable
fun SizeSettings(
    settings: KeyboardSettings,
    onSettingsChange: (KeyboardSettings) -> Unit
) {
    val sizes = listOf(
        KeyboardSettings.KeySize.SMALL,
        KeyboardSettings.KeySize.MEDIUM,
        KeyboardSettings.KeySize.LARGE,
        KeyboardSettings.KeySize.EXTRA_LARGE
    )
    val sizeNames = mapOf(
        KeyboardSettings.KeySize.SMALL to "Small",
        KeyboardSettings.KeySize.MEDIUM to "Medium",
        KeyboardSettings.KeySize.LARGE to "Large",
        KeyboardSettings.KeySize.EXTRA_LARGE to "Extra Large"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(sizes) { size ->
            val isSelected = settings.keySize == size
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF1A1A1A) else Color(0xFF0D0D0D)
                )
            ) {
                ListItem(
                    headlineContent = { 
                        Text(
                            sizeNames[size] ?: size.name,
                            color = if (isSelected) Color(0xFFFFAA00) else Color.White
                        )
                    },
                    trailingContent = {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFFFFAA00)
                            )
                        }
                    },
                    modifier = Modifier.clickable {
                        onSettingsChange(settings.copy(keySize = size))
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Custom Size", color = Color(0xFFFFAA00), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = settings.keySizePercent / 100f,
                onValueChange = { 
                    onSettingsChange(settings.copy(keySizePercent = (it * 100).toInt()))
                },
                valueRange = 0.5f..2.0f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFFAA00),
                    activeTrackColor = Color(0xFFFFAA00)
                )
            )
            Text(
                "${settings.keySizePercent}%",
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun SoundSettings(
    settings: KeyboardSettings,
    onSettingsChange: (KeyboardSettings) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            SwitchSetting(
                title = "Enable Sound",
                checked = settings.soundEnabled,
                onCheckedChange = { onSettingsChange(settings.copy(soundEnabled = it)) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Volume", color = Color(0xFFFFAA00), style = MaterialTheme.typography.titleMedium)
            Slider(
                value = settings.soundVolume / 100f,
                onValueChange = { 
                    onSettingsChange(settings.copy(soundVolume = (it * 100).toInt()))
                },
                valueRange = 0f..1f,
                enabled = settings.soundEnabled,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFFAA00),
                    activeTrackColor = Color(0xFFFFAA00)
                )
            )
            Text("${settings.soundVolume}%", color = Color.White)
        }
    }
}

@Composable
fun VibrationSettings(
    settings: KeyboardSettings,
    onSettingsChange: (KeyboardSettings) -> Unit
) {
    val strengths = listOf(
        KeyboardSettings.VibrationStrength.NONE,
        KeyboardSettings.VibrationStrength.LIGHT,
        KeyboardSettings.VibrationStrength.MEDIUM,
        KeyboardSettings.VibrationStrength.STRONG
    )
    val strengthNames = mapOf(
        KeyboardSettings.VibrationStrength.NONE to "No Vibration",
        KeyboardSettings.VibrationStrength.LIGHT to "Light",
        KeyboardSettings.VibrationStrength.MEDIUM to "Medium",
        KeyboardSettings.VibrationStrength.STRONG to "Strong"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            SwitchSetting(
                title = "Enable Vibration",
                checked = settings.vibrationEnabled,
                onCheckedChange = { onSettingsChange(settings.copy(vibrationEnabled = it)) }
            )
        }

        items(strengths) { strength ->
            val isSelected = settings.vibrationStrength == strength
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF1A1A1A) else Color(0xFF0D0D0D)
                )
            ) {
                ListItem(
                    headlineContent = { 
                        Text(
                            strengthNames[strength] ?: strength.name,
                            color = if (isSelected) Color(0xFFFFAA00) else Color.White
                        )
                    },
                    trailingContent = {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFFFFAA00)
                            )
                        }
                    },
                    modifier = Modifier.clickable {
                        onSettingsChange(settings.copy(vibrationStrength = strength))
                    }
                )
            }
        }
    }
}

@Composable
fun AccessibilitySettings(
    settings: KeyboardSettings,
    onSettingsChange: (KeyboardSettings) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            SwitchSetting(
                title = "TalkBack Support",
                checked = settings.accessibilityMode,
                onCheckedChange = { onSettingsChange(settings.copy(accessibilityMode = it)) }
            )
        }

        item {
            SwitchSetting(
                title = "Large Keys",
                checked = settings.largeKeys,
                onCheckedChange = { onSettingsChange(settings.copy(largeKeys = it)) }
            )
        }

        item {
            SwitchSetting(
                title = "High Contrast Mode",
                checked = settings.highContrast,
                onCheckedChange = { onSettingsChange(settings.copy(highContrast = it)) }
            )
        }

        item {
            SwitchSetting(
                title = "Color Blind Mode",
                checked = settings.colorBlindMode,
                onCheckedChange = { onSettingsChange(settings.copy(colorBlindMode = it)) }
            )
        }
    }
}

@Composable
fun SwitchSetting(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFFFAA00),
                checkedTrackColor = Color(0xFFFFAA00).copy(alpha = 0.5f)
            )
        )
    }
}
