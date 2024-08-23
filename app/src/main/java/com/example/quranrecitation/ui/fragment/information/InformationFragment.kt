package com.example.quranrecitation.ui.fragment.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.quranrecitation.databinding.FragmentInformationBinding

class InformationFragment : Fragment() {

    private var _binding: FragmentInformationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val informationViewModel =
            ViewModelProvider(this)[InformationViewModel::class.java]

        _binding = FragmentInformationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textTentangAplikasi
        informationViewModel.textTentangAplikasi.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val textView1: TextView = binding.textList1
        informationViewModel.text1.observe(viewLifecycleOwner) {
            textView1.text = it
        }

        val textView2: TextView = binding.textList2
        informationViewModel.text2.observe(viewLifecycleOwner) {
            textView2.text = it
        }

        val textView3: TextView = binding.textList3
        informationViewModel.text3.observe(viewLifecycleOwner) {
            textView3.text = it
        }

        val textView4: TextView = binding.textList4
        informationViewModel.text4.observe(viewLifecycleOwner) {
            textView4.text = it
        }

        val textView5: TextView = binding.textList5
        informationViewModel.text5.observe(viewLifecycleOwner) {
            textView5.text = it
        }

        val textView6: TextView = binding.textList6
        informationViewModel.text6.observe(viewLifecycleOwner) {
            textView6.text = it
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}