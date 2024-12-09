package com.mobile_programming.sari_sari_inventory_app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.mobile_programming.sari_sari_inventory_app.databinding.HomeProductStatsListItemBinding
import com.mobile_programming.sari_sari_inventory_app.ui.product.ProductDetails
import com.mobile_programming.sari_sari_inventory_app.ui.product.toProductDetails

class ProductStatsListAdapter(
    private val onItemClick: (Long) -> Unit
) :
    ListAdapter<ProductWithStatistic, ProductStatsListAdapter.ProductStatsItemViewHolder>(
        ProductStatsItemCallBack()
    ) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductStatsItemViewHolder {

        val binding = HomeProductStatsListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ProductStatsItemViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ProductStatsItemViewHolder,
        position: Int
    ) {
        holder.bind(
            productDetails = getItem(position).product?.toProductDetails(),
            statistic = getItem(position).statistic,
            onItemClick = onItemClick
        )
    }

    class ProductStatsItemCallBack : ItemCallback<ProductWithStatistic>() {
        override fun areItemsTheSame(
            oldItem: ProductWithStatistic,
            newItem: ProductWithStatistic
        ): Boolean {
            return oldItem.product == newItem.product
                    && oldItem.statistic == newItem.statistic
        }

        override fun areContentsTheSame(
            oldItem: ProductWithStatistic,
            newItem: ProductWithStatistic
        ): Boolean {
            return oldItem.product == newItem.product
                    && oldItem.statistic == newItem.statistic
        }
    }


    class ProductStatsItemViewHolder(
        private val binding: HomeProductStatsListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            productDetails: ProductDetails?,
            statistic: String,
            onItemClick: (Long) -> Unit
        ) {
            binding.apply {
                productDetails?.let { details ->
                    if (details.imageUri != null) {
                        productImage.setImageURI(details.imageUri)
                    }

                    if (details.productNumber.isNotBlank()) {
                        productNumber.text = details.productNumber
                    }

                    productName.text = details.productName

                    val surfaceColor = MaterialColors.getColor(
                        binding.root,
                        com.google.android.material.R.attr.colorOnSurface
                    )
                    productName.setTextColor(surfaceColor)
                    productNumber.setTextColor(surfaceColor)

                    productStatsCard.setOnClickListener {
                        onItemClick(details.id.toLongOrNull() ?: 0)
                    }
                }

                productStatistic.text = statistic
            }
        }
    }
}