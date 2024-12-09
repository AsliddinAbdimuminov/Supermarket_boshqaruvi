package com.mobile_programming.sari_sari_inventory_app.ui.home

import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.mobile_programming.sari_sari_inventory_app.MainActivity
import com.mobile_programming.sari_sari_inventory_app.R
import com.mobile_programming.sari_sari_inventory_app.databinding.FragmentHomeBinding
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.utils.SortingType

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private val viewModel: HomeViewModel by viewModels { AppViewModelProvider.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(layoutInflater)
        navController = (activity as MainActivity).findNavController(R.id.main_nav_host_fragment)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ProductStatsListAdapter {
            navController.navigate(
                HomeFragmentDirections.homeToProductDetails(it)
            )
        }

        binding.apply {
            binding.lifecycleOwner = this@HomeFragment

            productStatsRecyclerView.adapter = adapter
            productStatsRecyclerView.layoutManager = LinearLayoutManager(context)
            productStatsRecyclerView.itemAnimator = null

            botNavBar.setOnItemSelectedListener { onNavItemClick(it) }

            productStatsTabLayout.addOnTabSelectedListener(
                object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        onProductStatsTabSelected(tab)
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {
                        // Nothing here
                    }

                    override fun onTabReselected(tab: TabLayout.Tab) {
                        viewModel.changeSortingType(
                            ascending = !viewModel.uiState.value.sortAscending
                        )
                    }
                }
            )
        }

        adapter.submitList(viewModel.uiState.value.productStats)

        viewModel.uiState.asLiveData().observe(
            viewLifecycleOwner
        ) { newUiState ->
            adapter.submitList(newUiState.productStats)

            val outOfStockCount = getString(
                R.string.home_out_of_stock,
                viewModel.uiState.value.outOfStockCount
            )
            val styledOutOfStockCount: Spanned = Html.fromHtml(outOfStockCount)

            val lowOnStockCount = getString(
                R.string.home_low_on_stock,
                viewModel.uiState.value.lowOnStockCount
            )
            val styledLowOnStockCount: Spanned = Html.fromHtml(lowOnStockCount)

            binding.apply {
                outOfStockCard.outOfStockCounter.text = styledOutOfStockCount
                lowOnStockCard.lowOnStockCounter.text = styledLowOnStockCount
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshHomeContent()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding.apply {
            productStatsRecyclerView.adapter = null
            botNavBar.setOnClickListener(null)
            productStatsTabLayout.clearOnTabSelectedListeners()
            lifecycleOwner = null
        }

        viewModel.uiState.asLiveData().removeObservers(this)
        _binding = null
    }

    private fun onNavItemClick(navItem: MenuItem): Boolean {
        return when (navItem.itemId) {
            R.id.homeFragment -> return true

            R.id.inventoryFragment -> {
                navController.navigate(HomeFragmentDirections.homeToInventory())
                false
            }

            R.id.receiptFragment -> {
                navController.navigate(HomeFragmentDirections.homeToReceipt())
                false
            }

            R.id.settingsFragment -> {
                navController.navigate(HomeFragmentDirections.homeToSettings())
                false
            }

            else -> false
        }
    }

    private fun onProductStatsTabSelected(tab: TabLayout.Tab) {
        when (tab.position) {
            0 -> {
                viewModel.changeSortingType(
                    sortingType = SortingType.ByNumberSold,
                    ascending = true
                )
                Log.d("this", "sort by number sold")
            }

            else -> {
                viewModel.changeSortingType(
                    sortingType = SortingType.ByRevenue,
                    ascending = true
                )
                Log.d("this", "sort by revenue earned")
            }
        }
    }
}
