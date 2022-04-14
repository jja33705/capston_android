package com.example.capstonandroid.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.capstonandroid.db.entity.OpponentGpsData

@Dao
interface OpponentGpsDataDao {
    @Query("DELETE FROM opponent_gps_data")
    fun deleteAllOpponentGpsData()

    @Query("SELECT * FROM opponent_gps_data WHERE second = :second")
    fun getOpponentGpsDataBySecond(second: Int): OpponentGpsData

    @Insert
    fun insertOpponentGpsData(opponentGpsData: OpponentGpsData)

    @Query("SELECT * FROM opponent_gps_data")
    fun getAllOpponentGpsData(): List<OpponentGpsData>

    @Query("SELECT * FROM opponent_gps_data WHERE second <= :second")
    fun getOpponentGpsDataUntilSecond(second: Int): List<OpponentGpsData>

    @Query("SELECT EXISTS(SELECT * FROM opponent_gps_data WHERE second = :second)")
    fun isRecordExistsOpponentGpsData(second: Int): Boolean
}