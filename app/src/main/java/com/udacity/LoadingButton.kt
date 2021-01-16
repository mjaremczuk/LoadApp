package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.animation.addListener
import androidx.core.content.withStyledAttributes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var text: String? = null
    private var progressText: String? = null
    private var animationDuration: Long = 500
    private var drawAngle = 0F
    private val textBounds = Rect()

    private var completedColor = Color.GREEN
    private var progressColor = Color.RED
    private var ovalColor = Color.YELLOW

    private var textToDraw: String? = null
    private var progressRect = Rect(0, 0, 0, 0)
    private var ovalRectF = RectF(0F, 0F, 0F, 0F)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 24F
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
        typeface = Typeface.SANS_SERIF
    }
    private val valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (new) {
            ButtonState.Clicked -> showSelectedStateAndStartAnimating()
            ButtonState.Loading -> showProgress()
            ButtonState.Completed -> setProgressToZero()
        }
    }

    private fun showSelectedStateAndStartAnimating() {
        textToDraw = progressText
        buttonState = ButtonState.Loading
    }

    private fun setProgressToZero() {
        progressRect.right = 0
        drawAngle = 0F
        textToDraw = text
        invalidate()
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            progressColor = getColor(R.styleable.LoadingButton_progressColor, Color.RED)
            completedColor = getColor(R.styleable.LoadingButton_defaultColor, Color.GREEN)
            ovalColor = getColor(R.styleable.LoadingButton_ovalProgressColor, Color.YELLOW)
            animationDuration =
                getInteger(R.styleable.LoadingButton_defaultAnimDuration, 500).toLong()
            text = getString(R.styleable.LoadingButton_android_text)
            progressText = getString(R.styleable.LoadingButton_progressText)
            textPaint.textSize =
                getDimensionPixelSize(R.styleable.LoadingButton_android_textSize, 16).toFloat()
        }
        textToDraw = text
        // add view model to save and restore  values?
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawColor(completedColor)
        progressPaint.color = progressColor
        canvas?.drawRect(progressRect, progressPaint)
        textToDraw?.let {
            textPaint.getTextBounds(it, 0, it.length, textBounds)
            canvas?.drawText(
                it,
                (widthSize / 2).toFloat(),
                heightSize / 2 - textBounds.exactCenterY(),
                textPaint
            )
        }
        progressPaint.color = ovalColor
        val start = (widthSize / 2 + textBounds.exactCenterX()) + textBounds.height() / 2
        canvas?.drawArc(
            start,
            (heightSize / 2 - textBounds.height() / 2).toFloat(),
            start + textBounds.height(),
            (heightSize / 2 + textBounds.height() / 2).toFloat(),
            0F,
            drawAngle,
            true,
            progressPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    override fun performClick(): Boolean {
        super.performClick()
        buttonState = ButtonState.Clicked
        return true
    }

    private fun showProgress() {
        valueAnimator.apply {
            removeAllUpdateListeners()
            removeAllListeners()
            cancel()
            interpolator = AccelerateDecelerateInterpolator()
            setIntValues(widthSize)
            addUpdateListener {
                progressRect.right = it.animatedValue as Int
                drawAngle = ((it.animatedValue as Int) * 360 / widthSize).toFloat()
                progressRect.bottom = heightSize
                if (it.animatedValue == widthSize && buttonState == ButtonState.Completed) {
                    valueAnimator.cancel()
                }
                invalidate()
            }
//            repeatCount = -1
            addListener(
                onEnd = { buttonState = ButtonState.Completed }
            )
            duration = animationDuration
            start()
        }
    }
}