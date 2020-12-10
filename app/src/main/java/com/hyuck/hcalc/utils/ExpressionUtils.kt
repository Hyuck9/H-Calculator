package com.hyuck.hcalc.utils

import android.text.TextUtils

object ExpressionUtils {

    fun getLeftParenCount(str: String) = StringUtils.getCharCountOfString(str, '(')

    fun getRightParenCount(str: String) = StringUtils.getCharCountOfString(str, ')')

    fun isOperandLengthCheck(expr: String): Boolean {
        val lists = separateOperator(NumberFormatter.removeCommaInString(expr))
        return lists.isEmpty() || lists.last().length < 15
    }

    fun isOperatorChar(c: Char): Boolean = "+−÷×".contains(c)
    fun isOperatorAndParensChar(c: Char): Boolean = "+−÷×()".contains(c)
    fun isNotOperatorChar(c: Char): Boolean = !isOperatorChar(c)

    fun isNumeric(s: String): Boolean {
        if (TextUtils.isEmpty(s)) return false
        s.forEach {
            if ( !Character.isDigit(it) && !".E일이삼사m".contains(it) ) return false
        }
        return true
    }

    fun separateOperator(expr: String): MutableList<String> {
        val mutableList = mutableListOf<String>()
        var offset = 0
        expr.forEachIndexed { index, c ->
            if (isOperatorAndParensChar(c)) {
                if (isNumeric(expr.substring(offset, index))) {
                    mutableList.add(expr.substring(offset, index))
                }
                mutableList.add(c.toString())
                offset = index + 1
            }
        }

        if (offset != expr.length) {
            mutableList.add(expr.substring(offset))
        }
        return mutableList
    }
}