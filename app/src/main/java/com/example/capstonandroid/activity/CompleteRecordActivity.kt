package com.example.capstonandroid.activity

// 레코드 완료 후 뜨는 액티비티

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.TypedValue
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.capstonandroid.R
import com.example.capstonandroid.SelectPostRangeBottomSheetClickListener
import com.example.capstonandroid.SelectPostRangeBottomSheetDialog
import com.example.capstonandroid.databinding.ActivityCompleteRecordBinding
import com.example.capstonandroid.db.AppDatabase
import com.example.capstonandroid.db.dao.GpsDataDao
import com.example.capstonandroid.db.entity.GpsData
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.PostRecordGpsData
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList

class CompleteRecordActivity : AppCompatActivity(), SelectPostRangeBottomSheetClickListener {

    private var _binding: ActivityCompleteRecordBinding? = null
    private val binding get() = _binding!!

    private lateinit var retrofit: Retrofit // 레트로핏 인스턴스
    private lateinit var supplementService: BackendApi // api

    private lateinit var gpsDataDao: GpsDataDao // db dao 핸들

    private lateinit var gpsDataList: List<GpsData>

    // postRecordGpsData 에 넣을 하나하나의 리스트들
    private lateinit var speedList: ArrayList<Float>
    private lateinit var gpsList: ArrayList<List<Double>>
    private lateinit var altitudeList: ArrayList<Double>
    private lateinit var distanceList: ArrayList<Double>
    private lateinit var timeList: ArrayList<Int>

    private lateinit var postRecordGpsData: PostRecordGpsData // 총 합친 gps 데이터

    private var range = "public" // 공개 범위

    private lateinit var matchType: String // 매치 타입

    private lateinit var exerciseKind: String // 운동 종류

    private lateinit var mapImageFile: File
    private lateinit var imageList: ArrayList<File>
    private lateinit var imageRequestBodyList: ArrayList<MultipartBody.Part>

    // null 인지 아닌지에 따라 달라야 하니까 따로 선언
    private var trackIdRequestBody: RequestBody? = null
    private var opponentPostIdRequestBody: RequestBody? = null

    private var second: Int = 0 // 시간
    private var sumAltitude = 0.0 // 누적 상승 고도
    private var distance = 0.0 // 거리
    private var avgSpeed = 0.0 // 평균 속도
    private var kcal = 0.0 // 칼로리
    private var trackId: String? = null // 트랙 아이디
    private var opponentPostId: Int? = null // 상대 포스트 아이디

    private var loadedGpsData = false // gps 데이터 로딩 완료했는지

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCompleteRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRetrofit()

        supportActionBar?.title = "活動作成" // 액션바 텍스트

        val db = AppDatabase.getInstance(applicationContext)!!
        gpsDataDao = db.gpsDataDao()

        imageList = ArrayList()
        imageRequestBodyList = ArrayList()

        speedList = ArrayList()
        gpsList = ArrayList()
        altitudeList = ArrayList()
        distanceList = ArrayList()
        timeList = ArrayList()

        // db에 저장돼 있는 gps 데이터 불러옴
        CoroutineScope(Dispatchers.Main).launch {
            gpsDataList = withContext(Dispatchers.IO) {
                gpsDataDao.getAllGpsData()
            }
            if (gpsDataList.size < 2) {
                var alertDialog = AlertDialog.Builder(this@CompleteRecordActivity)
                    .setTitle("안내")
                    .setMessage("움직임이 감지되지 않습니다.")
                    .setPositiveButton("확인") { _, _ ->
                        finish()
                    }
                    .create()
                alertDialog.show()
            }
            for (gpsData in gpsDataList) {
                speedList.add(gpsData.speed)
                gpsList.add(listOf(gpsData.lng, gpsData.lat))
                altitudeList.add(gpsData.altitude)
                distanceList.add(gpsData.distance)
                timeList.add(gpsData.second)
            }
            println("gps data 길이: ${gpsDataList.size}")

            postRecordGpsData = PostRecordGpsData(speedList, gpsList, altitudeList, distanceList, timeList)

            println("이게 뭐지" + postRecordGpsData.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))

            loadedGpsData = true
        }


        val intent = intent
        second = intent.getIntExtra("second", 0)
        println("intent 넘어옴 (second): $second")
        sumAltitude = intent.getDoubleExtra("sumAltitude", 0.0)
        println("intent 넘어옴 (sumAltitude): $sumAltitude")
        distance = intent.getDoubleExtra("distance", 0.0)
        println("intent 넘어옴 (distance): $distance")
        avgSpeed = intent.getDoubleExtra("avgSpeed", 0.0)
        println("intent 넘어옴 (avgSpeed): $avgSpeed")
        kcal = intent.getDoubleExtra("kcal", 0.0)
        println("intent 넘어옴 (kcal): $kcal")
        trackId = intent.getStringExtra("trackId")
        println("intent 넘어옴 (trackId): $trackId")
        trackIdRequestBody = if (trackId!= null) trackId.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()) else null
        matchType = intent.getStringExtra("matchType")!!
        println("intent 넘어옴 (matchType): $matchType")
        exerciseKind = intent.getStringExtra("exerciseKind")!!
        println("intent 넘어옴 (exerciseKind): $exerciseKind")
        opponentPostId = intent.getIntExtra("opponentPostId", 0)
        println("intent 넘어옴 (opponentPostId): $opponentPostId")
        opponentPostIdRequestBody = if (opponentPostId != 0) opponentPostId.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()) else null

        // 이미지 선택 후 답을 받는 콜백
        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            println("응답: ${result.resultCode}")

            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, result.data!!.data!!)
                val absolutePath = bitmapToFile(bitmap)
                val imageFile = File(absolutePath.path)
                imageList.add(imageFile)

                // 미리보기 띄움
                val imageView = ImageView(this)
                imageView.setImageURI(result.data!!.data)
                val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 130F, resources.displayMetrics).toInt()
                val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90F, resources.displayMetrics).toInt()
                val layoutParam = LinearLayout.LayoutParams(width, height)
                layoutParam.rightMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10F, resources.displayMetrics).toInt()
                imageView.layoutParams = layoutParam
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.background = getDrawable(R.drawable.image_view_background)
                imageView.clipToOutline = true
                binding.linearLayoutImage.addView(imageView, imageList.size)
            }
        }

        // 이미지 업로드 버튼 초기화
        binding.uploadImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            activityResultLauncher.launch(intent)
        }

        // 공개 버튼 초기화
        binding.btnRange.setOnClickListener {
            val selectedId = when (range) {
                "public" -> {
                    R.id.radio_button_public
                }
                "private" -> {
                    R.id.radio_button_private
                }
                else -> {
                    0
                }
            }
            SelectPostRangeBottomSheetDialog.newInstance(selectedId).show(supportFragmentManager, "SelectPostRangeBottomSheetDialog")
        }

        // 등록 버튼 눌렀을 때
        binding.btPost.setOnClickListener{

            // db에 있는 gps 데이터 다 로딩했는지에 따라 분기처리
            if (loadedGpsData) {
                CoroutineScope(Dispatchers.Main).launch {
                    val token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")!!
                    val gson = GsonBuilder().create()
                    val postRecordGpsDataJsonString = gson.toJson(postRecordGpsData) // json 으로 파싱한 문자열로 보내줄 거임

                    // 이미지 하나하나 FormData 에 배열로 넣어줌
                    imageList.forEachIndexed { index, image ->
                        val imageRequestBody = image.asRequestBody("image/*".toMediaTypeOrNull())
                        val imageMultipartBody = MultipartBody.Part.createFormData("img[$index]", image.name, imageRequestBody)
                        imageRequestBodyList.add(imageMultipartBody)
                    }

                    val mapImageRequestBody = mapImageFile.asRequestBody("image/*".toMediaTypeOrNull())
                    val mapImageMultipartBody = MultipartBody.Part.createFormData("mapImg[0]", mapImageFile.name, mapImageRequestBody)

                    val postActivityResponse = supplementService.postRecordActivity(
                        token,
                        mapImageMultipartBody,
                        imageRequestBodyList,
                        avgSpeed.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
                        sumAltitude.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
                        kcal.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
                        binding.etDescription.text.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
                        distance.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
                        exerciseKind.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
                        matchType.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
                        range.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
                        second.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
                        binding.etTitle.text.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
                        trackIdRequestBody,
                        opponentPostIdRequestBody,
                        postRecordGpsDataJsonString.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                    )

                    println(postActivityResponse.code())

                    if (postActivityResponse.isSuccessful) {
                        println(postActivityResponse.body())
                        println(postActivityResponse.message())
                        println(postActivityResponse.errorBody())
                        val intent = Intent(this@CompleteRecordActivity, PostActivity::class.java)
                        intent.putExtra("postId", postActivityResponse.body()!!.postId)
                        startActivity(intent)
                        finish()
                    } else {
                        println(postActivityResponse.message())
                        println(postActivityResponse.errorBody())
                        println(postActivityResponse.toString())
                        Toast.makeText(this@CompleteRecordActivity, "저장 실패", Toast.LENGTH_SHORT)
                    }
                }
            } else {
                Toast.makeText(this@CompleteRecordActivity, "활동 데이터 로드 중. 잠시 후 다시 시도하세요", Toast.LENGTH_SHORT)
            }
        }

        // 맵 이미지 추가
        println("이미지 불러오기 시작")
        val bitmap = BitmapFactory.decodeFile("$cacheDir/map.png")
        val absolutePath = bitmapToFile(bitmap)
        mapImageFile = File(absolutePath.path)


        // 맵 이미지 미리보기 띄움
        val imageView = ImageView(this)
        imageView.setImageBitmap(bitmap)
        val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 130F, resources.displayMetrics).toInt()
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90F, resources.displayMetrics).toInt()
        val layoutParam = LinearLayout.LayoutParams(width, height)
        layoutParam.rightMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10F, resources.displayMetrics).toInt()
        imageView.layoutParams = layoutParam
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.background = getDrawable(R.drawable.image_view_background)
        imageView.clipToOutline = true
        binding.linearLayoutImage.addView(imageView, 0)
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
        var file = wrapper.getDir("Images",Context.MODE_PRIVATE)
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

    override fun onRadioButtonChanged(selectedId: Int) {
        println("선택된 것: $selectedId")

        when (selectedId) {
            R.id.radio_button_public -> {
                range = "public"
                binding.btnRange.text = "全員"
                binding.btnRange.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.icon_public, null), null, null, null)
            }
            R.id.radio_button_private -> {
                range = "private"
                binding.btnRange.text = "自分のみ"
                binding.btnRange.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.icon_private, null), null, null, null)
            }
        }

        println("선택했을 때: $range")
    }
}