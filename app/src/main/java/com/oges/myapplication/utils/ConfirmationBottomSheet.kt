package com.oges.myapplication.utils

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.oges.myapplication.databinding.BottomSheetConfirmationBinding

class ConfirmationBottomSheet(
    private val title: String,
    private val message: String,
    private val onConfirm: () -> Unit,
    private val onCancel: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetConfirmationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = title
        binding.tvMessage.text = "Are you sure you want to $message?"


        binding.btnConfirm.setOnClickListener {
            onConfirm()
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            onCancel()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}