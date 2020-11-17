package com.hyuck.hcalc.customviews

import android.widget.TextView

interface OnTextSizeChangeListener {
    fun onTextSizeChanged(textView: TextView, oldSize: Float)
}