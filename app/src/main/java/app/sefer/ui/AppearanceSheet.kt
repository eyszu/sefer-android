package app.sefer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sefer.R
import app.sefer.SeferViewModel
import app.sefer.data.BibleVersion
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSheet(vm: SeferViewModel) {
    val palette = vm.palette
    ModalBottomSheet(
        onDismissRequest = { vm.showAppearance = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = palette.surface,
        contentColor = palette.text,
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
        ) {
            // ---- Version ----
            SectionLabel("Version", palette)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BibleVersion.entries.forEach { v ->
                    val selected = v == vm.version
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) palette.accent.copy(alpha = 0.16f) else palette.container)
                            .border(
                                BorderStroke(if (selected) 2.dp else 1.dp, if (selected) palette.accent else palette.outline),
                                RoundedCornerShape(14.dp),
                            )
                            .clickable { vm.selectVersion(v) }
                            .padding(14.dp),
                    ) {
                        Text(v.language, fontWeight = FontWeight.Bold, color = palette.text, fontSize = 16.sp)
                        Text(v.fullName, color = palette.muted, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }

            // ---- Theme ----
            SectionLabel("Theme", palette)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SeferPalette.entries.forEach { p ->
                    val selected = p == vm.palette
                    Column(
                        Modifier.weight(1f).clickable { vm.selectPalette(p) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(p.bg)
                                .border(
                                    BorderStroke(if (selected) 3.dp else 1.dp, if (selected) palette.accent else palette.outline),
                                    RoundedCornerShape(14.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Aa", color = p.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Box(
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp)
                                    .fillMaxWidth(0.4f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(p.accent),
                            )
                        }
                        Text(
                            p.label, color = if (selected) palette.accent else palette.muted,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }

            // ---- Font ----
            SectionLabel("Font", palette)
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(BorderStroke(1.dp, palette.outline), RoundedCornerShape(14.dp)),
            ) {
                SeferFont.entries.forEachIndexed { i, f ->
                    val selected = f == vm.font
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(if (selected) palette.accent.copy(alpha = 0.12f) else palette.surface)
                            .clickable { vm.selectFont(f) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            f.label,
                            Modifier.weight(1f),
                            style = TextStyle(
                                fontFamily = f.family, fontSize = 19.sp,
                                color = if (selected) palette.accent else palette.text,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            ),
                        )
                        if (f == SeferFont.LITERATA) {
                            Text("Default", color = palette.muted, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp))
                        }
                        if (selected) SeferIcon(R.drawable.ic_check, "Selected", palette.accent, size = 20.dp)
                    }
                    if (i < SeferFont.entries.size - 1) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(palette.outline.copy(alpha = 0.6f)))
                    }
                }
            }

            // ---- Text size ----
            SectionLabel("Text size  ·  ${vm.fontSize} sp", palette)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("A", color = palette.muted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = vm.fontSize.toFloat(),
                    onValueChange = { vm.selectFontSize(it.roundToInt()) },
                    valueRange = SeferViewModel.MIN_FONT_SIZE.toFloat()..SeferViewModel.MAX_FONT_SIZE.toFloat(),
                    steps = SeferViewModel.MAX_FONT_SIZE - SeferViewModel.MIN_FONT_SIZE - 1,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = palette.accent,
                        activeTrackColor = palette.accent,
                        inactiveTrackColor = palette.container,
                        activeTickColor = palette.accent,
                        inactiveTickColor = palette.outline,
                    ),
                )
                Text("A", color = palette.muted, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }

            // ---- Live preview ----
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.bg)
                    .border(BorderStroke(1.dp, palette.outline), RoundedCornerShape(14.dp))
                    .padding(16.dp),
            ) {
                Text(
                    if (vm.version == BibleVersion.TAGALOG) {
                        "Nang pasimula ay nilikha ng Dios ang langit at ang lupa."
                    } else {
                        "In the beginning God created the heavens and the earth."
                    },
                    style = TextStyle(
                        fontFamily = vm.font.family,
                        fontSize = vm.fontSize.sp,
                        lineHeight = (vm.fontSize * 1.65f).sp,
                        color = palette.text,
                    ),
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, palette: SeferPalette) {
    Text(
        text.uppercase(),
        color = palette.accent, fontWeight = FontWeight.Bold,
        fontSize = 12.sp, letterSpacing = 1.2.sp,
        modifier = Modifier.padding(top = 22.dp, bottom = 10.dp),
    )
}
