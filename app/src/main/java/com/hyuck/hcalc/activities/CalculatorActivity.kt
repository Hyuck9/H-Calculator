package com.hyuck.hcalc.activities

import android.animation.*
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hyuck.hcalc.R
import com.hyuck.hcalc.customviews.OnTextSizeChangeListener
import com.hyuck.hcalc.evaluation.*
import com.hyuck.hcalc.extensions.toast
import com.hyuck.hcalc.utils.ExpressionUtils.getLeftParenCount
import com.hyuck.hcalc.utils.ExpressionUtils.getRightParenCount
import com.hyuck.hcalc.utils.ExpressionUtils.isNotOperatorChar
import com.hyuck.hcalc.utils.ExpressionUtils.isOperandLengthCheck
import com.hyuck.hcalc.utils.ExpressionUtils.isOperatorChar
import kotlinx.android.synthetic.main.activity_calculator.*
import kotlinx.android.synthetic.main.layout_digit_button.*

class CalculatorActivity : AppCompatActivity(), ExpressionEvaluator.EvaluateCallback {

    lateinit var tokenizer: ExpressionTokenizer
    lateinit var evaluator: ExpressionEvaluator

    var currentState: State? = null

    private var currentAnimator: Animator? = null

    private var countLeftParen = 0
    private var countRightParen = 0
    private var lastInputCheck = 's'
    private var beforeInputCheck = 's'

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("CalculatorActivity", "onCreate")
        setContentView(R.layout.activity_calculator)
        tokenizer = ExpressionTokenizer(this)
        evaluator = ExpressionEvaluator(tokenizer)

        evaluator.evaluate(displayFormula.text.toString(), this)

        setDisplayFormula()
        btnDel.setOnLongClickListener{
            onClear()
            return@setOnLongClickListener true
        }
    }

    private fun setDisplayFormula() {
        displayFormula.setEditableFactory(formulaEditableFactory)
        displayFormula.addTextChangedListener(formulaTextWatcher)
        displayFormula.setOnKeyListener(formulaOnKeyListener)
        displayFormula.setOnTextSizeChangeListener(formulaOnTextSizeChangeListener)
    }

    private val formulaEditableFactory = object: Editable.Factory() {
        override fun newEditable(source: CharSequence?): Editable {
            val isEdited = (currentState == State.INPUT || currentState == State.ERROR)
            return ExpressionBuilder(source!!, tokenizer, isEdited)
        }
    }

    private val formulaTextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            setState(State.INPUT)
            evaluator.evaluate(s.toString(), this@CalculatorActivity)
        }
    }

    private val formulaOnKeyListener = View.OnKeyListener { _, keyCode, event ->
        if ( keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            if (event.action == KeyEvent.ACTION_UP) onEquals()
            true
        } else false
    }

    private val formulaOnTextSizeChangeListener = object: OnTextSizeChangeListener {
        override fun onTextSizeChanged(textView: TextView, oldSize: Float) {
            if (currentState != State.INPUT) return
            val textScale = oldSize / textView.textSize
            val translationX = (1 - textScale) * (textView.width / 2 - textView.paddingEnd)
            val translationY = (1 - textScale) * (textView.height / 2 - textView.paddingBottom)
            animatorSetStart(textView, textScale, translationX, translationY)
        }
    }
    private fun animatorSetStart(
        textView: TextView,
        textScale: Float,
        translationX: Float,
        translationY: Float
    ) {
        Log.d("CalculatorActivity", "animatorSetStart")
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(textView, "scaleX", textScale, 1.0f),
                ObjectAnimator.ofFloat(textView, "scaleY", textScale, 1.0f),
                ObjectAnimator.ofFloat(textView, "translationX", translationX, 1.0f),
                ObjectAnimator.ofFloat(textView, "translationY", translationY, 1.0f)
            )
            duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
            interpolator = AccelerateDecelerateInterpolator()
        }.start()
    }

    private fun setState(state: State) {
        if (currentState != state) {
            currentState = state
            if (state == State.ERROR) {
                // TODO: 추후 상태값에 따른 로직 반영
            }
        }
    }

    private fun onEquals() {
        if (currentState == State.INPUT) {
            setState(State.EVALUATE)
            evaluator.evaluate(displayFormula.text.toString(), this)
        }
    }

    private fun onDelete() {
        val formulaText = displayFormula.editableText
        val formulaLength = formulaText.length
        if (formulaLength > 0) formulaText.delete(formulaLength - 1, formulaLength)
    }

    private fun onClear() {
        if (TextUtils.isEmpty(displayFormula.text)) {
            return
        }
        displayFormula.editableText.clear()
    }

    private fun onError(errorResourceId: Int) {
        if (currentState != State.EVALUATE) {
            displayResult.setText(errorResourceId)
            return
        }
        setState(State.ERROR)
        displayResult.setText(errorResourceId)
    }

    override fun onEvaluate(expr: String?, result: String?, errorResourceId: Int) {
        when {
            currentState == State.INPUT -> {
                displayResult.text = result
            }
            errorResourceId != INVALID_RES_ID -> {
                onError(errorResourceId)
            }
            !TextUtils.isEmpty(result) -> {
                onResult(result!!)
            }
            currentState == State.EVALUATE -> {
                setState(State.INPUT)
            }
        }
        displayFormula.requestFocus()
    }

    @Suppress("unused")
    fun onButtonClick(v: View) {
        when(v.id) {
            R.id.btnEqual -> onEquals()
            R.id.btnDel -> onDelete()
            R.id.btnMemoryClr -> {}
            R.id.btnParens -> parensButtonClick()
            R.id.btnOperatorPlus,
            R.id.btnOperatorMinus,
            R.id.btnOperatorMultiply,
            R.id.btnOperatorDivision -> operatorButtonClick(v)
            R.id.btnDigit00 -> doubleOButtonClick(v)
            else -> numberButtonClick(v)
        }

        setInputChar()
    }

    private fun setInputChar() {
        beforeInputCheck = if (displayFormula.text!!.length <= 1) 's' else displayFormula.text.toString()[displayFormula.text!!.length - 2]
        lastInputCheck = if (displayFormula.text.toString() == "") 's' else displayFormula.text.toString()[displayFormula.text!!.length - 1]
        countLeftParen = getLeftParenCount(displayFormula.text.toString())
        countRightParen = getRightParenCount(displayFormula.text.toString())
    }

    private fun parensButtonClick() {
        if (displayFormula.text!!.length >= 499) {
            toast("글자를 최대 500자 까지 입력할 수 있습니다.")
        } else {
            if (isLeftParenCheck() && lastInputCheck != '(' && isNotOperatorChar(lastInputCheck) && lastInputCheck != 's') {
                displayFormula.append( getString(R.string.rparens) )
            } else if (lastInputCheck != '(' && isNotOperatorChar(lastInputCheck) && lastInputCheck != 's' && ExpressionBuilder.isEdited) {
                displayFormula.append("${getString(R.string.op_mul)}${getString(R.string.lparens)}")
            } else {
                displayFormula.append( getString(R.string.lparens) )
            }
        }
    }
    private fun isLeftParenCheck(): Boolean = countLeftParen - countRightParen > 0

    private fun operatorButtonClick(v: View) {
        //TODO: Error Check -> onClear()
        if (displayFormula.text!!.length >= 499) {
            toast("글자를 최대 500자 까지 입력할 수 있습니다.")
        } else {
            displayFormula.append((v as Button).text)
        }
    }

    private fun doubleOButtonClick(v: View) {
        if (displayFormula.text!!.length >= 499) {
            toast("글자를 최대 500자 까지 입력할 수 있습니다.")
        } else {
            if (isOperandLengthCheck(displayFormula.text.toString()) || currentState == State.RESULT) {
                if ( lastInputCheck != 's' && isNotOperatorChar(lastInputCheck) && ExpressionBuilder.isEdited ) {
                    if ( !((displayFormula.text.toString().length == 1 || isOperatorChar(beforeInputCheck)) && lastInputCheck == '0') ) {
                        displayFormula.append((v as Button).text)
                    }
                }
            } else {
                toast("최대자리수(15개)를 초과했습니다.")
            }
        }
    }

    private fun numberButtonClick(v: View) {
        if (displayFormula.text!!.length >= 500) {
            toast("글자를 최대 500자 까지 입력할 수 있습니다.")
        } else {
            if (isOperandLengthCheck(displayFormula.text.toString()) || currentState == State.RESULT) {
                if (lastInputCheck == ')' && currentState != State.RESULT) {
                    displayFormula.append("${getString(R.string.op_mul)}${(v as Button).text}")
                } else {
                    displayFormula.append((v as Button).text)
                }
            } else {
                toast("최대자리수(15개)를 초과했습니다.")
            }
        }
    }

    private fun onResult(result: String) {
        val resultScale = displayFormula.getVariableTextSize(result) / displayResult.textSize
        val resultTranslationX = (1.0f - resultScale) * (displayResult.width / 2.0f - displayResult.paddingEnd)
        val resultTranslationY = (1.0f - resultScale) * (displayResult.height / 2.0f - displayResult.paddingBottom) +
                (displayFormula.bottom - displayResult.bottom) +
                (displayResult.paddingBottom - displayFormula.paddingBottom - displayFormula.paddingTop)
        val formulaTranslationY = (-displayFormula.bottom).toFloat()

        val resultTextColor = displayResult.currentTextColor
        val formulaTextColor = displayFormula.currentTextColor

        val textColorAnimator = ValueAnimator.ofObject(
            ArgbEvaluator(),
            resultTextColor,
            formulaTextColor
        ).apply{
            addUpdateListener{
                displayResult.setTextColor(it.animatedValue as Int)
            }
        }

        val animatorSet = AnimatorSet().apply {
            playTogether(
                textColorAnimator,
                ObjectAnimator.ofFloat(displayResult, "scaleX", resultScale),
                ObjectAnimator.ofFloat(displayResult, "scaleY", resultScale),
                ObjectAnimator.ofFloat(displayResult, "translationX", resultTranslationX),
                ObjectAnimator.ofFloat(displayResult, "translationY", resultTranslationY),
                ObjectAnimator.ofFloat(displayFormula, "translationY", formulaTranslationY)
            )
            duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    displayResult.text = result
                }

                override fun onAnimationEnd(animation: Animator) {
                    // Reset all of the values modified during the animation.
                    displayResult.setTextColor(resultTextColor)
                    displayResult.scaleX = 1.0f
                    displayResult.scaleY = 1.0f
                    displayResult.translationX = 0.0f
                    displayResult.translationY = 0.0f
                    displayFormula.translationY = 0.0f

                    // Finally update the formula to use the current result.
                    displayFormula.setText(result)
                    setState(State.RESULT)
                    currentAnimator = null
                }
            })
        }

        currentAnimator = animatorSet
        animatorSet.start()

    }
}