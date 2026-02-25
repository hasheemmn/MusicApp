package com.codmeric.musicplayer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.codmeric.musicplayer.databinding.FragmentEqualizerBinding
import com.codmeric.musicplayer.viewmodel.SharedMediaViewModel
import com.codmeric.musicplayer.MusicPlayerApp
import com.codmeric.musicplayer.viewmodel.SharedMediaViewModelFactory
import androidx.fragment.app.activityViewModels

class EqualizerFragment : Fragment() {

    private var _binding: FragmentEqualizerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SharedMediaViewModel by activityViewModels {
        val app = requireActivity().application as MusicPlayerApp
        SharedMediaViewModelFactory(
            app.repository,
            app.mediaManager,
            app.equalizerManager,
            requireContext().applicationContext
        )
    }

    // SeekBar range is 0..30; center (15) = 0 dB, range = -15 to +15
    private val seekBarCenter = 15
    // Equalizer level is in mil-libels (mB). 1500 mB = 15 dB
    private val mbPerStep = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEqualizerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEqSliders()
        setupKnobs()
        setupPresetButtons()
        setupMasterSwitch()
    }

    private fun setupMasterSwitch() {
        binding.switchEqualizer.isChecked = viewModel.isEqualizerEnabled()
        updateUiState(binding.switchEqualizer.isChecked)

        binding.switchEqualizer.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setEqualizerEnabled(isChecked)
            updateUiState(isChecked)
        }
    }

    private fun updateUiState(enabled: Boolean) {
        //val root = binding.root as ViewGroup
    }

    private fun setupEqSliders() {
        val seekBars = listOf(
            binding.sliderEq1, binding.sliderEq2, binding.sliderEq3,
            binding.sliderEq4, binding.sliderEq5
        )

        seekBars.forEachIndexed { index, seekBar ->
            val band = index.toShort()
            // Convert mil-libel level to center-relative seekbar progress
            val levelMb = viewModel.getBandLevel(band).toInt()
            seekBar.progress = (levelMb / mbPerStep) + seekBarCenter

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val newLevel = ((progress - seekBarCenter) * mbPerStep).toShort()
                        viewModel.setBandLevel(band, newLevel)
                    }
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }
    }

    private fun setupKnobs() {
        binding.knobBass.progressValue = viewModel.getBassLevel()
        binding.knobTreble.progressValue = viewModel.getTrebleLevel()

        binding.knobBass.setOnProgressListener { progress ->
            viewModel.setBassLevel(progress)
        }
        binding.knobTreble.setOnProgressListener { progress ->
            viewModel.setTrebleLevel(progress)
        }
    }

    private fun setupPresetButtons() {
        val presetButtons = mapOf(
            binding.chipFlat to "Flat",
            binding.chipRock to "Rock",
            binding.chipPop to "Pop",
            binding.chipJazz to "Jazz",
            binding.chipClassical to "Classical",
            binding.chipVocal to "Vocal"
        )

        presetButtons.forEach { (button, preset) ->
            button.setOnClickListener {
                viewModel.setPreset(preset)
                updateSeekBarsFromPreset()
            }
        }
    }

    private fun updateSeekBarsFromPreset() {
        val seekBars = listOf(
            binding.sliderEq1, binding.sliderEq2, binding.sliderEq3,
            binding.sliderEq4, binding.sliderEq5
        )
        seekBars.forEachIndexed { index, seekBar ->
            val levelMb = viewModel.getBandLevel(index.toShort()).toInt()
            seekBar.progress = (levelMb / mbPerStep) + seekBarCenter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
