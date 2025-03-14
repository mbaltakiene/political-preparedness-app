package com.example.android.politicalpreparedness.network.jsonadapter


import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val PATTERN = "yyyy-MM-dd"

class DateAdapter {

    @FromJson
    fun dateFromJson (date: String): Date {
        return SimpleDateFormat(PATTERN, Locale.getDefault()).parse(date)!!
    }

    @ToJson
    fun dateToJson (date: Date): String {
        return date.toString()
    }
}