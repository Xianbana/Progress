package com.xian.progress

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import com.xian.progress.model.ProgressConfig
import com.xian.progress.model.ProgressData

/**
 * 圆环进度条
 * 支持与 HorizontalProgressBar 一致的配置和 XML 属性
 */
class CircularProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentProgress = 0f
    private var targetProgress = 0f
    private var progressData: ProgressData? = null
    private var progressConfig = ProgressConfig()

    private val backgroundPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val progressPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val handler = Handler(Looper.getMainLooper())
    private var animationRunnable: Runnable? = null
    private var isAnimating = false

    var onProgressComplete: ((Boolean) -> Unit)? = null
    var onProgressChanged: ((Float) -> Unit)? = null

    init {
        initAttributes(attrs)
        updatePaints()
    }

    private fun initAttributes(attrs: AttributeSet?) {
        attrs?.let { attributeSet ->
            val typedArray: TypedArray = context.obtainStyledAttributes(
                attributeSet,
                R.styleable.HorizontalProgressBar,
                0,
                0
            )

            try {
                val backgroundColor = typedArray.getColor(
                    R.styleable.HorizontalProgressBar_progressBackgroundColor,
                    progressConfig.backgroundColor
                )

                val progressColor = typedArray.getColor(
                    R.styleable.HorizontalProgressBar_progressColor,
                    progressConfig.progressColor
                )

                val progress = typedArray.getFloat(R.styleable.HorizontalProgressBar_progress, 0f)

                val enableAnimation = typedArray.getBoolean(
                    R.styleable.HorizontalProgressBar_enableAnimation,
                    progressConfig.enableAnimation
                )

                val animateFromZero = typedArray.getBoolean(
                    R.styleable.HorizontalProgressBar_animateFromZero,
                    progressConfig.animateFromZero
                )

                val animationDuration = typedArray.getInteger(
                    R.styleable.HorizontalProgressBar_animationDuration,
                    progressConfig.animationDuration.toInt()
                ).toLong()

                val strokeWidth = typedArray.getDimension(
                    R.styleable.HorizontalProgressBar_progressHeight,
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        12f,
                        resources.displayMetrics
                    )
                )

                progressConfig = ProgressConfig(
                    backgroundColor = backgroundColor,
                    progressColor = progressColor,
                    enableAnimation = enableAnimation,
                    animateFromZero = animateFromZero,
                    animationDuration = animationDuration,
                    cornerRadius = 360f
                )

                // 将高度用作圆环的画笔宽度
                backgroundPaint.strokeWidth = strokeWidth
                progressPaint.strokeWidth = strokeWidth

                if (progress > 0f) {
                    setProgress(progress)
                }

            } finally {
                typedArray.recycle()
            }
        }
    }

    fun setProgressConfig(config: ProgressConfig) {
        this.progressConfig = config
        updatePaints()
        invalidate()
    }

    /**
     * 设置背景颜色
     */
    override fun setBackgroundColor(@ColorInt backgroundColor: Int) {
        this.progressConfig = this.progressConfig.copy(backgroundColor = backgroundColor)
        updatePaints()
        // 只有在没有动画时才立即重绘，否则让动画自然重绘
        if (!isAnimating) {
            invalidate()
        }
    }

    /**
     * 获取背景颜色
     */
    @ColorInt
    fun getBackgroundColor(): Int = this.progressConfig.backgroundColor

    /**
     * 设置进度颜色
     */
    fun setProgressColor(@ColorInt progressColor: Int) {
        this.progressConfig = this.progressConfig.copy(progressColor = progressColor)
        updatePaints()
        // 只有在没有动画时才立即重绘，否则让动画自然重绘
        if (!isAnimating) {
            invalidate()
        }
    }

    /**
     * 获取进度颜色
     */
    @ColorInt
    fun getProgressColor(): Int = this.progressConfig.progressColor

    /**
     * 设置是否启用动画
     */
    fun setEnableAnimation(enableAnimation: Boolean) {
        this.progressConfig = this.progressConfig.copy(enableAnimation = enableAnimation)
    }

    /**
     * 获取是否启用动画
     */
    fun isAnimationEnabled(): Boolean = this.progressConfig.enableAnimation

    /**
     * 设置是否每次动画从0开始
     */
    fun setAnimateFromZero(animateFromZero: Boolean) {
        this.progressConfig = this.progressConfig.copy(animateFromZero = animateFromZero)
    }

    /**
     * 获取是否每次动画从0开始
     */
    fun isAnimateFromZero(): Boolean = this.progressConfig.animateFromZero

    /**
     * 设置动画时长
     */
    fun setAnimationDuration(animationDuration: Long) {
        this.progressConfig = this.progressConfig.copy(animationDuration = animationDuration)
        
        // 如果当前正在动画中，重新启动动画以使用新的时长
        if (isAnimating) {
            // 停止当前动画但不设置isAnimating为false
            handler.removeCallbacksAndMessages(null)
            animationRunnable?.let { handler.removeCallbacks(it) }
            // 重新启动动画
            startProgressAnimation()
        }
    }

    /**
     * 获取动画时长
     */
    fun getAnimationDuration(): Long = this.progressConfig.animationDuration

    /**
     * 设置圆角半径
     */
    fun setCornerRadius(cornerRadius: Float) {
        this.progressConfig = this.progressConfig.copy(cornerRadius = cornerRadius)
        invalidate()
    }

    /**
     * 获取圆角半径
     */
    fun getCornerRadius(): Float = this.progressConfig.cornerRadius

    fun setProgress(progress: ProgressData) {
        this.progressData = progress
        this.targetProgress = progress.progressValue

        if (isInEditMode) {
            currentProgress = targetProgress
            invalidate()
            return
        }

        if (progressConfig.enableAnimation) {
            startProgressAnimation()
        } else {
            currentProgress = targetProgress
            invalidate()
            onProgressChanged?.invoke(currentProgress)
            onProgressComplete?.invoke(true)
        }
    }

    fun setProgress(progress: Number) {
        setProgress(ProgressData.Companion.create(progress))
    }

    fun setProgress(progress: Int) {
        setProgress(ProgressData.Companion.createInt(progress))
    }

    fun setProgress(progress: Float) {
        setProgress(ProgressData.Companion.createFloat(progress))
    }

    fun getCurrentProgress(): Float = currentProgress
    fun getTargetProgress(): Float = targetProgress
    fun isAnimating(): Boolean = isAnimating

    fun reset() {
        currentProgress = 0f
        targetProgress = 0f
        progressData = null
        isAnimating = false
        handler.removeCallbacksAndMessages(null)
        animationRunnable = null
        invalidate()
    }

    fun pauseAnimation() {
        if (isAnimating) {
            handler.removeCallbacksAndMessages(null)
            animationRunnable?.let { handler.removeCallbacks(it) }
            isAnimating = false
        }
    }

    fun resumeAnimation() {
        if (!isAnimating && progressConfig.enableAnimation && currentProgress < targetProgress) {
            startProgressAnimation()
        }
    }

    private fun updatePaints() {
        backgroundPaint.color = progressConfig.backgroundColor
        progressPaint.color = progressConfig.progressColor
    }

    private fun startProgressAnimation() {
        if (isInEditMode) {
            currentProgress = targetProgress
            isAnimating = false
            invalidate()
            return
        }
        
        // 停止之前的动画
        handler.removeCallbacksAndMessages(null)
        animationRunnable?.let { handler.removeCallbacks(it) }
        
        isAnimating = true
        val totalDuration = progressConfig.animationDuration
        val interval = 16L
        if (progressConfig.animateFromZero) {
            currentProgress = 0f
        }
        val distance = (targetProgress - currentProgress).coerceAtLeast(0f)
        val steps = ((totalDuration / interval).toInt()).coerceAtLeast(1)
        val increment = if (distance == 0f) 0f else distance / steps

        animationRunnable = object : Runnable {
            override fun run() {
                if (currentProgress < targetProgress && isAnimating) {
                    currentProgress = (currentProgress + increment).coerceAtMost(targetProgress)
                    invalidate()
                    onProgressChanged?.invoke(currentProgress)
                    handler.postDelayed(this, interval)
                } else {
                    currentProgress = targetProgress
                    isAnimating = false
                    invalidate()
                    onProgressChanged?.invoke(currentProgress)
                    onProgressComplete?.invoke(true)
                }
            }
        }

        animationRunnable?.let { handler.post(it) }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = minOf(width, height).toFloat()
        val stroke = backgroundPaint.strokeWidth
        val radius = (size - stroke) / 2f
        val cx = width / 2f
        val cy = height / 2f

        val oval = RectF(
            cx - radius,
            cy - radius,
            cx + radius,
            cy + radius
        )

        // 背景圆环
        canvas.drawArc(oval, 0f, 360f, false, backgroundPaint)

        // 进度圆弧（从 -90 度顶端开始）
        if (currentProgress > 0f) {
            val sweepAngle = 360f * (currentProgress / 100f)
            val finalColor = progressData?.color ?: progressConfig.progressColor
            progressPaint.color = finalColor
            canvas.drawArc(oval, -90f, sweepAngle, false, progressPaint)
        }
    }
}