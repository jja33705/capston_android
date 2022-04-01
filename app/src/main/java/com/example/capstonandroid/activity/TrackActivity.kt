package com.example.capstonandroid.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.example.capstonandroid.databinding.ActivityTrackBinding
import com.example.capstonandroid.network.RetrofitClient
import com.example.capstonandroid.network.api.BackendApi
import com.example.capstonandroid.network.dto.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class TrackActivity : AppCompatActivity() {
    private var _binding: ActivityTrackBinding? = null
    private val binding get() = _binding!!

    private lateinit var retrofit: Retrofit // 레트로핏 인스턴스
    private lateinit var supplementService: BackendApi // api

    private lateinit var trackId: String
    private lateinit var exerciseKind: String

    private lateinit var track: Track

    companion object {
        const val RANK_MATCHING_ACTIVITY_REQUEST_CODE = 888
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityTrackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 액티비티 이동 후 답을 받는 콜백
        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // 액티비티에서 응답 왔을 때
            if (result.resultCode == RANK_MATCHING_ACTIVITY_REQUEST_CODE) {
                val responseIntent = result.data!!

                // 매치 성공했는지 아닌지에 따라 분기처리
                if (responseIntent.getBooleanExtra("matchSuccess", false)) {
                    println(responseIntent.getStringExtra("gpsDataId"))

                    val intent = Intent(this, TrackPaceMakeActivity::class.java)
                    intent.putExtra("matchType", "랭크")
                    intent.putExtra("exerciseKind", exerciseKind)
                    intent.putExtra("trackId", trackId)
                    intent.putExtra("opponentGpsDataId", responseIntent.getStringExtra("opponentGpsDataId"))
                    intent.putExtra("opponentPostId", responseIntent.getIntExtra("opponentPostId", 0))
                    startActivity(intent)
                    finish()
                } else {
                    println("실패")
                }
            }
        }

        val intent = intent
        trackId = intent.getStringExtra("trackId")!!
        exerciseKind = intent.getStringExtra("exerciseKind")!!

        initRetrofit()

        CoroutineScope(Dispatchers.Main).launch {
            val token = "Bearer " + getSharedPreferences("other", MODE_PRIVATE).getString("TOKEN", "")!!
            // 트랙 가져오기
            val trackResponse = supplementService.getTrack(token, trackId)
            if (trackResponse.isSuccessful) {
                track = trackResponse.body()!!

                binding.tvTitle.text = track.trackName
                binding.tvDistance.text = "${track.totalDistance}km"
                binding.tvDescription.text = track.description

                // 랭킹 가져오기
                val rankingResponse = supplementService.getRanking(token, trackId)
                if (rankingResponse.isSuccessful) {
                    println("${rankingResponse.body()!!}")
                }
            } else {
                // 통신에러발생했을경우 처리해야함
            }
        }

        // 혼자하기 눌렀을때 리스너 등록
        binding.buttonNormal.setOnClickListener {
            val intent = Intent(this, TrackRecordActivity::class.java)
            intent.putExtra("trackId", trackId)
            intent.putExtra("exerciseKind", exerciseKind)
            startActivity(intent)
            finish()
        }
        // 친선전 눌렀을때 리스너 등록
        binding.buttonFriendly.setOnClickListener {
            val intent = Intent(this, TrackPaceMakeActivity::class.java)
            intent.putExtra("trackId", trackId)
            intent.putExtra("matchType", "친선")
            intent.putExtra("exerciseKind", exerciseKind)
            startActivity(intent)
            finish()
        }
        // 랭크전 눌렀을때 리스너 등록
        binding.buttonRank.setOnClickListener {
            val intent = Intent(this, RankMatchingActivity::class.java)
            intent.putExtra("trackId", trackId)
            activityResultLauncher.launch(intent)
        }
    }

    // 레트로핏 초기화
    private fun initRetrofit() {
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(BackendApi::class.java)
    }
}