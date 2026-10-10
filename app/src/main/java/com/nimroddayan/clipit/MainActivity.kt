package com.nimroddayan.clipit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import com.nimroddayan.clipit.ui.theme.ClipItTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val destination = intent.getStringExtra("destination")
            ClipItTheme { App(application as CouponApplication, destination) }
        }
    }
}


