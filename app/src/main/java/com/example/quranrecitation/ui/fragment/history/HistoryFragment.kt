package com.example.quranrecitation.ui.fragment.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.quranrecitation.feature.Adapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.quranrecitation.databinding.FragmentHistoryBinding
import com.example.quranrecitation.feature.OnItemClickListener
import com.example.quranrecitation.room.AppDatabase
import com.example.quranrecitation.room.AudioRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFragment : Fragment(), OnItemClickListener {

    private lateinit var records : ArrayList<AudioRecord>
    private lateinit var mAdapter : com.example.quranrecitation.feature.Adapter
    private lateinit var db :  AppDatabase

    private var _binding: FragmentHistoryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val historyViewModel =
            ViewModelProvider(this).get(HistoryViewModel::class.java)

        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        records = ArrayList()

        db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "audioRecords"
        ).build()

        mAdapter = com.example.quranrecitation.feature.Adapter(records, this)

        binding.recyclerview.apply{
            adapter = mAdapter
            layoutManager = LinearLayoutManager(context)
        }

        fetchAll()

//        val textView: TextView = binding.textNotifications
//        historyViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    private fun fetchAll(){
        GlobalScope.launch {
            records.clear()
            var queryResult = db.audioRecordDao().getAll()
            records.addAll(queryResult)

            withContext(Dispatchers.Main) {
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClickListener(position: Int) {
        Toast.makeText(requireContext(), "Simple Click", Toast.LENGTH_SHORT).show()
    }

    override fun onItemLongClickListener(position: Int) {
        Toast.makeText(requireContext(), "Long Click", Toast.LENGTH_SHORT).show()
    }
}