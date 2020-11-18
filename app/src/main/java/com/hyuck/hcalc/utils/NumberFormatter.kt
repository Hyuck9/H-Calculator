package com.hyuck.hcalc.utils

import java.text.DecimalFormat

object NumberFormatter {
    fun doubleToString(d: Double) = DecimalFormat().apply {
        maximumFractionDigits = 15
        isGroupingUsed = true
    }.format(d)

    fun stringToDouble(str: String) = str.replace(",", "").toDouble()

    fun addGroupingSeparators(str: String) = doubleToString(stringToDouble(str))

    @JvmStatic
    fun main(args: Array<String>) {
        println(doubleToString(245345345.123))
    }
}

fun Double.format(): String = NumberFormatter.doubleToString(this)