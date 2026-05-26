package com.example.pinmemory.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pinmemory.R
import com.example.pinmemory.data.local.MemoryDatabase
import com.example.pinmemory.data.remote.FirestoreDataSource
import com.example.pinmemory.data.repository.MemoryRepository
import com.google.firebase.auth.FirebaseAuth

class ListFragment : Fragment() {

    private lateinit var viewModel: ListViewModel
    private lateinit var adapter: MemoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val dao = MemoryDatabase.getDatabase(requireContext()).memoryDao()
        val repository = MemoryRepository(dao, FirestoreDataSource())
        val factory = ListViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ListViewModel::class.java]

        adapter = MemoryAdapter { memoryId ->
            val bundle = Bundle().apply { putString("memoryId", memoryId) }
            findNavController().navigate(R.id.action_list_to_detail, bundle)
        }

        val rvMemories = view.findViewById<RecyclerView>(R.id.rvMemories)
        rvMemories.layoutManager = LinearLayoutManager(requireContext())
        rvMemories.adapter = adapter

        viewModel.getMemories(userId).observe(viewLifecycleOwner) { memories ->
            adapter.submitList(memories)
        }

        view.findViewById<ImageButton>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.loginFragment)
        }

        view.findViewById<LinearLayout>(R.id.navMap).setOnClickListener {
            findNavController().navigate(R.id.mapFragment)
        }

        view.findViewById<LinearLayout>(R.id.navMemories).setOnClickListener {
            // вече сме на Memories
        }
    }
}