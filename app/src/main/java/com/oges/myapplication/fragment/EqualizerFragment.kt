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

        viewModel.initEqualizer()

        setupBands()
        setupKnobs()
        setupSwitch()
    }

    private fun setupBands() {

        val sliders = listOf(
            binding.band60,
            binding.band230,
            binding.band1k,
            binding.band3k,
            binding.band10k
        )

        sliders.forEachIndexed { index, seekBar ->
            seekBar.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        sb: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser) {
                            viewModel.setBand(index, progress - 15)
                        }
                    }

                    override fun onStartTrackingTouch(sb: SeekBar?) {}
                    override fun onStopTrackingTouch(sb: SeekBar?) {}
                }
            )
        }
    }

    private fun setupKnobs() {

        binding.knobLow.setOnValueChangeListener { value ->
            viewModel.setBass(value)
        }

        binding.knobHigh.setOnValueChangeListener { value ->
            viewModel.setTreble(value)
        }
    }

    private fun setupSwitch() {
        binding.switchEqEnable.setOnCheckedChangeListener { _, isChecked ->
            viewModel.enableEffects(isChecked)
        }
    }
}