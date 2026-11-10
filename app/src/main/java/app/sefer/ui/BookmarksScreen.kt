package app.sefer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sefer.R
import app.sefer.SeferViewModel

@Composable
fun BookmarksScreen(vm: SeferViewModel) {
    val palette = vm.palette
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Text(
            "Bookmarks",
            color = palette.text, fontWeight = FontWeight.Bold, fontSize = 22.sp,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
        )
        if (vm.bookmarkItems.isEmpty()) {
            EmptyState(
                palette, "No bookmarks yet",
                "Tap any verse while reading, then tap the bookmark icon to save it here.",
            )
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
                items(vm.bookmarkItems, key = { with(it.bookmark) { "$book:$chapter:$verse" } }) { item ->
                    val bm = item.bookmark
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { vm.openChapter(bm.book, bm.chapter, bm.verse) }
                            .padding(start = 20.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(Modifier.weight(1f).padding(top = 4.dp)) {
                            Text(
                                "${vm.bookName(bm.book)} ${bm.chapter}:${bm.verse}",
                                color = palette.accent, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                            )
                            Text(
                                item.text,
                                style = TextStyle(
                                    fontFamily = vm.font.family, fontSize = 16.sp, lineHeight = 24.sp,
                                    color = palette.text,
                                ),
                                maxLines = 3, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        IconButton(onClick = { vm.toggleBookmark(bm.book, bm.chapter, bm.verse) }) {
                            SeferIcon(R.drawable.ic_close, "Remove bookmark", palette.muted, size = 20.dp)
                        }
                    }
                    HorizontalDivider(
                        color = palette.outline.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(palette: SeferPalette, title: String, body: String) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SeferIcon(R.drawable.ic_bookmark_border, null, palette.muted.copy(alpha = 0.6f), size = 40.dp)
        Text(
            title, color = palette.text, fontWeight = FontWeight.SemiBold, fontSize = 17.sp,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            body, color = palette.muted, fontSize = 14.sp, lineHeight = 21.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
