package com.example.pinmemory.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pinmemory.R
import com.example.pinmemory.data.local.MemoryDatabase
import com.google.firebase.auth.FirebaseAuth

class ListFragment : Fragment() {

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

        adapter = MemoryAdapter { memoryId ->
            val bundle = Bundle().apply { putString("memoryId", memoryId) }
            findNavController().navigate(R.id.action_list_to_detail, bundle)
        }

        val rvMemories = view.findViewById<RecyclerView>(R.id.rvMemories)
        rvMemories.layoutManager = LinearLayoutManager(requireContext())
        rvMemories.adapter = adapter

        val dao = MemoryDatabase.getDatabase(requireContext()).memoryDao()
        dao.getMemoriesByUser(userId).observe(viewLifecycleOwner) { memories ->
            adapter.submitList(memories)
        }
    }
}