package com.mobile.test.application.core

import android.R
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.graphics.toColorInt
import com.google.android.material.snackbar.Snackbar
import com.mobile.test.application.databinding.DialogLoadingBinding

fun Context.showToast(msgString: String) {
    Toast.makeText(this, msgString, Toast.LENGTH_SHORT).show()
}

fun View.beVisible() {
    this.visibility = View.VISIBLE
}

fun View.beGone() {
    this.visibility = View.GONE
}

fun View.beInvisible() {
    this.visibility = View.INVISIBLE
}


fun View.snackBar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
}

private const val DEFAULT_DEBOUNCE_INTERVAL = 600L // ms

fun View.singleClick(
    debounceTime: Long = DEFAULT_DEBOUNCE_INTERVAL,
    onClick: (View) -> Unit
) {
    var lastClickTime = 0L

    setOnClickListener { view ->
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastClickTime >= debounceTime) {
            lastClickTime = currentTime
            onClick(view)
        }
    }
}

infix fun View.click(click: (View) -> Unit) {
    setOnClickListener {
        click(it)
    }
}

fun isPremium(): Boolean{
    return false
}



