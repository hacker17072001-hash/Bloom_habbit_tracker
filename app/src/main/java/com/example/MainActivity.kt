package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.BloomDatabase
import com.example.data.repository.BloomRepository
import com.example.ui.BloomApp
import com.example.ui.viewmodel.BloomViewModel
import com.example.ui.viewmodel.BloomViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = BloomDatabase.getDatabase(applicationContext)
        val repository = BloomRepository(database)
        val factory = BloomViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[BloomViewModel::class.java]

        setContent {
            BloomApp(viewModel = viewModel)
        }
    }
}
