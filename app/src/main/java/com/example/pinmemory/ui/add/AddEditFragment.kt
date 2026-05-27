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
import com.example.pinmemory.data.remote.FirestoreDataSource
import com.example.pinmemory.data.repository.MemoryRepository
import com.example.pinmemory.model.Memory
import com.example.pinmemory.util.LocationHelper
import com.example.pinmemory.util.toEntity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
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
    private var selectedDate: Long = System.currentTimeMillis()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    private var etTitle: TextInputEditText? = null
    private var etNote: TextInputEditText? = null
    private var tvLocation: TextView? = null
    private var tvDate: TextView? = null

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

        etTitle = view.findViewById(R.id.etTitle)
        etNote = view.findViewById(R.id.etNote)
        tvLocation = view.findViewById(R.id.tvLocation)
        tvDate = view.findViewById(R.id.tvDate)
        val btnPickImage = view.findViewById<MaterialButton>(R.id.btnPickImage)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)
        val btnBack = view.findViewById<android.widget.Button>(R.id.btnBack)

        btnBack.setOnClickListener { findNavController().popBackStack() }

        tvDate!!.text = " ${dateFormat.format(Date(selectedDate))}"

        tvDate!!.setOnClickListener {
            val calendar = Calendar.getInstance()
            android.app.DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.timeInMillis
                    tvDate!!.text = " ${dateFormat.format(Date(selectedDate))}"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        if (editMemoryId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val entity = repository.getMemoryById(editMemoryId!!)
                withContext(Dispatchers.Main) {
                    if (entity != null) {
                        etTitle!!.setText(entity.title)
                        etNote!!.setText(entity.note)
                        latitude = entity.latitude
                        longitude = entity.longitude
                        locationName = entity.locationName
                        selectedDate = entity.date
                        tvLocation!!.text = " ${entity.locationName}"
                        tvDate!!.text = " ${dateFormat.format(Date(entity.date))}"
                        if (entity.imageUrl.isNotEmpty()) {
                            Glide.with(this@AddEditFragment)
                                .load(entity.imageUrl)
                                .into(view.findViewById(R.id.ivMemoryImage))
                            selectedImageUri = Uri.parse(entity.imageUrl)
                        }
                    }
                }
            }
        } else {
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
                    tvLocation!!.text = " $locationName"
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
                        tvLocation!!.text = " $locationName"
                    } else {
                        tvLocation!!.text = " Location unavailable"
                    }
                }
            }
        }

        btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        btnSave.setOnClickListener {
            val title = etTitle!!.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid ?: run {
                Toast.makeText(requireContext(), "Not logged in!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val memoryId = editMemoryId ?: UUID.randomUUID().toString()

            CoroutineScope(Dispatchers.IO).launch {
                val imageUrl = if (selectedImageUri != null) {
                    selectedImageUri.toString()
                } else if (editMemoryId != null) {
                    repository.getMemoryById(editMemoryId!!)?.imageUrl ?: ""
                } else {
                    ""
                }
                withContext(Dispatchers.Main) {
                    saveMemory(memoryId, title, etNote!!.text.toString().trim(), userId, imageUrl)
                }
            }
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
            date = selectedDate,
            userId = userId
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.addMemory(memory, memory.toEntity())
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Memory saved!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}