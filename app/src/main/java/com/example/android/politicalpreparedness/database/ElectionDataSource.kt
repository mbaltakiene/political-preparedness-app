package com.example.android.politicalpreparedness.database

import com.example.android.politicalpreparedness.network.models.Election

interface ElectionDataSource {
    suspend fun insert(election: Election)
    suspend fun getAllElections(): List<Election>
    suspend fun getElectionById(id: Int): Election?
    suspend fun deleteElectionById(id: Int)
    suspend fun deleteElections()
}