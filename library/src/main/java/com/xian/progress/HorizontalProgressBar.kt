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
 * 水平进度条
 * 支持自定义背景色、进度色、动画开关、动画时长等配置
 * 支持XML属性配置
 */
class HorizontalProgressBar @JvmOverloads constructor(
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
        style = Paint.Style.FILL
    }

    private val progressPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val handler = Handler(Looper.getMainLooper())
    private var animationRunnable: Runnable? = null
    private var isAnimating = false

    /**
     * 进度完成回调
     */
    var onProgressComplete: ((Boolean) -> Unit)? = null

    /**
     * 进度变化回调
     */
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
                // 读取背景颜色
                val backgroundColor = typedArray.getColor(
                    R.styleable.HorizontalProgressBar_progressBackgroundColor,
                    progressConfig.backgroundColor
                )

                // 读取进度颜色
                val progressColor = typedArray.getColor(
                    R.styleable.HorizontalProgressBar_progressColor,
                    progressConfig.progressColor
                )

                // 读取进度值
                val progress = typedArray.getFloat(R.styleable.HorizontalProgressBar_progress, 0f)

                // 读取动画开关
                val enableAnimation = typedArray.getBoolean(
                    R.styleable.HorizontalProgressBar_enableAnimation,
                    progressConfig.enableAnimation
                )

                // 读取动画是否从0开始
                val animateFromZero = typedArray.getBoolean(
                    R.styleable.HorizontalProgressBar_animateFromZero,
                    progressConfig.animateFromZero
                )

                // 读取动画时长
                val animationDuration = typedArray.getInteger(
                    R.styleable.HorizontalProgressBar_animationDuration,
                    progressConfig.animationDuration.toInt()
                ).toLong()

                // 读取圆角半径
                val cornerRadius = typedArray.getDimension(
                    R.styleable.HorizontalProgressBar_cornerRadius,
                    progressConfig.cornerRadius
                )

                // 读取进度条高度
                val progressHeight = typedArray.getDimension(
                    R.styleable.HorizontalProgressBar_progressHeight,
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        20f,
                        resources.displayMetrics
                    )
                )

                // 创建配置
                progressConfig = ProgressConfig(
                    backgroundColor = backgroundColor,
                    progressColor = progressColor,
                    enableAnimation = enableAnimation,
                    animateFromZero = animateFromZero,
                    animationDuration = animationDuration,
                    cornerRadius = cornerRadius
                )

                // 设置进度条高度
                if (progressHeight > 0) {
                    val layoutParams = layoutParams
                    if (layoutParams != null) {
                        layoutParams.height = progressHeight.toInt()
                        this.layoutParams = layoutParams
                    }
                }

                // 如果有初始进度值，设置进度
                if (progress > 0f) {
                    setProgress(progress)
                }

            } finally {
                typedArray.recycle()
            }
        }
    }

    /**
     * 设置进度条配置
     */
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
        invalidate()
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

    /**
     * 设置进度
     */
    fun setProgress(progress: ProgressData) {
        this.progressData = progress
        this.targetProgress = progress.progressValue

        // 在布局编辑器中（预览模式）跳过动画，直接显示目标进度
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

    /**
     * 设置进度（支持Number类型）
     */
    fun setProgress(progress: Number) {
        setProgress(ProgressData.Companion.create(progress))
    }

    /**
     * 设置进度（int类型）
     */
    fun setProgress(progress: Int) {
        setProgress(ProgressData.Companion.createInt(progress))
    }

    /**
     * 设置进度（float类型）
     */
    fun setProgress(progress: Float) {
        setProgress(ProgressData.Companion.createFloat(progress))
    }

    /**
     * 获取当前进度
     */
    fun getCurrentProgress(): Float = currentProgress

    /**
     * 获取目标进度
     */
    fun getTargetProgress(): Float = targetProgress

    /**
     * 是否正在动画中
     */
    fun isAnimating(): Boolean = isAnimating

    /**
     * 重置进度条
     */
    fun reset() {
        currentProgress = 0f
        targetProgress = 0f
        progressData = null
        isAnimating = false
        handler.removeCallbacksAndMessages(null)
        animationRunnable = null
        invalidate()
    }

    /**
     * 暂停动画
     */
    fun pauseAnimation() {
        if (isAnimating) {
            handler.removeCallbacksAndMessages(null)
            animationRunnable?.let { handler.removeCallbacks(it) }
            isAnimating = false
        }
    }

    /**
     * 恢复动画
     */
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
            // 预览模式下不执行动画，直接到目标进度
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
        val interval = 16L // 60fps
        // 起始进度：根据配置决定是否从0开始
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
                    // 动画完成
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

        val width = width.toFloat()
        val height = height.toFloat()

        // 绘制背景
        canvas.drawRoundRect(
            RectF(0f, 0f, width, height),
            progressConfig.cornerRadius,
            progressConfig.cornerRadius,
            backgroundPaint
        )

        // 绘制进度
        if (currentProgress > 0f) {
            val progressWidth = width * (currentProgress / 100f)
            if (progressWidth > 0) {
                // 使用数据中的颜色或配置中的默认颜色
                val finalColor = progressData?.color ?: progressConfig.progressColor
                progressPaint.color = finalColor

                canvas.drawRoundRect(
                    RectF(0f, 0f, progressWidth, height),
                    progressConfig.cornerRadius,
                    progressConfig.cornerRadius,
                    progressPaint
                )
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacksAndMessages(null)
        animationRunnable = null
        isAnimating = false
    }
}