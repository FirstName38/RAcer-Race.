package com.example.ui.screens.theme

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.GlassCard
import com.example.ui.theme.AppFont
import com.example.ui.theme.AppTheme
import com.example.ui.theme.AppWallpaper
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseUrgent
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import com.example.ui.theme.parseHexColor
import com.example.ui.viewmodel.RacerViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeConfigScreen(
    viewModel: RacerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTheme by viewModel.currentAppTheme.collectAsState()
    val currentWallpaper by viewModel.currentAppWallpaper.collectAsState()
    val currentFont by viewModel.currentAppFont.collectAsState()
    val customWallpaperUri by viewModel.customWallpaperUri.collectAsState()
    val customAccentHex by viewModel.customAccentColorHex.collectAsState()
    val customScrimAlpha by viewModel.customWallpaperScrimAlpha.collectAsState()

    val currentAccentColor = remember(customAccentHex, currentTheme) {
        parseHexColor(customAccentHex) ?: currentTheme.accentColor
    }

    // Photo picker for custom gallery background
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setCustomWallpaperUri(uri.toString())
            viewModel.setAppWallpaper(AppWallpaper.CUSTOM_GALLERY)
            Toast.makeText(context, "Custom gallery wallpaper loaded!", Toast.LENGTH_SHORT).show()
        }
    }

    // Preset Accent Colors
    val presetAccents = listOf(
        Pair("Neon Cyan", "#06B6D4"),
        Pair("Electric Violet", "#8B5CF6"),
        Pair("Matrix Emerald", "#10B981"),
        Pair("Cyber Amber", "#F59E0B"),
        Pair("Sunset Rose", "#F43F5E"),
        Pair("Sakura Pink", "#EC4899"),
        Pair("Arctic Sky", "#38BDF8"),
        Pair("Royal Gold", "#FBBF24"),
        Pair("Solar Orange", "#F97316"),
        Pair("Deep Lavender", "#A78BFA"),
        Pair("Clean Silver", "#E2E8F0")
    )

    // RGB Custom sliders state
    var sliderR by remember { mutableFloatStateOf(currentAccentColor.red) }
    var sliderG by remember { mutableFloatStateOf(currentAccentColor.green) }
    var sliderB by remember { mutableFloatStateOf(currentAccentColor.blue) }
    var showRgbSliders by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Header with Back Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Theme & Aesthetics Studio",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Custom wallpapers, accent color palette & typography",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Real-Time Live Preview Mockup Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = currentAccentColor
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(currentAccentColor)
                            )
                            Text(
                                text = "LIVE UI PREVIEW",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = currentAccentColor
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = currentAccentColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, currentAccentColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "${currentTheme.displayName} • ${currentFont.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = currentAccentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = "Focus Flow Cockpit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Dopamine shield active • 25-minute Pomodoro cycle engaged",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    // Accent Progress Bar Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CharcoalBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.68f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(currentAccentColor)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {},
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = currentAccentColor, contentColor = Color.Black),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Primary Action", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {},
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, currentAccentColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Secondary", color = currentAccentColor, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // SECTION 1: CUSTOM GALLERY WALLPAPER UPLOAD
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = currentAccentColor, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Custom Gallery Wallpaper",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        if (currentWallpaper == AppWallpaper.CUSTOM_GALLERY && !customWallpaperUri.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldSuccess.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Upload any wallpaper, photo, or art from your device's gallery. The app automatically applies a high-contrast dark scrim so your tasks and timers remain clearly legible.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    if (!customWallpaperUri.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(CharcoalCardElevated)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = customWallpaperUri,
                                contentDescription = "Active Custom Wallpaper",
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, currentAccentColor, RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Custom Image Active", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text("High-resolution gallery backdrop", fontSize = 11.sp, color = TextMuted)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (currentWallpaper != AppWallpaper.CUSTOM_GALLERY) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = currentAccentColor.copy(alpha = 0.2f),
                                            modifier = Modifier.clickable {
                                                viewModel.setAppWallpaper(AppWallpaper.CUSTOM_GALLERY)
                                            }
                                        ) {
                                            Text("Set Active", color = currentAccentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = RoseUrgent.copy(alpha = 0.2f),
                                        modifier = Modifier.clickable {
                                            viewModel.setCustomWallpaperUri(null)
                                            if (currentWallpaper == AppWallpaper.CUSTOM_GALLERY) {
                                                viewModel.setAppWallpaper(AppWallpaper.AURORA_WAVES)
                                            }
                                            Toast.makeText(context, "Custom wallpaper removed", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("Remove", color = RoseUrgent, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                }
                            }
                        }

                        // Scrim Opacity Slider
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Dark Scrim Opacity (Readability Protection)", fontSize = 11.sp, color = TextSecondary)
                                Text("${(customScrimAlpha * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = currentAccentColor)
                            }
                            Slider(
                                value = customScrimAlpha,
                                onValueChange = { viewModel.setCustomWallpaperScrim(it) },
                                valueRange = 0.30f..0.90f,
                                colors = SliderDefaults.colors(
                                    thumbColor = currentAccentColor,
                                    activeTrackColor = currentAccentColor,
                                    inactiveTrackColor = CharcoalBorder
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (customWallpaperUri.isNullOrBlank()) "Choose Image from Gallery" else "Replace Gallery Image", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // SECTION 2: ACCENT COLOR PICKER
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.ColorLens, contentDescription = null, tint = currentAccentColor, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Accent Color Palette",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        if (customAccentHex != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CharcoalCardElevated,
                                modifier = Modifier.clickable {
                                    viewModel.setCustomAccentColor(null)
                                    Toast.makeText(context, "Accent reset to theme default", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                                    Text("Reset Default", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }
                    }

                    Text(
                        text = "Customize the primary energetic highlight for focus buttons, radar rings, streak flames, and active badges.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    // Preset Color Swatches
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        presetAccents.forEach { (name, hex) ->
                            val color = parseHexColor(hex) ?: Color.White
                            val isSelected = customAccentHex.equals(hex, ignoreCase = true) || (customAccentHex == null && currentTheme.accentColor == color)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) color.copy(alpha = 0.25f) else CharcoalCardElevated,
                                border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) color else CharcoalBorder),
                                modifier = Modifier.clickable {
                                    viewModel.setCustomAccentColor(hex)
                                    sliderR = color.red
                                    sliderG = color.green
                                    sliderB = color.blue
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Custom RGB Sliders Toggle
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CharcoalCardElevated,
                        border = BorderStroke(1.dp, CharcoalBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRgbSliders = !showRgbSliders }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = currentAccentColor, modifier = Modifier.size(18.dp))
                                Text("Custom RGB Color Mixer", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Text(
                                text = if (showRgbSliders) "Hide" else "Customize",
                                fontSize = 11.sp,
                                color = currentAccentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (showRgbSliders) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CharcoalCardElevated)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val computedColor = Color(sliderR, sliderG, sliderB)
                            val rInt = (sliderR * 255).toInt()
                            val gInt = (sliderG * 255).toInt()
                            val bInt = (sliderB * 255).toInt()
                            val computedHex = "#%02X%02X%02X".format(rInt, gInt, bInt)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(computedColor)
                                            .border(1.dp, Color.White, CircleShape)
                                    )
                                    Text("Color Preview: $computedHex", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                                }
                                Button(
                                    onClick = {
                                        viewModel.setCustomAccentColor(computedHex)
                                        Toast.makeText(context, "Applied custom accent $computedHex", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = computedColor, contentColor = Color.Black)
                                ) {
                                    Text("Apply", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Red Slider
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("R", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), modifier = Modifier.width(20.dp))
                                Slider(
                                    value = sliderR,
                                    onValueChange = { sliderR = it },
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFFEF4444), activeTrackColor = Color(0xFFEF4444))
                                )
                                Text("$rInt", fontSize = 11.sp, color = TextMuted, modifier = Modifier.width(28.dp))
                            }

                            // Green Slider
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("G", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), modifier = Modifier.width(20.dp))
                                Slider(
                                    value = sliderG,
                                    onValueChange = { sliderG = it },
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF10B981), activeTrackColor = Color(0xFF10B981))
                                )
                                Text("$gInt", fontSize = 11.sp, color = TextMuted, modifier = Modifier.width(28.dp))
                            }

                            // Blue Slider
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("B", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6), modifier = Modifier.width(20.dp))
                                Slider(
                                    value = sliderB,
                                    onValueChange = { sliderB = it },
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF3B82F6), activeTrackColor = Color(0xFF3B82F6))
                                )
                                Text("$bInt", fontSize = 11.sp, color = TextMuted, modifier = Modifier.width(28.dp))
                            }
                        }
                    }
                }
            }
        }

        // SECTION 3: CUSTOM TYPOGRAPHY SELECTION
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.FontDownload, contentDescription = null, tint = currentAccentColor, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Custom Typography",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "Transform how numbers, headings, and task titles render throughout the interface.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    AppFont.entries.forEach { fontOption ->
                        val isSelected = currentFont == fontOption
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) currentAccentColor.copy(alpha = 0.18f) else CharcoalCardElevated,
                            border = BorderStroke(1.dp, if (isSelected) currentAccentColor else CharcoalBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAppFont(fontOption)
                                    Toast.makeText(context, "Font switched to ${fontOption.displayName}", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fontOption.displayName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = fontOption.fontFamily),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) currentAccentColor else TextPrimary
                                    )
                                    Text(
                                        text = fontOption.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Focused velocity 09:45 AM • Flow state",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = fontOption.fontFamily),
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = currentAccentColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION 4: CURATED THEME PALETTES (12 THEMES)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = currentAccentColor, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Preset Color Themes (12 Options)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "Curated ambient palettes engineered for late-night deep work and distraction reduction.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppTheme.entries.forEach { themeOption ->
                            val isSelected = currentTheme == themeOption
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) currentAccentColor.copy(alpha = 0.2f) else CharcoalCardElevated,
                                border = BorderStroke(1.dp, if (isSelected) currentAccentColor else CharcoalBorder),
                                modifier = Modifier
                                    .width(160.dp)
                                    .clickable {
                                        viewModel.setAppTheme(themeOption)
                                        Toast.makeText(context, "${themeOption.displayName} activated", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(themeOption.accentColor)
                                            .border(1.dp, themeOption.previewColor, CircleShape)
                                    )
                                    Text(
                                        text = themeOption.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) currentAccentColor else TextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION 5: WALLPAPER EFFECTS
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = currentAccentColor, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Wallpaper Ambience & Effects",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    AppWallpaper.entries.forEach { wallpaperOption ->
                        val isSelected = currentWallpaper == wallpaperOption
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) currentAccentColor.copy(alpha = 0.18f) else CharcoalCardElevated,
                            border = BorderStroke(1.dp, if (isSelected) currentAccentColor else CharcoalBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (wallpaperOption == AppWallpaper.CUSTOM_GALLERY && customWallpaperUri.isNullOrBlank()) {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    } else {
                                        viewModel.setAppWallpaper(wallpaperOption)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = wallpaperOption.displayName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) currentAccentColor else TextPrimary
                                        )
                                        if (wallpaperOption == AppWallpaper.MATRIX_RAIN || wallpaperOption == AppWallpaper.COSMIC_DUST) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = CyanAccent.copy(alpha = 0.2f)
                                            ) {
                                                Text("ANIMATED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyanAccent, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                    Text(
                                        text = wallpaperOption.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = currentAccentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
