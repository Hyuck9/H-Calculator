package com.hyuck.hcalc.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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
import kotlinx.android.synthetic.main.activity_calculator.*
import kotlinx.android.synthetic.main.layout_digit_button.*

class CalculatorActivity : AppCompatActivity(), ExpressionEvaluator.EvaluateCallback {

    lateinit var tokenizer: ExpressionTokenizer
    lateinit var evaluator: ExpressionEvaluator

    var currentState: State? = null


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
            animatorSetStart(textView,textScale, translationX, translationY)
        }
    }
    private fun animatorSetStart(textView: TextView, textScale: Float, translationX: Float, translationY: Float) {
        Log.d("CalculatorActivity", "animatorSetStart")
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(textView, "scaleX", textScale, 1f),
                ObjectAnimator.ofFloat(textView, "scaleY", textScale, 1f),
                ObjectAnimator.ofFloat(textView, "translationX", translationX, 1f),
                ObjectAnimator.ofFloat(textView, "translationY", translationY, 1f)
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
        if (formulaLength > 0) formulaText.delete(formulaLength-1, formulaLength)
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
            TextUtils.isEmpty(result) -> {
//                onResult(result)
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
            R.id.btnParens -> {}
            R.id.btnOperatorPlus, R.id.btnOperatorMinus, R.id.btnOperatorMultiply, R.id.btnOperatorDivision -> {operatorButtonClick(v)}
            R.id.btnDigit00 -> {}
            else -> {
                numberButtonClick(v)
            }
        }
    }

    private fun operatorButtonClick(v: View) {
        displayFormula.append((v as Button).text)
    }

    private fun numberButtonClick(v: View) {
        if (displayFormula.text!!.length >= 500) {
            toast("글자를 최대 500자 까지 입력할 수 있습니다.")
        } else {
            displayFormula.append((v as Button).text)
        }
    }

//    abstract fun onResult(result: String?)
}