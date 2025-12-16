package com.ads.adsmodule.ads.utils

import android.R
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ads.adsmodule.BuildConfig
import com.ads.adsmodule.databinding.DialogLoadingBinding

fun isPremium(): Boolean {
    return false
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



private var loadingDialog: Dialog? = null

fun Activity.showLoading(message: String = "") {
    // Dismiss any existing one first
    if (isFinishing || isDestroyed) return
    loadingDialog?.dismiss()
    val binding = DialogLoadingBinding.inflate(LayoutInflater.from(this))
    loadingDialog = Dialog(this, R.style.Theme_Translucent_NoTitleBar).apply {
        setContentView(binding.root)
        setCancelable(false)
        if (!this@showLoading.isFinishing && !this@showLoading.isDestroyed) {
            show()
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }
}

fun Context.dismissLoading() {
    val activity = this as? Activity ?: return
    val dialog = loadingDialog
        ?: return

    // If activity is dead, don't try to dismiss
    if (activity.isFinishing || activity.isDestroyed) {
        loadingDialog = null
        return
    }

    try {
        if (dialog.isShowing) {
            dialog.dismiss()   // safely dismiss
        }
    } catch (_: Exception) {
        // swallow exception safely
    } finally {
        loadingDialog = null  // ALWAYS clear reference
    }
}


private var appOpenLoadingDialog: Dialog? = null


fun Activity.isWindowAttached(): Boolean {
    val decor = window?.decorView
    return decor != null && decor.isAttachedToWindow
}


private const val SHOW_RETRY_MAX = 2
private const val SHOW_RETRY_DELAY_MS = 100L
private val mainHandler = Handler(Looper.getMainLooper())

fun Activity.safeShowAppOpenLoading(retries: Int = 0) {
    if (isFinishing || isDestroyed) return

    // If window is not attached, retry a few times, else skip showing.
    if (!isWindowAttached()) {
        if (retries < SHOW_RETRY_MAX) {
            mainHandler.postDelayed({ safeShowAppOpenLoading(retries + 1) }, SHOW_RETRY_DELAY_MS)
        }
        return
    }

    // Dismiss any existing (guarded)
    safeDismissAppOpenLoading()

    val binding = DialogLoadingBinding.inflate(LayoutInflater.from(this))
    appOpenLoadingDialog = Dialog(this, R.style.Theme_Translucent_NoTitleBar).apply {
        setContentView(binding.root)
        setCancelable(false)
        try {
            // Double-check again before showing
            if (!this@safeShowAppOpenLoading.isFinishing && !this@safeShowAppOpenLoading.isDestroyed && isWindowAttached()) {
                show()
                window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        } catch (t: Throwable) {
            // swallow; don't crash the app if show() fails
            t.printStackTrace()
            appOpenLoadingDialog = null
        }
    }
}
fun Activity.safeDismissAppOpenLoading() {
    val dialog = appOpenLoadingDialog ?: return

    // If activity window is not attached, avoid calling dismiss on that dialog directly.
    val canDismiss = try {
        // If dialog has a window and that window's decorView is attached -> safe to dismiss
        val decor = dialog.window?.decorView
        decor?.isAttachedToWindow == true
    } catch (t: Throwable) {
        false
    }

    if (!canDismiss) {
        // try one last safe dismiss inside try/catch in case platform state differs
        try {
            if (dialog.isShowing) dialog.dismiss()
        } catch (t: Throwable) {
            // swallow; dialog already detached
        } finally {
            appOpenLoadingDialog = null
        }
        return
    }

    try {
        if (dialog.isShowing) dialog.dismiss()
    } catch (t: Throwable) {
        // swallow exception (common on window detach races)
    } finally {
        appOpenLoadingDialog = null
    }
}


fun logD(tag: String = "DEFAULT_TAG", message: String = "") {
    if (BuildConfig.DEBUG) {
        Log.d(tag, message)
    }
}

