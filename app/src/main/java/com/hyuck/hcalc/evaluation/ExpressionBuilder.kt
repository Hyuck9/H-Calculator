package com.hyuck.hcalc.evaluation

import android.text.SpannableStringBuilder
import android.text.TextUtils

//TODO: 추후 isEdited 및 State enum class에 대해 살펴보기
class ExpressionBuilder(text: CharSequence, private val tokenizer: ExpressionTokenizer, isEdited: Boolean): SpannableStringBuilder(text) {

    companion object {
        var isEdited: Boolean = false
    }

    init {
        ExpressionBuilder.isEdited = isEdited
    }


    override fun replace(start: Int, end: Int, tb: CharSequence?, tbstart: Int, tbend: Int): SpannableStringBuilder {
        var startIndex = start
        if (startIndex != length || end != length) {
            isEdited = true
            return super.replace(startIndex, end, tb, tbstart, tbend)
        }

        var appendExpr = tokenizer.getNormalizedExpression(tb?.subSequence(tbstart, tbend).toString())

        if (appendExpr.length == 1) {
            val expr = tokenizer.getNormalizedExpression(toString())

            when (appendExpr[0]) {
                '.' -> {
                    val index = expr.lastIndexOf('.')
                    if (index != -1 && TextUtils.isDigitsOnly(expr.substring(index + 1, startIndex))) {
                        appendExpr = ""
                    }
                }

                '+', '*', '/' -> {
                    if (startIndex == 0) {
                        appendExpr = ""
                    }

                    while (startIndex > 0 && "+-*/".indexOf(expr[startIndex - 1]) != -1) {
                        --startIndex
                    }
                    if (startIndex > 0 && "+-".indexOf(expr[startIndex - 1]) != -1) {
                        --startIndex
                    }

                    isEdited = true
                }

                '-' -> {
                    if (startIndex > 0 && "+-".indexOf(expr[startIndex - 1]) != -1) {
                        --startIndex
                    }
                    isEdited = true
                }
            }
        }

        if (!isEdited && appendExpr.isNotEmpty()) {
            startIndex = 0
            isEdited = true
        }

        appendExpr = tokenizer.getLocalizedExpression(appendExpr)
        return super.replace(startIndex, end, appendExpr, 0, appendExpr.length)
    }
}