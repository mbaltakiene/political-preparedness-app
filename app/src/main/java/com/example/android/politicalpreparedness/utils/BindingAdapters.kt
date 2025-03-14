package com.example.android.politicalpreparedness.utils

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.network.models.Election
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: LiveData<List<Election>?>) {
    val adapter = recyclerView.adapter as ElectionListAdapter
    adapter.submitList(data.value)
}

@BindingAdapter("electionDay")
fun bindElectionDate(textView: TextView, electionDate: Date) {
    val sdf = SimpleDateFormat("EEE MMM dd HH':'mm':'ss z yyyy", Locale.US)
    textView.text = sdf.format(electionDate)
}

@BindingAdapter("showLoading")
fun bindLoadingImage(imageView: ImageView, isLoading: Boolean) {
    if ( isLoading ) {
        imageView.visibility = View.VISIBLE
    }
    else {
        imageView.visibility = View.GONE
    }
}


@BindingAdapter("followedElection")
fun bindSaveElectionButton(button: Button, isFollowed: LiveData<Boolean>) {
    if (isFollowed.value == true) {
        button.text = ContextCompat.getString( button.context, R.string.unfollow_election)
    }
    else {
        button.text = ContextCompat.getString( button.context, R.string.follow_election)
    }
}