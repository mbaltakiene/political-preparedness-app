package com.example.android.politicalpreparedness.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg election: Election)

    @Query("SELECT * FROM election_table")
    fun getAllElections(): List<Election>

    @Query("SELECT * FROM election_table WHERE id = :id")
    fun getElectionById(id: Int): Election?

    @Query("DELETE FROM election_table WHERE id = :id")
    fun deleteElectionById(id: Int)

    @Query("DELETE FROM election_table")
    fun deleteElections()

}