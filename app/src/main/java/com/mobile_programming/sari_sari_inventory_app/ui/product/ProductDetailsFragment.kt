package com.mobile_programming.sari_sari_inventory_app.ui.product

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.mobile_programming.sari_sari_inventory_app.MainActivity
import com.mobile_programming.sari_sari_inventory_app.R
import com.mobile_programming.sari_sari_inventory_app.databinding.FragmentProductDetailsBinding
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.utils.TextInputErrorType
import com.mobile_programming.sari_sari_inventory_app.utils.productImageDir
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

class ProductDetailsFragment : Fragment() {
    private var _binding: FragmentProductDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private val viewModel: ProductDetailsViewModel by viewModels { AppViewModelProvider.Factory }

    private var tempImage: File? = null
    private  var tempCameraImage: File? = null
    private val tempImageName = "tempImage"
    private val tempImageExtension = ".jpg"

    private val cameraRequestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            val context = requireContext()

            if (isGranted) {
                launchCamera(context)
            } else {
                context.showPermissionRequiredToast(
                    getString(R.string.camera)
                )
            }
        }

    private val cameraLauncher =
        registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                tempImage?.delete()
                tempImage = tempCameraImage

                tempImage?.let { file ->
                    setTempImage(requireContext().getFileUri(file))
                }
            } else {
                tempCameraImage?.delete()
            }
        }

    private val pickImagePermissionRequestLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickImagesLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                } else {
                    getContentLauncher.launch("image/*")
                }
            } else {
                context?.showPermissionRequiredToast(
                    getString(R.string.gallery)
                )
            }
        }

    private val pickImagesLauncher =
        registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            uri?.let {
                createTempImage(requireContext(), it)
            }
        }

    private val getContentLauncher =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                createTempImage(requireContext(), it)
            }
        }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onUpPressed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailsBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        navController = (activity as MainActivity).findNavController(R.id.main_nav_host_fragment)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        viewModel.uiState.value.productDetails.imageUri?.let { uri ->
            binding.productImage.setImageURI(uri)
        }

        // Bu orqaga tugmani yuqoriga o'tish o'rniga tahrirlash rejimidan chiqadi
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, onBackPressedCallback)

        viewModel.uiState.asLiveData().observe(
            viewLifecycleOwner,
        ) {
            updateDisplayedImage()

            binding.topAppToolbar.menu.findItem(R.id.save_changes)?.let { menuItem ->
                menuItem.isEnabled = viewModel.uiState.value.isInputValid
            }

            checkTextFieldErrors(
                binding.productNumberField,
                ProductEntryViewModel.PRODUCT_NUMBER_ERROR_KEY
            )

            checkTextFieldErrors(
                binding.productNameField,
                ProductEntryViewModel.PRODUCT_NAME_ERROR_KEY
            )

            checkTextFieldErrors(
                binding.productPriceField,
                ProductEntryViewModel.PRODUCT_PRICE_ERROR_KEY
            )

            checkTextFieldErrors(
                binding.productStockField,
                ProductEntryViewModel.PRODUCT_STOCK_ERROR_KEY
            )
        }

        binding.topAppToolbar.setNavigationOnClickListener {
            onUpPressed()
        }

        binding.topAppToolbar.setOnMenuItemClickListener { menuItem: MenuItem ->
            onTopAppBarMenuItemClick(menuItem)
        }

        binding.productNumberField.editText?.doAfterTextChanged { text ->
            viewModel.updateUiState(
                viewModel.uiState.value.tempDetails.copy(
                    productNumber = text.toString()
                )
            )
        }

        binding.productNameField.editText?.doAfterTextChanged { text ->
            viewModel.updateUiState(
                viewModel.uiState.value.tempDetails.copy(
                    productName = text.toString()
                )
            )
        }

        binding.productPriceField.editText?.doAfterTextChanged { text ->
            viewModel.updateUiState(
                viewModel.uiState.value.tempDetails.copy(
                    price = text.toString()
                )
            )
        }

        binding.productStockField.editText?.doAfterTextChanged { text ->
            viewModel.updateUiState(
                viewModel.uiState.value.tempDetails.copy(
                    stock = text.toString()
                )
            )
        }

        binding.addPhotoButton.setOnClickListener {
            showImageSourceSelector(requireContext())
        }

        binding.removePhotoButton.setOnClickListener {
            clearTempImage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        tempImage?.delete()
        tempCameraImage?.delete()
        onBackPressedCallback.remove()

        binding.apply {
            topAppToolbar.setOnMenuItemClickListener(null)
            topAppToolbar.setNavigationOnClickListener(null)
            addPhotoButton.setOnClickListener(null)
            removePhotoButton.setOnClickListener(null)
            productNameField.editText?.removeTextChangedListener(null)
            productNumberField.editText?.removeTextChangedListener(null)
            productPriceField.editText?.removeTextChangedListener(null)
            productStockField.editText?.removeTextChangedListener(null)
            viewModel = null
            lifecycleOwner = null
        }

        viewModel.uiState.asLiveData().removeObservers(this)
        _binding = null
    }

    private fun onUpPressed() {
        if (viewModel.uiState.value.isEditing) {
            toggleEditing(false)
        } else {
            navController.navigateUp()
        }
    }

    private fun onTopAppBarMenuItemClick(menuItem: MenuItem) : Boolean {
        return when(menuItem.itemId) {
            R.id.edit_product -> {
                toggleEditing(true)
                true
            }

            R.id.delete_product -> {
                showDeleteConfirmationDialog(
                    context = requireContext(),
                    productDetails = viewModel.uiState.value.productDetails
                )
                true
            }

            R.id.save_changes -> {
                saveUpdates(requireContext())
                true
            }

            else -> false
        }
    }

    private fun toggleEditing(isEditing: Boolean) {
        viewModel.toggleEditingMode(isEditing)

        binding.topAppToolbar.menu.clear()
        if(isEditing) {
            binding.topAppToolbar.inflateMenu(
                R.menu.product_details_edit_mode_top_app_bar_menu
            )
        } else {
            binding.topAppToolbar.inflateMenu(
                R.menu.product_details_top_app_bar_menu
            )
            clearTempImage()
        }
    }

    private fun showProductUpdatedToast(context: Context) {
        Toast.makeText(
            context,
            context.getString(R.string.product_details_updated),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateDisplayedImage() {
        viewModel.uiState.value.let { uiState ->
            if(uiState.isEditing) {
                setDisplayedImage(uiState.tempDetails.imageUri)
            } else {
                setDisplayedImage(uiState.productDetails.imageUri)
            }
        }
    }

    private fun setDisplayedImage(uri : Uri?) {
        if(uri != null) {
            binding.productImage.setImageURI(uri)
            binding.productImage.setContentPadding(0, 0, 0, 0)
        } else {
            val padding = resources.getDimension(R.dimen.padding_extra_large).toInt()

            binding.productImage.setImageResource(R.drawable.baseline_camera_alt_24)
            binding.productImage.setContentPadding(
                padding,
                padding,
                padding,
                padding
            )
        }
    }

    private fun saveUpdates(context: Context) {
        lifecycleScope.launch {

            saveTempImageToLocal(context)
            viewModel.updateProduct(viewModel.uiState.value.tempDetails)

            viewModel.uiState.value.let { uiState ->
                if(uiState.tempDetails.imageUri != uiState.productDetails.imageUri) {
                    uiState.productDetails.imageUri?.let { uri ->
                        requireContext().contentResolver.delete(
                            uri,
                            null,
                            null
                        )
                    }
                }
            }

            toggleEditing(false)
            showProductUpdatedToast(context)
        }
    }

    // Ushbu kod bloki tasvirni keshdan mahalliy xotiraga o'tkazadi
    private fun saveTempImageToLocal(context: Context) {
        tempImage?.let { file ->
            val newImageFileName =
                "${Calendar.getInstance().timeInMillis}.jpeg"

            val newImageFile = context.copyFileToInternalStorage(
                oldFile = file,
                newFileName = newImageFileName,
                child = productImageDir
            )

            val newImageUri = context.getFileUri(newImageFile)

            viewModel.updateUiState(
                productDetails = viewModel.uiState.value.tempDetails.copy(
                    imageUri = newImageUri
                )
            )
        }

        tempImage?.delete()
    }

    private fun clearTempImage() {
        viewModel.updateUiState(
            viewModel.uiState.value.tempDetails.copy(
                imageUri = null
            )
        )

        tempImage?.delete()
        tempImage = null
    }

    private fun setTempImage(uri: Uri) {
        viewModel.updateUiState(
            viewModel.uiState.value.tempDetails.copy(
                imageUri = uri
            )
        )
    }

    private fun createTempImage(
        context: Context,
        uri: Uri
    ) {
        tempImage?.delete()
        tempImage = null

        tempImage = context.storeTempImageInCache(
            uri = uri,
            prefix = tempImageName,
            suffix = tempImageExtension
        )

        tempImage?.let { file ->
            setTempImage(context.getFileUri(file))
        }
    }

    private fun checkTextFieldErrors(
        textField: TextInputLayout,
        errorKey: String,
    ) {
        val fieldError = viewModel.uiState.value.detailsErrorMap[errorKey]

        textField.apply {
            isErrorEnabled = fieldError != null

            error = fieldError?.let { error ->
                when (error) {
                    TextInputErrorType.DuplicateFound -> {
                        getString(error.stringResourceId, errorKey)
                    }

                    else -> {
                        getString(error.stringResourceId)
                    }
                }
            }
        }
    }

    private fun showImageSourceSelector(
        context: Context
    ) {
        val options = arrayOf(
            getString(R.string.camera),
            getString(R.string.gallery)
        )

        MaterialAlertDialogBuilder(context)
            .setTitle("Rasm manbasini tanlang")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        cameraRequestPermissionLauncher.launch(
                            Manifest.permission.CAMERA
                        )
                    }

                    1 -> {
                        launchGallery()
                    }
                }
            }
            .setNegativeButton("Bekor qilish", null)
            .show()
    }

    private fun launchGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // For API 34, READ_MEDIA_VISUAL_USER_SELECTED is used
            // This feature is fairly new and provides partial access to media files
            pickImagePermissionRequestLauncher.launch(
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For API 33, READ_MEDIA_IMAGES is used
            // This feature is an updated version of the getContent API
            pickImagePermissionRequestLauncher.launch(
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            // For API 32 and below, READ_EXTERNAL_STORAGE is used
            pickImagePermissionRequestLauncher.launch(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun launchCamera(
        context: Context
    ) {
        tempCameraImage = File.createTempFile(
            tempImageName,
            tempImageExtension
        )

        tempCameraImage?.let {  file ->
            cameraLauncher.launch(
                context.getFileUri(file)
            )
        }
    }

    private fun showDeleteConfirmationDialog(
        context: Context,
        productDetails: ProductDetails
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.delete_confirmation_title)
            .setMessage(R.string.delete_confirmation_message)
            .setNeutralButton(R.string.cancel, null)
            .setPositiveButton(R.string.confirm) { dialog, _ ->
                deleteSelectedProduct(context, productDetails)
                navController.navigateUp()
                dialog.dismiss()
            }
            .show()
    }

    private fun showProductDeletedToast(
        context: Context
    ) {
        Toast.makeText(
            context,
            context.getString(R.string.product_deleted),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun deleteSelectedProduct(
        context: Context,
        productDetails: ProductDetails
    ) {
        lifecycleScope.launch {
            productDetails.imageUri?.let { uri ->
                context.contentResolver.delete(
                    uri,
                    null,
                    null
                )
            }

            viewModel.deleteProduct()
            showProductDeletedToast(context)
        }
    }
}