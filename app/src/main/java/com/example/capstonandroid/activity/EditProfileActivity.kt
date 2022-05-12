package com.example.capstonandroid.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import com.example.capstonandroid.R
import com.example.capstonandroid.databinding.ActivityEditProfileBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {
    private var _binding: ActivityEditProfileBinding? = null
    private val binding get() = _binding!!

    private  lateinit var  retrofit: Retrofit  //레트로핏
    private  lateinit var supplementService: BackendApi // api

    private val sexList = arrayOf("男性", "女性", "トランスジェンダー", "知らせたくない")
    private val sexValueList = arrayOf("M", "F", "T", "N")
    private var selectedSexIndex = 0

    private var profileImageFile: File? = null

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
                    "M" -> 0
                    "F" -> 1
                    "T" -> 2
                    "N" -> 3
                    else -> -1
                }
                binding.etSex.setText(sexList[selectedSexIndex])

                binding.etBirth.setText(user.birth)
                binding.etWeight.setText(user.weight.toString())

                if (user.profile != null) {
                    val bitmap = withContext(Dispatchers.IO) {
                        convertBitmapFromURL(user.profile)!!
                    }
                    binding.circleImageViewProfileImage.setImageBitmap(bitmap)
                    val absolutePath = bitmapToFile(bitmap)
                    profileImageFile = File(absolutePath.path)
                }
            }
        }

        // 생년월일 눌렀을 때
        binding.etBirth.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                val decimalFormat = DecimalFormat("00")
                binding.etBirth.setText("$year-${decimalFormat.format(month + 1)}-${decimalFormat.format(day)}")
            }
            val datePickerDialog = DatePickerDialog(this, R.style.DatePickerDialog_Spinner, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONDAY), calendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
        }

        // 성별 눌렀을 때
        binding.etSex.setOnClickListener {
            AlertDialog.Builder(this).setTitle("性別").setSingleChoiceItems(sexList, selectedSexIndex) { dialogInterface, position ->
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
                profileImageFile = File(absolutePath.path)

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

        binding.btnSaveEditProfile.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val token = "Bearer ${getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")}"

                val profileImageRequestBody = profileImageFile!!.asRequestBody("image/*".toMediaTypeOrNull())
                val profileImageMultipartBody = MultipartBody.Part.createFormData("profile", profileImageFile!!.name, profileImageRequestBody)

                val editProfileResponse = supplementService.editProfile(
                    token,
                    profileImageMultipartBody,
                    binding.etName.text!!.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
                    binding.etBirth.text!!.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
                    binding.etIntroduce.text!!.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
                    binding.etLocation.text!!.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
                    sexValueList[selectedSexIndex].toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
                    binding.etWeight.text!!.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
                )
                if (editProfileResponse.isSuccessful) {
                    finish()
                }
            }
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

    private fun convertBitmapFromURL(url: String): Bitmap? {
        try {
            val url = URL(url)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream

            return BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            println("이미지 에러")
        }
        return null
    }
}