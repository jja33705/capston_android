package com.example.capstonandroid

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.capstonandroid.databinding.SelectExerciseKindBottomSheetDialogBinding
import com.example.capstonandroid.databinding.SelectPostRangeBottomSheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

interface SelectPostRangeBottomSheetClickListener {
    fun onRadioButtonChanged(selectedId: Int)
}

class SelectPostRangeBottomSheetDialog(private val selectedId: Int) : BottomSheetDialogFragment() {

    private var _binding: SelectPostRangeBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    lateinit var selectPostRangeBottomSheetClickListener: SelectPostRangeBottomSheetClickListener

    companion object {
        fun newInstance(selectedId: Int): SelectPostRangeBottomSheetDialog {
            return SelectPostRangeBottomSheetDialog(selectedId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SelectPostRangeBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            selectPostRangeBottomSheetClickListener = context as SelectPostRangeBottomSheetClickListener
        } catch (e: Exception) {
            println("selectExerciseKindBottomSheetDialog: onAttach Error")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기값 세팅
        when (selectedId) {
            R.id.radio_button_public -> {
                binding.rangeRadioGroup.check(R.id.radio_button_public)
            }
            R.id.radio_button_private -> {
                binding.rangeRadioGroup.check(R.id.radio_button_private)
            }
        }

        // 선택한 라디오 버튼에 따라 분기처리
        binding.rangeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.radio_button_public -> {
                    selectPostRangeBottomSheetClickListener.onRadioButtonChanged(R.id.radio_button_public)
                    dismiss()
                }
                R.id.radio_button_private -> {
                    selectPostRangeBottomSheetClickListener.onRadioButtonChanged(R.id.radio_button_private)
                    dismiss()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}