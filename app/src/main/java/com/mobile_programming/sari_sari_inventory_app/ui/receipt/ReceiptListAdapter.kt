package com.mobile_programming.sari_sari_inventory_app.ui.receipt

import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.setPadding
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobile_programming.sari_sari_inventory_app.R
import com.mobile_programming.sari_sari_inventory_app.databinding.ReceiptListItemBinding
import com.mobile_programming.sari_sari_inventory_app.ui.product.ProductDetails
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.ProductWithAmount

class ReceiptListAdapter(
    private val onItemClick: (ProductWithAmount) -> Unit,
    private val onRemoveClick: (ProductDetails) -> Unit,
) :
    ListAdapter<ProductWithAmount, ReceiptListAdapter.ReceiptItemViewHolder>(ReceiptItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptItemViewHolder {

        val binding = ReceiptListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ReceiptItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReceiptItemViewHolder, position: Int) {
        holder.bind(
            productWithAmount = getItem(position),
            onItemClick = onItemClick,
            onRemoveClick = onRemoveClick
        )
    }

    class ReceiptItemCallback : ItemCallback<ProductWithAmount>() {
        override fun areItemsTheSame(
            oldItem: ProductWithAmount,
            newItem: ProductWithAmount
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ProductWithAmount,
            newItem: ProductWithAmount
        ): Boolean {
            return oldItem == newItem
        }

    }

    class ReceiptItemViewHolder(
        private val binding: ReceiptListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            productWithAmount: ProductWithAmount,
            onItemClick: (ProductWithAmount) -> Unit,
            onRemoveClick: (ProductDetails) -> Unit
        ) {
            val context = binding.root.context
            val productDetails = productWithAmount.productDetails

            binding.productWithAmount = productWithAmount

            if (productDetails.imageUri != null) {
                binding.productImage.setImageURI(productDetails.imageUri)
                binding.productImage.setPadding(0)
                binding.productImage.contentDescription = context.getString(
                    R.string.product_image, productDetails.productName
                )
            }

            val amountString: String = context.getString(
                R.string.product_amount,
                productWithAmount.amount.toString()
            )
            val styledAmountString: Spanned = Html.fromHtml(amountString)
            binding.productAmount.text = styledAmountString

            val subTotalString: String = context.getString(
                R.string.product_sub_total,
                productWithAmount.formattedTotalPrice()
            )
            val styledSubTotalString: Spanned = Html.fromHtml(subTotalString)
            binding.productSubTotal.text = styledSubTotalString

            binding.removeButton.setOnClickListener {
                onRemoveClick(productDetails)
            }
            binding.removeButton.contentDescription =
                context.getString(
                    R.string.remove_product_from_receipt_content_desc,
                    productDetails.productName
                )

            binding.cardView.setOnClickListener {
                onItemClick(productWithAmount)
            }
        }
    }


}