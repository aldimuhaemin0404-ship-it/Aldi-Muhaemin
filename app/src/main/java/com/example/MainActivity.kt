package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.KasHarianDatabase
import com.example.data.repository.KasHarianRepository
import com.example.ui.KasHarianApp
import com.example.ui.KasHarianViewModel
import com.example.ui.theme.KasHarianTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = KasHarianDatabase.getInstance(applicationContext)
        val repository = KasHarianRepository(database.kasHarianDao())

        setContent {
            KasHarianTheme {
                val vm: KasHarianViewModel = viewModel(
                    factory = KasHarianViewModel.factory(repository)
                )
                KasHarianApp(viewModel = vm)
            }
        }
    }
}
