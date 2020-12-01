package com.hyuck.hcalc.extensions

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.hyuck.hcalc.evaluation.isOnMainThread

var toast: Toast? = null

fun Context.toast(id: Int, length: Int = Toast.LENGTH_SHORT) {
    toast(getString(id), length)
}

fun Context.toast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    if (isOnMainThread()) {
        doToast(this, msg, length)
    } else {
        Handler(Looper.getMainLooper()).post {
            doToast(this, msg, length)
        }
    }
}

private fun doToast(context: Context, message: String, length: Int) {
    toast?.cancel()
    if (context is Activity) {
        if (!context.isFinishing && !context.isDestroyed) {
            toast = Toast.makeText(context, message, length)
            toast!!.show()
        }
    } else {
        toast = Toast.makeText(context, message, length)
        toast!!.show()
    }
}