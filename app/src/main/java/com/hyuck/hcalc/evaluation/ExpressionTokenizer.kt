package com.hyuck.hcalc.evaluation

import android.content.Context
import com.hyuck.hcalc.R
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.collections.HashMap

class ExpressionTokenizer(context: Context) {

    private val replacementMap = HashMap<String, String>()

    init {
        val symbols = DecimalFormatSymbols(Locale.US)
        val zeroDigit = symbols.zeroDigit
        for (i in 0..9) { replacementMap[i.toString()] = ((i + zeroDigit.toInt()).toChar()).toString() }
        replacementMap["."] = symbols.decimalSeparator.toString()
        replacementMap["/"] = context.getString(R.string.op_div)
        replacementMap["*"] = context.getString(R.string.op_mul)
        replacementMap["-"] = context.getString(R.string.op_sub)
        replacementMap["Infinity"] = context.getString(R.string.infinity)
    }

    fun getNormalizedExpression(expr: String): String {
        var normalizedExpression = expr
        replacementMap.entries.forEach { normalizedExpression = normalizedExpression.replace(it.value, it.key) }
        return normalizedExpression
    }

    fun getLocalizedExpression(expr: String): String {
        var localizedExpression = expr
        replacementMap.entries.forEach { localizedExpression = localizedExpression.replace(it.key, it.value) }
        return localizedExpression
    }
}