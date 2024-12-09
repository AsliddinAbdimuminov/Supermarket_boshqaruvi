package com.mobile_programming.sari_sari_inventory_app

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.mobile_programming.sari_sari_inventory_app.databinding.ActivityMainBinding
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    private val viewModel: MainActivityViewModel by viewModels {
        AppViewModelProvider.Factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            viewModel.collectDarkModePreference().collect {
                updateTheme(it)
            }
        }

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = binding.mainNavHostFragment.getFragment<NavHostFragment>()
        navController = navHostFragment.navController
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun updateTheme(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
        } else {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }
}