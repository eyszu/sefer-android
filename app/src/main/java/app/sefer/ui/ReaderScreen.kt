package app.sefer.ui

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sefer.R
import app.sefer.SeferViewModel
import app.sefer.data.BibleVersion

@Composable
fun ReaderScreen(vm: SeferViewModel) {
    val palette = vm.palette
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val swipeThreshold = with(LocalDensity.current) { 80.dp.toPx() }

    // Scroll to the requested verse (item 0 is the chapter heading) or back to the top.
    LaunchedEffect(vm.scrollTarget) {
        val target = vm.scrollTarget ?: return@LaunchedEffect
        val index = target.verse
            ?.let { v -> vm.verses.indexOfFirst { it.verse == v } }
            ?.let { if (it >= 0) it + 1 else 0 }
            ?: 0
        listState.scrollToItem(index)
    }

    val verseStyle = TextStyle(
        fontFamily = vm.font.family,
        fontSize = vm.fontSize.sp,
        lineHeight = (vm.fontSize * vm.lineSpacing / 100f).sp,
        color = palette.text,
    )

    Column(Modifier.fillMaxSize()) {
        ReaderTopBar(vm)
        Box(
            Modifier
                .weight(1f)
                // Swipe left -> next chapter, swipe right -> previous chapter.
                .pointerInput(Unit) {
                    var total = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { total = 0f },
                        onDragCancel = { total = 0f },
                        onDragEnd = {
                            if (total <= -swipeThreshold) {
                                vm.nextChapterRef?.let { vm.openChapter(it.first, it.second) }
                            } else if (total >= swipeThreshold) {
                                vm.previousChapterRef?.let { vm.openChapter(it.first, it.second) }
                            }
                            total = 0f
                        },
                        onHorizontalDrag = { _, dx -> total += dx },
                    )
                },
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            ) {
                item(key = "heading") {
                    Column(
                        Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = vm.bookName(),
                            style = TextStyle(
                                fontFamily = vm.font.family,
                                fontWeight = FontWeight.Bold,
                                fontSize = (vm.fontSize * 1.5f).sp,
                                color = palette.text,
                            ),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "Chapter ${vm.chapter}",
                            style = TextStyle(
                                fontFamily = vm.font.family,
                                fontSize = (vm.fontSize * 0.85f).sp,
                                color = palette.accent,
                            ),
                        )
                    }
                }
                items(vm.verses, key = { it.verse }) { v ->
                    val selected = vm.selectedVerse == v.verse
                    val bookmarked = vm.isBookmarked(v.book, v.chapter, v.verse)
                    val background = when {
                        selected -> palette.accent.copy(alpha = 0.22f)
                        bookmarked -> palette.accent.copy(alpha = 0.09f)
                        else -> androidx.compose.ui.graphics.Color.Transparent
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(background)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                vm.selectedVerse = if (selected) null else v.verse
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        color = palette.accent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = (vm.fontSize * 0.62f).sp,
                                        baselineShift = BaselineShift.Superscript,
                                    ),
                                ) { append(v.verse.toString()) }
                                append(" ")
                                append(v.text)
                            },
                            style = verseStyle,
                            modifier = Modifier.weight(1f),
                        )
                        if (bookmarked) {
                            SeferIcon(
                                R.drawable.ic_bookmark, null, palette.accent,
                                Modifier.padding(start = 6.dp, top = 4.dp), size = 16.dp,
                            )
                        }
                    }
                }
                item(key = "footer") { ChapterNav(vm) }
            }

            val selectedNumber = vm.selectedVerse
            val selectedVerse = vm.verses.firstOrNull { it.verse == selectedNumber }
            if (selectedVerse != null) {
                val reference = "${vm.bookName()} ${selectedVerse.chapter}:${selectedVerse.verse}"
                val shareText = "“${selectedVerse.text}”\n— $reference (${vm.version.abbr})"
                val bookmarked = vm.isBookmarked(selectedVerse.book, selectedVerse.chapter, selectedVerse.verse)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = palette.container,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, palette.outline),
                ) {
                    Row(
                        Modifier.padding(start = 20.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            reference,
                            color = palette.text,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = {
                            vm.toggleBookmark(selectedVerse.book, selectedVerse.chapter, selectedVerse.verse)
                        }) {
                            SeferIcon(
                                if (bookmarked) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border,
                                if (bookmarked) "Remove bookmark" else "Add bookmark",
                                palette.accent,
                            )
                        }
                        IconButton(onClick = { clipboard.setText(AnnotatedString(shareText)) }) {
                            SeferIcon(R.drawable.ic_copy, "Copy verse", palette.text)
                        }
                        IconButton(onClick = {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(send, null))
                        }) {
                            SeferIcon(R.drawable.ic_share, "Share verse", palette.text)
                        }
                        IconButton(onClick = { vm.selectedVerse = null }) {
                            SeferIcon(R.drawable.ic_close, "Dismiss", palette.muted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderTopBar(vm: SeferViewModel) {
    val palette = vm.palette
    var versionMenu by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { vm.showPicker = true }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${vm.bookName()} ${vm.chapter}",
                style = TextStyle(
                    fontFamily = vm.font.family,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = palette.text,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            SeferIcon(R.drawable.ic_expand_more, "Choose book and chapter", palette.muted)
        }
        Box {
            Surface(
                onClick = { versionMenu = true },
                shape = RoundedCornerShape(50),
                color = palette.container,
            ) {
                Text(
                    vm.version.abbr,
                    color = palette.accent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
            DropdownMenu(expanded = versionMenu, onDismissRequest = { versionMenu = false }) {
                BibleVersion.entries.forEach { v ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(v.language, fontWeight = FontWeight.SemiBold, color = palette.text)
                                Text(v.fullName, fontSize = 12.sp, color = palette.muted)
                            }
                        },
                        trailingIcon = {
                            if (v == vm.version) SeferIcon(R.drawable.ic_check, null, palette.accent, size = 20.dp)
                        },
                        onClick = { vm.selectVersion(v); versionMenu = false },
                    )
                }
            }
        }
        IconButton(onClick = { vm.showAppearance = true }) {
            Text("Aa", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }
    }
}

@Composable
private fun ChapterNav(vm: SeferViewModel) {
    val palette = vm.palette
    val prev = vm.previousChapterRef
    val next = vm.nextChapterRef
    Row(
        Modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (prev != null) {
            OutlinedButton(
                onClick = { vm.openChapter(prev.first, prev.second) },
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, palette.outline),
            ) {
                SeferIcon(R.drawable.ic_chevron_left, null, palette.accent, size = 20.dp)
                Text(
                    "${vm.bookName(prev.first)} ${prev.second}",
                    color = palette.text, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        } else Spacer(Modifier.weight(1f))
        if (next != null) {
            OutlinedButton(
                onClick = { vm.openChapter(next.first, next.second) },
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, palette.outline),
            ) {
                Text(
                    "${vm.bookName(next.first)} ${next.second}",
                    color = palette.text, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                SeferIcon(R.drawable.ic_chevron_right, null, palette.accent, size = 20.dp)
            }
        } else Spacer(Modifier.weight(1f))
    }
}
