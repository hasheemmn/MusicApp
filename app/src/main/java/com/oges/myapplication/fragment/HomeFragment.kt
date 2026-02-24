package com.oges.myapplication.fragment

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
        setupClicks()
        observeUi()
        setupSeekBar()
    }

    private fun observeUi() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

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

                launch {
                    viewModel.position.collect {
                        binding.seekBar.progress = it.toInt()
                        binding.currentTime.text = formatTime(it)
                    }
                }

                launch {
                    viewModel.duration.collect {
                        binding.seekBar.max = it.toInt()
                        binding.totalTime.text = formatTime(it)
                    }
                }
            }
        }
    }

    private fun setupClicks() {
        binding.IdPlayPauseBtn.setOnClickListener {
            viewModel.onPlayPause()
        }
        binding.IdNextBtn.setOnClickListener {
            viewModel.onNext()
        }
        binding.IdPreviousBtn.setOnClickListener {
            viewModel.onPrevious()
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
                    if (fromUser)
                        viewModel.seekTo(progress.toLong())
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )
    }

    private fun formatTime(ms: Long): String {
        val sec = ms / 1000
        val min = sec / 60
        val remain = sec % 60
        return String.format("%d:%02d", min, remain)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}