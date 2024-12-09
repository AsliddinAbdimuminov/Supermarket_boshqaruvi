package com.mobile_programming.sari_sari_inventory_app.ui.product

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
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
import com.mobile_programming.sari_sari_inventory_app.databinding.FragmentProductEntryBinding
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.utils.TextInputErrorType
import com.mobile_programming.sari_sari_inventory_app.utils.productImageDir
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Calendar

class ProductEntryFragment : Fragment() {
    private var _binding: FragmentProductEntryBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private val viewModel: ProductEntryViewModel by viewModels { AppViewModelProvider.Factory }

    // There are two temp image values since one is for the camera
    // In case the camera does not return with an image
    private var tempImage: File? = null
    private var tempCameraImage: File? = null
    private val tempImageName = "tempImage"
    private val tempImageExtension = ".jpg"

    private val cameraRequestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            val context = requireContext()

            if(isGranted) {
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

    // This launcher check for the current device SDK
    // and calls the appropriate API to get media from
    private val pickImagePermissionRequestLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if(isGranted) {
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
        ) { uri : Uri? ->
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductEntryBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        navController = (activity as MainActivity).findNavController(R.id.main_nav_host_fragment)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()

        binding.viewModel = viewModel

        viewModel.uiState.asLiveData().observe(
            viewLifecycleOwner
        ) { newState ->

            setDisplayedImage(newState.productDetails.imageUri)

            binding.topAppToolbar.menu.findItem(R.id.save_product)?.let { menuItem ->
                menuItem.isEnabled = viewModel.uiState.value.isInputValid
            }

            checkTextFieldErrors(
                textField = binding.productNumberField,
                ProductEntryViewModel.PRODUCT_NUMBER_ERROR_KEY
            )

            checkTextFieldErrors(
                textField = binding.productNameField,
                ProductEntryViewModel.PRODUCT_NAME_ERROR_KEY
            )

            checkTextFieldErrors(
                textField = binding.productPriceField,
                ProductEntryViewModel.PRODUCT_PRICE_ERROR_KEY
            )

            checkTextFieldErrors(
                textField = binding.productStockField,
                ProductEntryViewModel.PRODUCT_STOCK_ERROR_KEY
            )
        }

        binding.productNumberField.editText?.doAfterTextChanged {
            viewModel.updateUiState(
                viewModel.uiState.value.productDetails.copy(
                    productNumber = it.toString()
                )
            )
        }

        binding.productNameField.editText?.doAfterTextChanged {
            viewModel.updateUiState(
                viewModel.uiState.value.productDetails.copy(
                    productName = it.toString()
                )
            )
        }

        binding.productPriceField.editText?.doAfterTextChanged {
            viewModel.updateUiState(
                viewModel.uiState.value.productDetails.copy(
                    price = it.toString()
                )
            )
        }

        binding.productStockField.editText?.doAfterTextChanged {
            viewModel.updateUiState(
                viewModel.uiState.value.productDetails.copy(
                    stock = it.toString()
                )
            )
        }

        binding.topAppToolbar.setNavigationOnClickListener {
            navController.popBackStack()
        }

        binding.topAppToolbar.setOnMenuItemClickListener { menuItem: MenuItem ->
            onTopAppBarMenuItemClick(menuItem)
        }

        binding.addPhotoButton.setOnClickListener {
            showImageSourceSelector(context)
        }

        binding.removePhotoButton.setOnClickListener {
            clearTempImage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        tempImage?.delete()
        tempImage = null

        tempCameraImage?.delete()
        tempCameraImage = null

        binding.apply {
            topAppToolbar.setNavigationOnClickListener(null)
            topAppToolbar.setOnMenuItemClickListener(null)
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

    private fun onTopAppBarMenuItemClick(menuItem: MenuItem) : Boolean {
        return when(menuItem.itemId) {
            R.id.save_product -> {
                addProduct(requireContext())
                true
            }

            else -> false
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

    private fun clearTempImage() {
        viewModel.updateUiState(
            viewModel.uiState.value.productDetails.copy(
                imageUri = null
            )
        )

        tempImage?.delete()
        tempImage = null
    }

    private fun setTempImage(uri: Uri) {
        viewModel.updateUiState(
            viewModel.uiState.value.productDetails.copy(
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
                productDetails = viewModel.uiState.value.productDetails.copy(
                    imageUri = newImageUri
                )
            )
        }
        tempImage?.delete()
    }

    private fun showProductAddedToast(context: Context) {
        Toast.makeText(
            context,
            context.getString(R.string.new_product_added),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun addProduct(context: Context) {
        lifecycleScope.launch {
            saveTempImageToLocal(context)

            viewModel.insertProduct()
            showProductAddedToast(context)

            navController.popBackStack()
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

        tempCameraImage?.let { file ->
            cameraLauncher.launch(context.getFileUri(file))
        }
    }
}

fun Context.storeTempImageInCache(
    uri: Uri,
    prefix: String,
    suffix: String,
): File {
    val inputStream = this.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(inputStream)

    val file = File.createTempFile(prefix, suffix).apply {
        deleteOnExit()
    }

    val outputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
    outputStream.close()

    return file
}

fun Context.getFileUri(
    file: File
): Uri {
    return FileProvider.getUriForFile(
        this,
        "${this.packageName}.provider",
        file
    )
}

fun Context.copyFileToInternalStorage(
    oldFile: File,
    newFileName: String,
    child: String
): File {
    val internalDir = File(this.filesDir.toString() + child)

    if (!internalDir.exists()) {
        internalDir.mkdir()
    }

    val newFile = File(internalDir, newFileName)

    try {
        val inputStream = FileInputStream(oldFile)
        val outputStream = FileOutputStream(newFile)
        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        inputStream.close()
        outputStream.flush()
        outputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return newFile
}

fun Context.showPermissionRequiredToast(permissionName: String) {
    Toast.makeText(
        this,
        this.getString(R.string.permission_required_toast, permissionName),
        Toast.LENGTH_SHORT
    ).show()
}