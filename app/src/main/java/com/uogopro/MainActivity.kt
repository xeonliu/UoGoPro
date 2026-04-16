package com.uogopro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.uogopro.ui.AppRoot
import com.uogopro.ui.UoGoProTheme
import com.uogopro.viewmodel.CameraViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UoGoProTheme {
                AppRoot(viewModel = viewModel)
            }
        }
    }
}
