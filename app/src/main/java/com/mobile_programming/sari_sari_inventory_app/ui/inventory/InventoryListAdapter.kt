package com.mobile_programming.sari_sari_inventory_app.ui.inventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.setPadding
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobile_programming.sari_sari_inventory_app.R
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.databinding.InventoryListItemBinding
import com.mobile_programming.sari_sari_inventory_app.ui.product.formattedPrice
import com.mobile_programming.sari_sari_inventory_app.ui.product.toProductDetails

class InventoryListAdapter(
    private val onItemClick: (Long) -> Unit,
    private val onDeleteClick: (Product) -> Unit,
) :
    ListAdapter<Product, InventoryListAdapter.InventoryItemViewHolder>(InventoryItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryItemViewHolder {

        val binding = InventoryListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return InventoryItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventoryItemViewHolder, position: Int) {
        holder.bind(
            product = getItem(position),
            onItemClick = onItemClick,
            onDeleteClick = onDeleteClick
        )
    }

    class InventoryItemCallback : ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }

    }

    class InventoryItemViewHolder(
        private val binding: InventoryListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            product: Product,
            onItemClick: (Long) -> Unit,
            onDeleteClick: (Product) -> Unit
        ) {
            val context = binding.root.context
            val productDetails = product.toProductDetails()

            binding.productDetails = productDetails

            if (productDetails.imageUri != null) {
                binding.productImage.setImageURI(productDetails.imageUri)
                binding.productImage.setPadding(0)
            }

            binding.productNumber.text =
                productDetails.productNumber.ifBlank {
                    context.getString(R.string.no_product_number)
                }

            binding.productPrice.text = product.formattedPrice()

            binding.deleteButton.contentDescription = context.getString(
                R.string.delete_product_content_desc,
                productDetails.productName
            )
            binding.deleteButton.setOnClickListener {
                onDeleteClick(product)
            }

            binding.cardView.setOnClickListener {
                onItemClick(productDetails.id.toLongOrNull() ?: 0)
            }
        }
    }


}