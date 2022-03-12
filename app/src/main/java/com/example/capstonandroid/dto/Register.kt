package com.example.capstonandroid.dto

data class Register(
    val name: String, //이름 있음
    val email: String, //이메일 있음
    val password: String, //비밀번호 있음
    val sex: String, //성별 있음
    val weight: String, //몸무게 있음
    val profile: String, //사진 없음
    val birth: String, //생년 있음
    val introduce: String, // 자기소개 없음
    val location: String, // 지역 없음
)