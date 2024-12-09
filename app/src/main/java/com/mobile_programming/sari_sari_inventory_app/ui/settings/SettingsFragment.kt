package com.mobile_programming.sari_sari_inventory_app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.mobile_programming.sari_sari_inventory_app.databinding.FragmentSettingsBinding
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private val viewModel: SettingsViewModel by viewModels { AppViewModelProvider.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSettingsBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        navController = findNavController(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.darkModeThemeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleDarkTheme(isChecked)
        }

        binding.apply {
            binding.topAppToolbar.setNavigationOnClickListener {
                navController.popBackStack()
            }
        }

        viewModel.uiState.asLiveData().observe(
            viewLifecycleOwner
        ) { uiState ->
            binding.darkModeThemeSwitch.isChecked = uiState.isDarkModeEnabled
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding.apply {
            darkModeThemeSwitch.setOnCheckedChangeListener(null)
            lifecycleOwner = null
        }

        viewModel.uiState.asLiveData().removeObservers(this)
        _binding = null
    }
}