package com.hyuck.hcalc.evaluation

import android.util.Log
import com.hyuck.hcalc.R
import com.hyuck.hcalc.utils.NumberFormatter
import org.javia.arity.Symbols
import org.javia.arity.SyntaxException

class ExpressionEvaluator(var tokenizer: ExpressionTokenizer?) {

    private val symbols = Symbols()

    fun evaluate(expr: CharSequence, callback: EvaluateCallback) {
        evaluate(expr.toString(), callback)
    }

    fun evaluate(expression: String, callback: EvaluateCallback) {
        val expr = removeAnyTrailingOperators( tokenizer!!.getNormalizedExpression(expression) )

        if (isEmptyOrSimpleNumber(expr, callback)) return

        evaluateResult(expr, callback)
    }

    private fun removeAnyTrailingOperators(expr: String): String {
        var expression = expr
        while (expression.isNotEmpty() && "+-/*".contains(expression.last())) {
            expression = expression.substring(0, expression.length - 1)
        }
        return expression
    }

    private fun isEmptyOrSimpleNumber(expr: String, callback: EvaluateCallback): Boolean {
        try {
            if (expr.isEmpty() || expr.toDoubleOrNull() != null) {
                callback.onEvaluate(expr, null, INVALID_RES_ID)
                return true
            }
        } catch (e: NumberFormatException) {
            Log.w("ExpressionEvaluator", "expr is Not a Simple Number", e)
        }
        return false
    }

    private fun evaluateResult(expr: String, callback: EvaluateCallback) {
        try {
            val result = symbols.eval(expr)
            if (result.isNaN()) {
                callback.onEvaluate(expr, null, R.string.error_nan)
            } else {

                val resultString = tokenizer!!.getLocalizedExpression(NumberFormatter.doubleToString(result))
                callback.onEvaluate(expr, resultString, INVALID_RES_ID)
            }
        } catch (e: SyntaxException) {
            callback.onEvaluate(expr, null, R.string.error_syntax)
        }
    }


    interface EvaluateCallback {
        fun onEvaluate(expr: String?, result: String?, errorResourceId: Int)
    }

}