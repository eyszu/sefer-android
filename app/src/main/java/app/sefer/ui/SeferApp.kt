package app.sefer.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import app.sefer.R
import app.sefer.SeferViewModel
import app.sefer.Tab

@Composable
fun SeferApp(vm: SeferViewModel) {
    val palette = vm.palette

    // Keep status/navigation bar icons legible against the chosen theme, not the system theme.
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = palette.isLight
        controller.isAppearanceLightNavigationBars = palette.isLight
    }

    SeferTheme(palette) {
        Surface(Modifier.fillMaxSize(), color = palette.bg) {
            if (!vm.ready) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = palette.accent)
                }
                return@Surface
            }

            BackHandler(enabled = vm.showPicker) { vm.showPicker = false }
            BackHandler(enabled = !vm.showPicker && vm.selectedVerse != null && vm.tab == Tab.READ) {
                vm.selectedVerse = null
            }
            BackHandler(enabled = !vm.showPicker && vm.tab != Tab.READ) { vm.tab = Tab.READ }

            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    when (vm.tab) {
                        Tab.READ -> ReaderScreen(vm)
                        Tab.SEARCH -> SearchScreen(vm)
                        Tab.BOOKMARKS -> BookmarksScreen(vm)
                    }
                    if (vm.showPicker) BookPicker(vm)
                }
                NavigationBar(containerColor = palette.surface, contentColor = palette.text) {
                    val itemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = palette.accent,
                        selectedTextColor = palette.accent,
                        indicatorColor = palette.accent.copy(alpha = 0.16f),
                        unselectedIconColor = palette.muted,
                        unselectedTextColor = palette.muted,
                    )
                    NavigationBarItem(
                        selected = vm.tab == Tab.READ && !vm.showPicker,
                        onClick = { vm.showPicker = false; vm.tab = Tab.READ },
                        icon = { SeferIcon(R.drawable.ic_book, null) },
                        label = { Text("Read", fontSize = 12.sp) },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = vm.tab == Tab.SEARCH,
                        onClick = { vm.showPicker = false; vm.tab = Tab.SEARCH },
                        icon = { SeferIcon(R.drawable.ic_search, null) },
                        label = { Text("Search", fontSize = 12.sp) },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = vm.tab == Tab.BOOKMARKS,
                        onClick = { vm.showPicker = false; vm.tab = Tab.BOOKMARKS },
                        icon = { SeferIcon(R.drawable.ic_bookmark, null) },
                        label = { Text("Bookmarks", fontSize = 12.sp) },
                        colors = itemColors,
                    )
                }
            }

            if (vm.showAppearance) AppearanceSheet(vm)
        }
    }
}
