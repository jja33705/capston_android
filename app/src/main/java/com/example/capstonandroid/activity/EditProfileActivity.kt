package com.example.capstonandroid.activity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityEditProfileBinding
import java.util.*

class EditProfileActivity : AppCompatActivity() {
    private var _binding: ActivityEditProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "プロフィール編集"

        binding.etBirth.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                println("고른 날짜: $year $month $day")
                binding.etBirth.setText("$year-${month + 1}-$day")
            }
            val datePickerDialog = DatePickerDialog(this, R.style.DatePickerDialog_Spinner, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONDAY), calendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
        }

        binding.etSex.setOnClickListener {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}