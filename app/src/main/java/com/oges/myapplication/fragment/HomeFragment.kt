package com.oges.myapplication.fragment

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.oges.myapplication.R
import com.oges.myapplication.databinding.FragmentHomeBinding
import com.oges.myapplication.vm.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentHomeBinding.bind(view)

        setupSeekBar()
        observeUi()
        clickListeners()

        viewModel.onStart()
    }

    private fun observeUi() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.duration.collect { duration ->
                        binding.seekBar.max = duration
                        binding.totalTime.text = formatTime(duration)
                    }
                }

                launch {
                    viewModel.currentPosition.collect { position ->
                        binding.seekBar.progress = position
                        binding.currentTime.text = formatTime(position)
                    }
                }

                launch {
                    combine(
                        viewModel.songs,
                        viewModel.currentIndex
                    ) { songs, index ->
                        songs.getOrNull(index)
                    }.collect { song ->

                        song?.let {
                            binding.IdHomeNameTxt.text = it.title
                            binding.IdHomeDescriptionTxt.text = it.author

                            Glide.with(binding.root)
                                .load(it.coverImage)
                                .placeholder(R.color._444444)
                                .error(R.color._444444)
                                .into(binding.IdPodcastImageView)
                        }
                    }
                }

                launch {
                    viewModel.isPlaying.collect { playing ->
                        binding.IdPlayPauseImageView.setImageResource(
                            if (playing)
                                R.drawable.ic_white_pause_icon
                            else
                                R.drawable.ic_white_play_icon
                        )
                    }
                }
            }
        }
    }

    private fun setupSeekBar() {
        binding.seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        viewModel.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )
    }

    private fun clickListeners() {
        binding.IdPlayPauseBtn.setOnClickListener {
            viewModel.onPlayPause()
        }

        binding.IdNextBtn.setOnClickListener {
            viewModel.onNext()
        }
        binding.IdPreviousBtn.setOnClickListener{
            viewModel.onPrevious()
        }

        binding.icEquilizer.setOnClickListener {
            findNavController().navigate(
                R.id.action_homeFragment_to_equalizerFragment
            )
        }
    }

    private fun formatTime(ms: Int): String {
        val seconds = ms / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}