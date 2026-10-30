package com.tagangbiblia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.tagangbiblia.app.ui.BibleApp
import com.tagangbiblia.app.ui.BibleTheme

class MainActivity : ComponentActivity() {
    private val vm: BibleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BibleTheme(vm.themeMode) { BibleApp(vm) }
        }
    }
}
