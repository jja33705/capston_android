package com.example.capstonandroid.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.TypedValue
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityEditProfileBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class EditProfileActivity : AppCompatActivity() {
    private var _binding: ActivityEditProfileBinding? = null
    private val binding get() = _binding!!

    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    private val sexList = arrayOf("男性", "女性")
    private var selectedSexIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "プロフィール編集"

        initRetrofit()

        CoroutineScope(Dispatchers.Main).launch {
            val token = "Bearer ${getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")}"
            val getUserResponse = supplementService.getUser(token)
            if (getUserResponse.isSuccessful) {
                val user = getUserResponse.body()!!
                binding.etName.setText(user.name)
                binding.etLocation.setText(user.location)
                binding.etIntroduce.setText(user.introduce)

                selectedSexIndex = when (user.sex) {
                    "male" -> 0
                    "female" -> 1
                    else -> -1
                }
                binding.etSex.setText(sexList[selectedSexIndex])

                binding.etBirth.setText(user.birth)
                binding.etWeight.setText(user.weight.toString())
            }
        }

        // 생년월일 눌렀을 때
        binding.etBirth.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                binding.etBirth.setText("$year-${month + 1}-$day")
            }
            val datePickerDialog = DatePickerDialog(this, R.style.DatePickerDialog_Spinner, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONDAY), calendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
        }

        // 성별 눌렀을 때
        binding.etSex.setOnClickListener {
            AlertDialog.Builder(this).setTitle("성별").setSingleChoiceItems(sexList, selectedSexIndex) { dialogInterface, position ->
                selectedSexIndex = position
                binding.etSex.setText(sexList[selectedSexIndex])
                dialogInterface.cancel()
            }.show()
        }

        // 이미지 선택 후 답을 받는 콜백
        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            println("응답: ${result.resultCode}")

            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, result.data!!.data!!)
                val absolutePath = bitmapToFile(bitmap)
                val imageFile = File(absolutePath.path)

                // 미리보기 띄움
                binding.circleImageViewProfileImage.setImageURI(result.data!!.data)
            }
        }

        // 프로필 이미지 눌렀을 때
        binding.circleImageViewProfileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            activityResultLauncher.launch(intent)
        }
    }

    // 레트로핏 초기화
    private fun initRetrofit() {
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun bitmapToFile(bitmap: Bitmap): Uri {
        // Get the context wrapper
        val wrapper = ContextWrapper(applicationContext)

        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")

        try{
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }

        // Return the saved bitmap uri
        return Uri.parse(file.absolutePath)
    }
}