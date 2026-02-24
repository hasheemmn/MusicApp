package com.oges.myapplication.vm


import androidx.lifecycle.ViewModel
import com.oges.myapplication.api.AudioRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EqualizerVM @Inject constructor(
    private val repository: AudioRepository
) : ViewModel() {






}