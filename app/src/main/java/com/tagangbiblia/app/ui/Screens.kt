package com.tagangbiblia.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tagangbiblia.app.BibleViewModel
import com.tagangbiblia.app.Screen
import com.tagangbiblia.app.ThemeMode
import com.tagangbiblia.app.data.Verse
import kotlin.math.roundToInt

private val Scripture = FontFamily.Serif

// ─────────────────────────────── root ───────────────────────────────

@Composable
fun BibleApp(vm: BibleViewModel) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when {
            vm.error != null -> Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
                Text(
                    "Could not open the Bible database:\n${vm.error}",
                    color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center
                )
            }
            !vm.ready -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            else -> {
                BackHandler(enabled = vm.screen != Screen.Reader || vm.selected.isNotEmpty()) {
                    when {
                        vm.screen == Screen.Chapters -> vm.screen = Screen.Books
                        vm.screen != Screen.Reader -> vm.screen = Screen.Reader
                        else -> vm.clearSelection()
                    }
                }
                when (vm.screen) {
                    Screen.Reader -> ReaderScreen(vm)
                    Screen.Books -> BooksScreen(vm)
                    Screen.Chapters -> ChaptersScreen(vm)
                    Screen.Bookmarks -> BookmarksScreen(vm)
                }
            }
        }
    }
}

// ─────────────────────────────── reader ───────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(vm: BibleViewModel) {
    val ctx = LocalContext.current
    var showSettings by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val selecting = vm.selected.isNotEmpty()

    // After each chapter load: jump to the requested verse, or back to the top.
    LaunchedEffect(vm.loadId) {
        val target = vm.scrollToVerse
        val index = if (target != null) {
            val i = vm.verses.indexOfFirst { it.number == target }
            if (i >= 0) i + 1 else 0   // +1 for the header item
        } else 0
        listState.scrollToItem(index)
        vm.scrollToVerse = null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (selecting) {
                TopAppBar(
                    title = { Text("${vm.selected.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = vm::clearSelection) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            copyToClipboard(ctx, vm.selectionAsText())
                            vm.clearSelection()
                        }) { Icon(Icons.Filled.ContentCopy, contentDescription = "Copy verse") }
                        val allMarked = vm.selectionAllBookmarked
                        IconButton(onClick = vm::toggleBookmarkOnSelection) {
                            Icon(
                                if (allMarked) Icons.Filled.BookmarkRemove else Icons.Filled.BookmarkAdd,
                                contentDescription = if (allMarked) "Remove bookmark" else "Bookmark verse"
                            )
                        }
                        IconButton(onClick = {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, vm.selectionAsText())
                            }
                            ctx.startActivity(Intent.createChooser(send, "Share verse"))
                        }) { Icon(Icons.Filled.Share, contentDescription = "Share verse") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Row(
                            Modifier.clip(RoundedCornerShape(8.dp))
                                .clickable { vm.screen = Screen.Books }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(vm.currentTitle, fontFamily = Scripture, fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Choose book")
                        }
                    },
                    actions = {
                        IconButton(onClick = { vm.screen = Screen.Bookmarks }) {
                            Icon(Icons.Filled.Bookmarks, contentDescription = "Bookmarks")
                        }
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Reading settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                IconButton(onClick = vm::prevChapter, enabled = vm.hasPrev()) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous chapter")
                }
                Text(
                    vm.currentTitle,
                    Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = vm::nextChapter, enabled = vm.hasNext()) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next chapter")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 14.dp, end = 14.dp,
                top = padding.calculateTopPadding() + 4.dp,
                bottom = padding.calculateBottomPadding() + 24.dp
            )
        ) {
            item(key = "header") {
                Column(Modifier.fillMaxWidth().padding(bottom = 12.dp, top = 4.dp, start = 10.dp)) {
                    Text(
                        vm.currentBook?.displayName.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = Scripture
                    )
                    Text(
                        "Chapter ${vm.chapter}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = Scripture, fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Tap a verse to copy or bookmark it",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(vm.verses, key = { it.number }) { verse ->
                VerseRow(
                    verse = verse,
                    fontSize = vm.fontSize,
                    selected = verse.number in vm.selected,
                    bookmarked = vm.isBookmarked(verse.number),
                    onClick = { vm.toggleSelect(verse.number) }
                )
            }
            item(key = "footer") {
                Text(
                    "Ang Biblia · Philippine Bible Society (1905) · Public Domain",
                    Modifier.fillMaxWidth().padding(top = 24.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showSettings) SettingsSheet(vm) { showSettings = false }
}

@Composable
private fun VerseRow(
    verse: Verse, fontSize: Float, selected: Boolean, bookmarked: Boolean, onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val accent = cs.secondary
    val text = buildAnnotatedString {
        withStyle(
            SpanStyle(
                fontSize = (fontSize * 0.6f).sp,
                fontWeight = FontWeight.Bold,
                color = cs.primary,
                baselineShift = BaselineShift.Superscript
            )
        ) { append(verse.number.toString()) }
        append(" ")
        append(verse.text)
    }
    Text(
        text = text,
        color = cs.onBackground,
        fontFamily = Scripture,
        fontSize = fontSize.sp,
        lineHeight = (fontSize * 1.55f).sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) cs.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .drawBehind {
                if (bookmarked) drawRect(accent, size = Size(3.dp.toPx(), size.height))
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

private fun copyToClipboard(ctx: Context, text: String) {
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Bible verse", text))
    Toast.makeText(ctx, "Copied", Toast.LENGTH_SHORT).show()
}

// ─────────────────────────────── settings ───────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(vm: BibleViewModel, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(Modifier.padding(horizontal = 24.dp).navigationBarsPadding().padding(bottom = 24.dp)) {
            Text("Reading settings", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Text size", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text("${vm.fontSize.roundToInt()} sp", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("A", fontSize = 14.sp, fontFamily = Scripture)
                Slider(
                    value = vm.fontSize,
                    onValueChange = vm::updateFontSize,
                    valueRange = BibleViewModel.MIN_FONT..BibleViewModel.MAX_FONT,
                    steps = 12, // 2 sp increments
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                )
                Text("A", fontSize = 30.sp, fontFamily = Scripture)
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(
                    "Nang pasimula ay nilikha ng Dios ang langit at ang lupa.",
                    Modifier.padding(16.dp),
                    fontFamily = Scripture,
                    fontSize = vm.fontSize.sp,
                    lineHeight = (vm.fontSize * 1.55f).sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(24.dp))
            Text("Theme", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeChip("System", Icons.Filled.BrightnessAuto, ThemeMode.SYSTEM, vm)
                ThemeChip("Light", Icons.Filled.LightMode, ThemeMode.LIGHT, vm)
                ThemeChip("Dark", Icons.Filled.DarkMode, ThemeMode.DARK, vm)
            }
        }
    }
}

@Composable
private fun ThemeChip(label: String, icon: ImageVector, mode: ThemeMode, vm: BibleViewModel) {
    FilterChip(
        selected = vm.themeMode == mode,
        onClick = { vm.updateTheme(mode) },
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) }
    )
}

// ─────────────────────────────── book / chapter pickers ───────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(vm: BibleViewModel) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Books") },
                navigationIcon = {
                    IconButton(onClick = { vm.screen = Screen.Reader }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize(), contentPadding = padding) {
            items(vm.books, key = { it.id }) { book ->
                val current = book.id == vm.bookId
                ListItem(
                    headlineContent = {
                        Text(book.displayName, fontFamily = Scripture, fontSize = 18.sp,
                            fontWeight = if (current) FontWeight.Bold else FontWeight.Normal)
                    },
                    supportingContent = { Text("${vm.chapterCounts[book.id] ?: 0} chapters") },
                    colors = ListItemDefaults.colors(
                        containerColor = if (current) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent
                    ),
                    modifier = Modifier.clickable {
                        vm.pickerBook = book
                        vm.screen = Screen.Chapters
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(vm: BibleViewModel) {
    val book = vm.pickerBook ?: vm.currentBook ?: return
    val count = vm.chapterCounts[book.id] ?: 0
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(book.displayName, fontFamily = Scripture) },
                navigationIcon = {
                    IconButton(onClick = { vm.screen = Screen.Books }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(64.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items((1..count).toList()) { ch ->
                val current = book.id == vm.bookId && ch == vm.chapter
                Surface(
                    onClick = { vm.openChapter(book.id, ch) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (current) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if (current) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.height(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(ch.toString(), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────── bookmarks ───────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(vm: BibleViewModel) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Bookmarks") },
                navigationIcon = {
                    IconButton(onClick = { vm.screen = Screen.Reader }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (vm.bookmarks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), Alignment.Center) {
                Text(
                    "No bookmarks yet.\nTap a verse while reading, then choose the bookmark button.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = padding) {
                items(vm.bookmarks, key = { "${it.bookId}:${it.chapter}:${it.verse}" }) { bm ->
                    ListItem(
                        headlineContent = {
                            Text(
                                "${vm.bookName(bm.bookId)} ${bm.chapter}:${bm.verse}",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        supportingContent = {
                            Text(bm.text, maxLines = 3, overflow = TextOverflow.Ellipsis, fontFamily = Scripture)
                        },
                        trailingContent = {
                            IconButton(onClick = { vm.removeBookmark(bm) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove bookmark")
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { vm.openChapter(bm.bookId, bm.chapter, bm.verse) }
                    )
                }
            }
        }
    }
}
