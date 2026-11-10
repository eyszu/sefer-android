package app.sefer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sefer.R
import app.sefer.SeferViewModel
import app.sefer.data.BibleVersion

/** Full-screen book list; tap a book to expand its chapter grid. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookPicker(vm: SeferViewModel) {
    val palette = vm.palette
    val books = vm.bookIds
    var expanded by remember { mutableIntStateOf(vm.book) }
    val listState = rememberLazyListState()
    val tagalog = vm.version == BibleVersion.TAGALOG

    LaunchedEffect(Unit) {
        // Rows are [OT header, books..., NT header, books...]; find the current book's row.
        val idx = books.indexOf(vm.book)
        if (idx >= 0) {
            val headerBefore = if (vm.book <= 39) 1 else 2
            listState.scrollToItem((idx + headerBefore - 1).coerceAtLeast(0))
        }
    }

    Surface(Modifier.fillMaxSize(), color = palette.bg) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(start = 20.dp, end = 4.dp).padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Books",
                    Modifier.weight(1f),
                    color = palette.text, fontWeight = FontWeight.Bold, fontSize = 22.sp,
                )
                IconButton(onClick = { vm.showPicker = false }) {
                    SeferIcon(R.drawable.ic_close, "Close", palette.text)
                }
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                // Flatten into rows so headers and books share one lazy list.
                val rows = buildList<Any> {
                    var otHeader = false
                    var ntHeader = false
                    for (b in books) {
                        if (b <= 39 && !otHeader) { add(if (tagalog) "Hebrew Bible" else "Hebrew Bible"); otHeader = true }
                        add(b)
                    }
                }
                itemsIndexed(rows) { _, row ->
                    if (row is String) {
                        Text(
                            row.uppercase(),
                            color = palette.accent, fontWeight = FontWeight.Bold,
                            fontSize = 12.sp, letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 6.dp),
                        )
                    } else {
                        val b = row as Int
                        val isExpanded = expanded == b
                        val count = vm.chapterCounts[b] ?: 1
                        Column(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = if (isExpanded) -1 else b }
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    vm.bookName(b),
                                    Modifier.weight(1f),
                                    color = if (b == vm.book) palette.accent else palette.text,
                                    fontWeight = if (b == vm.book) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 17.sp,
                                )
                                Text("$count", color = palette.muted, fontSize = 13.sp)
                            }
                            if (isExpanded) {
                                FlowRow(
                                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    for (c in 1..count) {
                                        val current = b == vm.book && c == vm.chapter
                                        Box(
                                            Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (current) palette.accent else palette.container)
                                                .clickable { vm.openChapter(b, c) },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                "$c",
                                                color = if (current) palette.bg else palette.text,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 15.sp,
                                                textAlign = TextAlign.Center,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
