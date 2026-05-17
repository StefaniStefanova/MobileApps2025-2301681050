package com.example.pinmemory.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.pinmemory.R
import com.example.pinmemory.data.local.MemoryDatabase
import com.example.pinmemory.data.remote.FirestoreDataSource
import com.example.pinmemory.data.repository.MemoryRepository
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DetailFragment : Fragment() {

    private lateinit var repository: MemoryRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dao = MemoryDatabase.getDatabase(requireContext()).memoryDao()
        repository = MemoryRepository(dao, FirestoreDataSource())

        val memoryId = arguments?.getString("memoryId") ?: return

        val ivImage = view.findViewById<ImageView>(R.id.ivMemoryImage)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val tvLocation = view.findViewById<TextView>(R.id.tvLocation)
        val tvNote = view.findViewById<TextView>(R.id.tvNote)
        val btnEdit = view.findViewById<MaterialButton>(R.id.btnEdit)
        val btnDelete = view.findViewById<MaterialButton>(R.id.btnDelete)

        CoroutineScope(Dispatchers.IO).launch {
            val entity = repository.getMemoryById(memoryId)
            withContext(Dispatchers.Main) {
                if (entity != null) {
                    tvTitle.text = entity.title
                    tvNote.text = entity.note
                    tvLocation.text = "📍 ${entity.locationName}"

                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    tvDate.text = "📅 ${dateFormat.format(Date(entity.date))}"

                    if (entity.imageUrl.isNotEmpty()) {
                        Glide.with(this@DetailFragment)
                            .load(entity.imageUrl)
                            .into(ivImage)
                    }

                    btnEdit.setOnClickListener {
                        val bundle = Bundle().apply { putString("memoryId", memoryId) }
                        findNavController().navigate(R.id.addEditFragment, bundle)
                    }

                    btnDelete.setOnClickListener {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Delete Memory")
                            .setMessage("Are you sure you want to delete this memory?")
                            .setPositiveButton("Delete") { _, _ ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    repository.deleteMemory(memoryId, entity!!)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(requireContext(), "Memory deleted", Toast.LENGTH_SHORT).show()
                                        findNavController().popBackStack()
                                    }
                                }
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }
            }
        }
    }
}