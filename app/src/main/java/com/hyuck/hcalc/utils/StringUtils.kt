package com.hyuck.hcalc.utils

object StringUtils {

    fun getCharCountOfString(str: String, find: Char): Int {
        var count = 0
        for (element in str) {
            if (find == element) {
                count++
            }
        }
        return count
    }

}