package com.oges.myapplication.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.oges.myapplication.databinding.FragmentEqualiserBinding
import com.oges.myapplication.vm.EqualizerVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EqualizerFragment : Fragment() {

    private var _binding: FragmentEqualiserBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EqualizerVM by viewModels()

    private val seekBarCenter = 15
    private val mbPerStep = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEqualiserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivBack.setOnClickListener{
            findNavController().popBackStack()
        }
        setupEqSliders()
        setupKnobs()
        setupPresetButtons()
        setupSaveButton()
        restoreUI()
    }

    /* ---------------- SLIDERS ---------------- */

    private fun setupEqSliders() {

        val seekBars = listOf(
            binding.band60,
            binding.band230,
            binding.band1k,
            binding.band3k,
            binding.band10k
        )

        seekBars.forEachIndexed { index, seekBar ->

            seekBar.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        val level =
                            ((progress - seekBarCenter) * mbPerStep).toShort()

                        viewModel.setBandLevel(index.toShort(), level)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    /* ---------------- KNOBS ---------------- */

    private fun setupKnobs() {

        binding.knobLow.setOnProgressListener {
            viewModel.setBassLevel(it)
        }

        binding.knobHigh.setOnProgressListener {
            viewModel.setTrebleLevel(it)
        }
    }

    /* ---------------- PRESETS ---------------- */

    private fun setupPresetButtons() {

        val presets = mapOf(
            binding.chipFlat to "Flat",
            binding.chipRock to "Rock",
            binding.chipPop to "Pop",
            binding.chipJazz to "Jazz",
            binding.chipClassical to "Classical",
            binding.chipVocal to "Vocal"
        )

        presets.forEach { (button, presetName) ->
            button.setOnClickListener {
                viewModel.setPreset(presetName)
                updateUIFromVM()
            }
        }
    }

    private fun updateUIFromVM() {

        val seekBars = listOf(
            binding.band60,
            binding.band230,
            binding.band1k,
            binding.band3k,
            binding.band10k
        )

        seekBars.forEachIndexed { index, seekBar ->
            val level =
                viewModel.getBandLevel(index.toShort()).toInt()

            seekBar.progress =
                (level / mbPerStep) + seekBarCenter
        }

        binding.knobLow.progressValue =
            viewModel.getBassLevel()

        binding.knobHigh.progressValue =
            viewModel.getTrebleLevel()
    }

    /* ---------------- SAVE ---------------- */

    private fun setupSaveButton() {

        binding.cvSave.setOnClickListener {

            viewModel.applyChanges()

            Toast.makeText(
                requireContext(),
                "Equalizer settings saved",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
        }
    }

    /* ---------------- RESTORE ---------------- */

    private fun restoreUI() {
        updateUIFromVM()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}