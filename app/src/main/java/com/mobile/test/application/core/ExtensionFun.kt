package com.mobile.test.application.core

import android.R
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
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


private var lastNavigationTime = 0L
fun Fragment.safeNavigate(
    destinationId: Int,
    bundle: Bundle? = null,
    singleTop: Boolean = true,
    withAnimations: Boolean = true,
    retryDelay: Long = 200L,       // delay before retry
    maxRetries: Int = 3             // max retry attempts
) {


    val navController = findNavController()
    val handler = Handler(Looper.getMainLooper())

    var attempt = 0

    fun tryNavigate() {
        attempt++

        // 1️⃣ Prevent very fast double navigation
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNavigationTime < 300) return
        lastNavigationTime = currentTime

        // 2️⃣ Prevent navigating to the same destination
        if (navController.currentDestination?.id == destinationId) return


        // 3️⃣ Build NavOptions
        val navOptionsBuilder = NavOptions.Builder()
        if (withAnimations) {
            navOptionsBuilder

        }
        if (singleTop) {
            navOptionsBuilder.setLaunchSingleTop(true)
        }
        val navOptions = navOptionsBuilder.build()

        // 4️⃣ Check destination exists in graph
        if (navController.graph.findNode(destinationId) == null) return

        // 5️⃣ Try navigation with retry
        try {
            navController.navigate(destinationId, bundle, navOptions)
        } catch (e: Exception) {
            if (attempt <= maxRetries) {
                handler.postDelayed({ tryNavigate() }, retryDelay)
            } else {
                e.printStackTrace() // log final failure
            }
        }
    }

    tryNavigate()




}

//fun isPremium(): Boolean{
//    return false
//}



