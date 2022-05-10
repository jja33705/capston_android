package com.example.capstonandroid.activity

// 메인 액티비티

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonandroid.R
import com.example.capstonandroid.adapter.PostRecyclerViewAdapter
import com.example.capstonandroid.service.RecordService
import com.example.capstonandroid.service.TrackPaceMakeService
import com.example.capstonandroid.service.TrackRecordService
import com.example.capstonandroid.databinding.ActivityMainBinding
import com.example.capstonandroid.fragment.HomeFragment
import com.example.capstonandroid.fragment.MeFragment
import com.example.capstonandroid.network.dto.Comment
import com.example.capstonandroid.network.dto.Post

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var selectExerciseKindDialog: Dialog // 커스텀 다이얼로그


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 레코드 중이면 레코드 액티비티로 이동
        if (RecordService.isStarted) {
            val intent = Intent(this, RecordActivity::class.java)
            intent.putExtra("exerciseKind", RecordService.exerciseKind)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) // 액티비티 스택 내에 있으면 재실행 함
            startActivity(intent)
        }

        // 트랙 레코드 중이면 트랙 레코드 액티비티로 이동
        if (TrackRecordService.isStarted) {
            val intent = Intent(this, TrackRecordActivity::class.java)
            intent.putExtra("exerciseKind", TrackRecordService.exerciseKind)
            intent.putExtra("trackId", TrackRecordService.trackId)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) // 액티비티 스택 내에 있으면 재실행 함
            startActivity(intent)
        }

        // 트랙 페이스메이커 중이면 트랙 페이스메이커 액티비티로 이동
        if (TrackPaceMakeService.isStarted) {
            val intent = Intent(this, TrackPaceMakeActivity::class.java)
            intent.putExtra("exerciseKind", TrackPaceMakeService.exerciseKind)
            intent.putExtra("trackId", TrackPaceMakeService.trackId)
            intent.putExtra("matchType", TrackPaceMakeService.matchType)
            intent.putExtra("opponentGpsDataId", TrackPaceMakeService.opponentGpsDataId)
            intent.putExtra("opponentPostId", TrackPaceMakeService.opponentPostId)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) // 액티비티 스택 내에 있으면 재실행 함
            startActivity(intent)
        }

        checkAndStartActivityFromNotification(intent)

        // 커스텀 다이얼로그 초기화
        selectExerciseKindDialog = Dialog(this)
        selectExerciseKindDialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 제거
        selectExerciseKindDialog.setContentView(R.layout.select_kind_dialog)

        // 다이얼로그에서 라이딩 버튼 클릭했을 때
        val ridingButton: Button = selectExerciseKindDialog.findViewById(R.id.bt_riding)
        ridingButton.setOnClickListener{
            startRecordActivity("B")
        }

        // 다이얼로그에서 러닝 버튼 클릭했을 때
        val runningButton: Button = selectExerciseKindDialog.findViewById(R.id.bt_running)
        runningButton.setOnClickListener {
            startRecordActivity("R")
        }

        // 바텀 네비게이션에서 선택한 메뉴 아이디에 따라 표시할 화면 분기처리 (나중에 addToBackStack 부분 찾아보고 Transaction 관리해 줘야 할 것 같음.)
        binding.bottomNav.setOnItemSelectedListener {
            println(it.itemId)
            when (it.itemId) {
                R.id.homeFragment -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
                    supportActionBar?.title = "ホーム"
                }
                R.id.trackFragment -> {
//                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, TrackFragment()).commit()
                    val intent = Intent(this, SelectTrackActivity::class.java)
                    startActivity(intent)
                    return@setOnItemSelectedListener false
                }
                R.id.recordActivity -> {
                    showSelectExerciseKindDialog()
                    return@setOnItemSelectedListener false
                }
                R.id.meFragment -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, MeFragment()).commit()
                    supportActionBar?.title = "나"
                }
            }
            true
        }

        // 처음 들어왔을때는 homeFragment
        binding.bottomNav.selectedItemId = R.id.homeFragment
    }

    // 어떤 종류인지 선택하는 다이얼로그 띄움
    private fun showSelectExerciseKindDialog() {
        selectExerciseKindDialog.show()
        selectExerciseKindDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 모서리 둥글게 하기 위해서
    }

    // 선택한 운동 종류에 맞게 값을 넣고 실행
    private fun startRecordActivity(exerciseKind: String) {
        val intent = Intent(this, RecordActivity::class.java)
        intent.putExtra("exerciseKind", exerciseKind)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) // 액티비티 스택 내에 있으면 재실행 함
        startActivity(intent)
        selectExerciseKindDialog.dismiss() // 닫기
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item?.itemId){
            R.id.action_search -> {
                val intent = Intent(this,FollowerActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_notification -> {
                val intent = Intent(this, NotificationActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkAndStartActivityFromNotification(intent!!)
    }

    private fun checkAndStartActivityFromNotification(intent: Intent) {
        println("onNewIntent: ${intent?.getStringExtra("type")}")
        println("onNewIntent: ${intent?.getStringExtra("postId")}")
        println("onNewIntent: ${intent?.getStringExtra("userId")}")
        println("onNewIntent: ${intent?.getStringExtra("id")}")
        // 노티피케이션 관련된 곳으로 이동
        if (intent.getStringExtra("type") != null) {
            val intent = when (intent.getStringExtra("type")) {
                "follow", "followRequest" -> {
                    Intent(this, ProfileActivity::class.java).apply {
                        putExtra("userId", intent.getStringExtra("userId")?.toInt())
                    }
                }
                else -> {
                    Intent(this, PostActivity::class.java).apply {
                        putExtra("postId", intent.getStringExtra("postId")?.toInt())
                    }
                }
            }
            startActivity(intent)
        }
    }
}
