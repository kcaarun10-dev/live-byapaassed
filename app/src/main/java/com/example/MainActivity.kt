package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.repository.StreamedRepository
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.ArSportsTheme
import com.example.ui.theme.GlassMidnight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArSportsTheme {
                val repository = remember { StreamedRepository() }
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(GlassMidnight),
                    color = GlassMidnight
                ) {
                    AppNavigation(repository = repository)
                }
            }
        }
    }
}
