package com.example.pinmemory.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.pinmemory.R
import com.example.pinmemory.data.local.MemoryDatabase
import com.example.pinmemory.data.remote.FirestoreDataSource
import com.example.pinmemory.data.repository.MemoryRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var selectedLatLng: LatLng? = null
    private lateinit var viewModel: MapViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dao = MemoryDatabase.getDatabase(requireContext()).memoryDao()
        val repository = MemoryRepository(dao, FirestoreDataSource())
        val factory = MapViewModelFactory(repository)
        viewModel = MapViewModelFactory(repository).let {
            androidx.lifecycle.ViewModelProvider(this, it)[MapViewModel::class.java]
        }

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        view.findViewById<FloatingActionButton>(R.id.fabAddMemory).setOnClickListener {
            val bundle = Bundle().apply {
                putDouble("latitude", selectedLatLng?.latitude ?: 0.0)
                putDouble("longitude", selectedLatLng?.longitude ?: 0.0)
            }
            findNavController().navigate(R.id.action_map_to_add, bundle)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val defaultLocation = LatLng(42.6977, 23.3219)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 7f))

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModel.getMemories(userId).observe(viewLifecycleOwner) { memories ->
            googleMap.clear()
            memories.forEach { memory ->
                val position = LatLng(memory.latitude, memory.longitude)
                val marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(memory.title)
                )
                marker?.tag = memory.id
            }
        }

        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            selectedLatLng = latLng
            googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("New Memory")
            )
        }

        googleMap.setOnMarkerClickListener { marker ->
            val memoryId = marker.tag as? String
            if (memoryId != null) {
                val bundle = Bundle().apply { putString("memoryId", memoryId) }
                findNavController().navigate(R.id.action_map_to_detail, bundle)
            }
            true
        }
    }
}