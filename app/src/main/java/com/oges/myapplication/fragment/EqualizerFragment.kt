package com.oges.myapplication.fragment

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.oges.myapplication.R
import com.oges.myapplication.databinding.FragmentEqualiserBinding
import com.oges.myapplication.vm.EqualizerVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EqualizerFragment : Fragment(R.layout.fragment_equaliser) {

    private lateinit var binding: FragmentEqualiserBinding
    private val viewModel: EqualizerVM by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentEqualiserBinding.bind(view)



    }


}