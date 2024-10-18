package com.oedinn.fleetify

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.oedinn.fleetify.Adapter.ReportAdapter
import com.oedinn.fleetify.Model.LaporanData
import com.oedinn.fleetify.Model.ReportRepository
import com.oedinn.fleetify.ViewModel.ReportViewModel
import com.oedinn.fleetify.ViewModel.ReportViewModelFactory
import com.oedinn.fleetify.databinding.ActivityMainBinding
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private val repository = ReportRepository()
    private val viewModel: ReportViewModel by viewModels { ReportViewModelFactory(repository) }
    private lateinit var vehicleAdapter: ArrayAdapter<String>
    private lateinit var imageView: ImageView
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var takePhotoLauncher: ActivityResultLauncher<Uri>

    private var photoUri: Uri? = null

    companion object {
        private const val REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = binding.recyclerView
        val adapter = ReportAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Observe reports from ViewModel
        viewModel.reports.observe(this, Observer { reports ->
            reports?.let { adapter.submitList(it) }
        })

        viewModel.fetchReports("9gC97zdWAY")

        binding.floatBtn.setOnClickListener {
            showUploadDialog()
        }

        // Launcher untuk memilih gambar dari galeri
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { intent ->
                    val imageUri: Uri? = intent.data
                    if (imageUri != null) {
                        imageView.setImageURI(imageUri)
                    } else {
                        Log.e("ImagePicker", "Image URI is null")
                    }
                } ?: run {
                    Log.e("ImagePicker", "Intent data is null")
                }
            } else {
                Log.e("ImagePicker", "Result was not OK")
            }
        }

        // Launcher untuk mengambil foto dari kamera
        takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                imageView.setImageURI(photoUri) // pastikan photoUri adalah Uri tempat foto disimpan
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showUploadDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_upload, null)
        val spinnerVehicle: Spinner = dialogView.findViewById(R.id.spinner_vehicle_type)

        imageView = dialogView.findViewById(R.id.imageViewSelected)
        val btnPickImage: Button = dialogView.findViewById(R.id.btnSelectImage)
        val btnUpload:CardView = dialogView.findViewById(R.id.crdPost)
        // Get the current date
        val currentDate = getCurrentDate()

        // Find the TextView by its ID
        val dateTextView: TextView = dialogView.findViewById(R.id.date)

        // Set the current date to the TextView
        dateTextView.text = currentDate

        viewModel.fetchVehicleList()
        viewModel.vehicleList.observe(this) { vehicles ->
            val vehicleTypes = vehicles.map { it.type }
            vehicleAdapter = ArrayAdapter(this, R.layout.spinner_item, vehicleTypes)
            vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerVehicle.adapter = vehicleAdapter

            spinnerVehicle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedVehicleId = vehicles[position].vehicleId
                    Log.d("Selected Vehicle ID", selectedVehicleId.toString())
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Do nothing
                }
            }
        }
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()

        btnUpload.setOnClickListener {
            val selectedVehicle = spinnerVehicle.selectedItem.toString()  // Get selected vehicle type
            val note = dialogView.findViewById<EditText>(R.id.edNote).text.toString()  // Get note input

            // Validate the input fields
            if (photoUri == null || note.isEmpty()) {
                // Show error message if photo or note is missing
                return@setOnClickListener
            }

            val file = File(photoUri!!.path ?: "")
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)

            val laporanData = LaporanData(
                vehicleId = selectedVehicle,  // Map vehicle type to ID if necessary
                note = note,
                photo = body,
                userId = viewModel.fetchReports("9gC97zdWAY").toString()
            )

            viewModel.uploadReport(laporanData)  // Trigger the ViewModel to upload

            // Observe the upload status
            viewModel.uploadStatus.observe(this) { response ->
                if (response.isSuccessful) {
                    dialog.dismiss()
                    Log.d("Upload", "Upload successful")
                } else {
                    // Handle failure (e.g., show an error message)
                    Log.e("Upload", "Upload failed")
                }
            }
        }

        btnPickImage.setOnClickListener {
            showImagePickerDialog()
        }
        dialog.show()
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd, MM yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Kamera", "Galeri")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pilih Sumber Gambar")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun openCamera() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoUri = createImageFile()

            photoUri?.let {
                takePhotoLauncher.launch(it)
            } ?: run {
                Log.e("Camera", "Photo URI is null")
            }
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
        }
    }

    private fun createImageFile(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
        return FileProvider.getUriForFile(
            this@MainActivity,
            "${packageName}.fileprovider",
            imageFile
        ).also {
            photoUri = it
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)} passing\n      in a {@link RequestMultiplePermissions} object for the {@link ActivityResultContract} and\n      handling the result in the {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Log.e("Permissions", "Camera permission denied")
            }
        }
    }
}
