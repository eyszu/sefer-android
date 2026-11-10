package app.sefer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sefer.R
import app.sefer.SeferViewModel

@Composable
fun SearchScreen(vm: SeferViewModel) {
    val palette = vm.palette
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val reference = vm.referenceFor(vm.query)
    val terms = vm.resultQuery.split(Regex("\\s+")).filter { it.isNotEmpty() }

    Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
        OutlinedTextField(
            value = vm.query,
            onValueChange = vm::onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .focusRequester(focusRequester),
            placeholder = { Text("Search words or a verse (Daniel 7:25)", color = palette.muted) },
            leadingIcon = { SeferIcon(R.drawable.ic_search, null, palette.muted) },
            trailingIcon = {
                if (vm.query.isNotEmpty()) {
                    IconButton(onClick = { vm.onQueryChange("") }) {
                        SeferIcon(R.drawable.ic_close, "Clear", palette.muted)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        )

        val trimmed = vm.query.trim()
        when {
            trimmed.length < 2 && reference == null -> EmptyState(
                palette, "Search ${vm.version.language}",
                "Type a word or phrase, or jump straight to a passage, e.g. “Psalm 23” or “Gen 1:3”.",
            )
            else -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                if (reference != null) {
                    item(key = "ref") {
                        val label = buildString {
                            append(vm.bookName(reference.book)).append(' ').append(reference.chapter)
                            if (reference.verse != null) append(':').append(reference.verse)
                        }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(palette.accent.copy(alpha = 0.14f))
                                .clickable { focusManager.clearFocus(); vm.open(reference) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SeferIcon(R.drawable.ic_book, null, palette.accent)
                            Text(
                                "Go to $label",
                                Modifier.weight(1f).padding(start = 12.dp),
                                color = palette.text, fontWeight = FontWeight.SemiBold,
                            )
                            SeferIcon(R.drawable.ic_chevron_right, null, palette.accent)
                        }
                    }
                }
                if (vm.searching && vm.results.isEmpty()) {
                    item(key = "spinner") {
                        Row(Modifier.fillMaxWidth().padding(32.dp), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(color = palette.accent)
                        }
                    }
                } else if (vm.results.isEmpty() && trimmed.length >= 2) {
                    item(key = "none") {
                        Text(
                            "No verses found for “$trimmed” in ${vm.version.language}.",
                            color = palette.muted,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                        )
                    }
                } else if (vm.results.isNotEmpty()) {
                    item(key = "count") {
                        val n = vm.results.size
                        val capped = n >= SeferViewModel.MAX_RESULTS
                        Text(
                            if (capped) "$n+ verses (showing first $n)" else if (n == 1) "1 verse" else "$n verses",
                            color = palette.muted, fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                        )
                    }
                    items(vm.results, key = { "${it.book}:${it.chapter}:${it.verse}" }) { v ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clickable { focusManager.clearFocus(); vm.openChapter(v.book, v.chapter, v.verse) }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                        ) {
                            Text(
                                "${vm.bookName(v.book)} ${v.chapter}:${v.verse}",
                                color = palette.accent, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                            )
                            Text(
                                highlightTerms(v.text, terms, palette.text, palette.accent.copy(alpha = 0.28f)),
                                style = TextStyle(
                                    fontFamily = vm.font.family, fontSize = 16.sp, lineHeight = 24.sp,
                                    color = palette.text,
                                ),
                                maxLines = 4, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        HorizontalDivider(color = palette.outline.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 20.dp))
                    }
                }
            }
        }
    }
}
