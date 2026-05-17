package com.example.pinmemory.ui.add

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.pinmemory.R
import com.example.pinmemory.data.local.MemoryDatabase
import com.example.pinmemory.data.local.MemoryEntity
import com.example.pinmemory.data.remote.FirestoreDataSource
import com.example.pinmemory.data.repository.MemoryRepository
import com.example.pinmemory.model.Memory
import com.example.pinmemory.util.LocationHelper
import com.example.pinmemory.util.toEntity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AddEditFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var repository: MemoryRepository
    private lateinit var locationHelper: LocationHelper

    private var selectedImageUri: Uri? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var locationName: String = ""
    private var editMemoryId: String? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            view?.findViewById<ImageView>(R.id.ivMemoryImage)?.let {
                Glide.with(this).load(selectedImageUri).into(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        locationHelper = LocationHelper(requireContext())

        val dao = MemoryDatabase.getDatabase(requireContext()).memoryDao()
        repository = MemoryRepository(dao, FirestoreDataSource())

        editMemoryId = arguments?.getString("memoryId")

        val etTitle = view.findViewById<TextInputEditText>(R.id.etTitle)
        val etNote = view.findViewById<TextInputEditText>(R.id.etNote)
        val tvLocation = view.findViewById<TextView>(R.id.tvLocation)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val btnPickImage = view.findViewById<MaterialButton>(R.id.btnPickImage)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)

        // Set date
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        tvDate.text = "📅 ${dateFormat.format(Date())}"

// Get location from map selection or GPS
        // Get location from map selection or GPS
        val argLat = arguments?.getDouble("latitude", 0.0) ?: 0.0
        val argLng = arguments?.getDouble("longitude", 0.0) ?: 0.0

        if (argLat != 0.0 && argLng != 0.0) {
            latitude = argLat
            longitude = argLng
            CoroutineScope(Dispatchers.Main).launch {
                locationName = withContext(Dispatchers.IO) {
                    locationHelper.getLocationName(latitude, longitude)
                }
                if (locationName == "Unknown location") {
                    locationName = "%.4f, %.4f".format(latitude, longitude)
                }
                tvLocation.text = "📍 $locationName"
            }
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                val location = withContext(Dispatchers.IO) {
                    locationHelper.getLastLocation()
                }
                if (location != null) {
                    latitude = location.first
                    longitude = location.second
                    locationName = withContext(Dispatchers.IO) {
                        locationHelper.getLocationName(latitude, longitude)
                    }
                    if (locationName == "Unknown location") {
                        locationName = "%.4f, %.4f".format(latitude, longitude)
                    }
                    tvLocation.text = "📍 $locationName"
                } else {
                    tvLocation.text = "📍 Location unavailable"
                }
            }
        }

        btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            val memoryId = editMemoryId ?: UUID.randomUUID().toString()

            if (selectedImageUri != null) {
                uploadImageAndSave(memoryId, title, etNote.text.toString().trim(), userId)
            } else {
                saveMemory(memoryId, title, etNote.text.toString().trim(), userId, "")
            }
        }
    }

    private fun uploadImageAndSave(
        memoryId: String, title: String, note: String, userId: String
    ) {
        val storageRef = FirebaseStorage.getInstance().reference
            .child("memories/$userId/$memoryId.jpg")

        storageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveMemory(memoryId, title, note, userId, uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveMemory(
        memoryId: String, title: String, note: String, userId: String, imageUrl: String
    ) {
        val memory = Memory(
            id = memoryId,
            title = title,
            note = note,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            imageUrl = imageUrl,
            date = System.currentTimeMillis(),
            userId = userId
        )

        CoroutineScope(Dispatchers.IO).launch {
            repository.addMemory(memory, memory.toEntity())
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Memory saved!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }
}