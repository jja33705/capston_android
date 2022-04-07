package com.example.capstonandroid

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.capstonandroid.databinding.SelectExerciseKindBottomSheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

interface SelectExerciseKindBottomSheetClickListener {
    fun onRadioButtonChanged(selectedId: Int)
}

class SelectExerciseKindBottomSheetDialog(private val selectedId: Int) : BottomSheetDialogFragment() {

    private var _binding: SelectExerciseKindBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    lateinit var selectExerciseKindBottomSheetClickListener: SelectExerciseKindBottomSheetClickListener

    companion object {
        fun newInstance(selectedId: Int): SelectExerciseKindBottomSheetDialog {
            return SelectExerciseKindBottomSheetDialog(selectedId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SelectExerciseKindBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            selectExerciseKindBottomSheetClickListener = context as SelectExerciseKindBottomSheetClickListener
        } catch (e: Exception) {
            println("selectExerciseKindBottomSheetDialog: onAttach Error")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기값 세팅
        when (selectedId) {
            R.id.radio_button_running -> {
                binding.exerciseKindRadioGroup.check(R.id.radio_button_running)
            }
            R.id.radio_button_cycling -> {
                binding.exerciseKindRadioGroup.check(R.id.radio_button_cycling)
            }
        }

        // 선택한 라디오 버튼에 따라 분기처리
        binding.exerciseKindRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.radio_button_running -> {
                    selectExerciseKindBottomSheetClickListener.onRadioButtonChanged(R.id.radio_button_running)
                    dismiss()
                }
                R.id.radio_button_cycling -> {
                    selectExerciseKindBottomSheetClickListener.onRadioButtonChanged(R.id.radio_button_cycling)
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