package com.example.android.politicalpreparedness.representative.adapter

import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.representative.model.Representative


@BindingAdapter("profileImage")
fun fetchImage(view: ImageView, src: String?) {
    src?.also {
        val uri = src.toUri().buildUpon().scheme("https").build()
        //Add Glide call to load image and circle crop
        Glide.with(view.context)
            .load(uri)
            .placeholder(R.drawable.ic_profile)
            .error(R.drawable.ic_profile)
            .centerCrop()
            .circleCrop()
            .into(view);
    } ?: run {
        view.setImageResource(R.drawable.ic_profile)
    }
}

@BindingAdapter("stateValue")
fun Spinner.setNewValue(value: String?) {
    val adapter = toTypedAdapter<String>(this.adapter as ArrayAdapter<*>)
    val position = when (adapter.getItem(0)) {
        is String -> adapter.getPosition(value)
        else -> this.selectedItemPosition
    }
    if (position >= 0) {
        setSelection(position)
    }
}

inline fun <reified T> toTypedAdapter(adapter: ArrayAdapter<*>): ArrayAdapter<T>{
    return adapter as ArrayAdapter<T>
}

@BindingAdapter("representativeListData")
fun bindRecyclerView(recyclerView: RecyclerView, data: LiveData<List<Representative>?>) {
    val adapter = recyclerView.adapter as RepresentativeListAdapter
    if (data.value!=null) {
        adapter.submitList(data.value)
    }
    else {
        adapter.submitList(ArrayList<Representative>())
    }
}