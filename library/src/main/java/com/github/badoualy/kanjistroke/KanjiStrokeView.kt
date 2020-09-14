package com.github.badoualy.kanjistroke

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import java.io.File
import java.io.InputStream
import java.util.TreeSet

/**
 * View to draw an animated (or not) Kanji from the SVG stroke data.
 * Developed for KanjiVG data only which only have a 109x109 bounding box.
 */
class KanjiStrokeView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    private val rawPathList = ArrayList<Path>()
    private val rawPathLengthList = ArrayList<Float>()

    private val pathList = ArrayList<Path>()
    private val pathMeasureList = ArrayList<PathMeasure>()

    private val transformMatrix = Matrix()

    // Animation
    private var drawAnimation: ValueAnimator? = null
    private var dashPathEffect: DashPathEffect? = null
    private var strokeIndex = 0
    private val fingerPosition = PointF(0f, 0f)
    private val pos = FloatArray(2)

    val animRunning: Boolean
        get() = drawAnimation?.isRunning ?: false
    val animStarted: Boolean
        get() = drawAnimation?.isStarted ?: false

    val strokeCount: Int
        get() = pathList.size
    val isKanjiDrawn: Boolean
        get() = strokeIndex == strokeCount

    // Used for strokes
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    // Used for animated stroke
    private val animPaint = Paint(paint)
    // Used to draw a circle at current position on the animated stroke
    private val fingerPaint = Paint(paint).apply { style = Paint.Style.FILL }
    // Used to draw the kanji in background in a lighter way (to see what'll be drawn)
    private val lightPaint = Paint(paint)
    val highlightedStrokeList = TreeSet<Int>()

    var drawLight = true
        set(value) {
            field = value
            invalidate()
        }
    var autoRun = false
    var autoRunDelay = 500L
    var animate = true
    var strokeColor = Color.BLACK
    var highlightColor = Color.RED

    init {
        //setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        val primaryColor = TypedValue().let {
            context.theme.resolveAttribute(R.attr.colorPrimary, it, true)
            it.data
        }

        val accentColor = TypedValue().let {
            context.theme.resolveAttribute(R.attr.colorAccent, it, true)
            it.data
        }

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.KanjiStrokeView,
                                                   defStyleAttr, 0)

            autoRun = a.getBoolean(R.styleable.KanjiStrokeView_svAutoRun, false)
            animate = a.getBoolean(R.styleable.KanjiStrokeView_svAnimate, true)

            strokeColor = a.getColor(R.styleable.KanjiStrokeView_svStrokeColor, Color.BLACK)
            fingerPaint.color = a.getColor(R.styleable.KanjiStrokeView_svFingerColor, primaryColor)
            lightPaint.color = a.getColor(R.styleable.KanjiStrokeView_svStrokeLightColor,
                                          Color.argb(50, 0, 0, 0))
            highlightColor = a.getColor(R.styleable.KanjiStrokeView_svStrokeHighlightColor,
                                        accentColor)

            a.getDimension(R.styleable.KanjiStrokeView_svStrokeWidth,
                           resources.getDimension(R.dimen.sv_default_stroke_width)).let {
                paint.strokeWidth = it
                lightPaint.strokeWidth = it
                animPaint.strokeWidth = it
            }
            fingerPaint.strokeWidth = a.getDimension(R.styleable.KanjiStrokeView_svFingerRadius,
                                                     resources.getDimension(R.dimen.sv_default_finger_radius))
        }

        animPaint.strokeWidth = paint.strokeWidth

        if (isInEditMode) {
            // Pretty preview
            loadPathData(previewStrokePathData)
            strokeIndex = 1
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        transformMatrix.setRectToRect(inputRect, RectF(0f, 0f, w.toFloat(), h.toFloat()),
                                      Matrix.ScaleToFit.FILL)
        applyTransformMatrix()

        if (isInEditMode) {
            // Animate current stroke for preview
            val length = pathMeasureList[strokeIndex].length
            dashPathEffect = DashPathEffect(floatArrayOf(length, length),
                                            length * 0.6f)
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (strokeCount == 0) return

        if (!animate) {
            // Draw complete kanji without animation
            for (i in 0 until strokeCount) {
                paint.color = if (highlightedStrokeList.contains(i)) highlightColor else strokeColor
                canvas.drawPath(pathList[i], paint)
            }
            return
        }

        if (!isKanjiDrawn && !animStarted && autoRun) {
            // Nothing to draw, start drawAnimation
            startDrawAnimation(autoRunDelay)
        }

        // Draw behind the kanji with lighter paint
        if (drawLight) {
            for (i in strokeIndex until strokeCount) {
                canvas.drawPath(pathList[i], lightPaint)
            }
        }

        // Draw finished stroke
        for (i in 0 until strokeIndex) {
            paint.color = if (highlightedStrokeList.contains(i)) highlightColor else strokeColor
            canvas.drawPath(pathList[i], paint)
        }

        // Animate current stroke
        if (dashPathEffect != null && (animRunning || isInEditMode)) {
            animPaint.pathEffect = dashPathEffect!!
            animPaint.color = if (highlightedStrokeList.contains(strokeIndex + 1)) highlightColor else strokeColor
            canvas.drawPath(pathList[strokeIndex], animPaint)
            canvas.drawCircle(fingerPosition.x, fingerPosition.y,
                              fingerPaint.strokeWidth, fingerPaint)
        }
    }

    fun setPathData(strokeView: KanjiStrokeView) {
        clear()

        // Copy input
        // Harmless to keep same instance since we never modify it after creation
        rawPathList.addAll(strokeView.rawPathList)
        rawPathLengthList.addAll(strokeView.rawPathLengthList)

        // Apply transformation
        applyTransformMatrix()
        invalidate()
    }

    fun loadPathData(pathDataList: List<String>?) {
        buildPathList(pathDataList)
    }

    fun loadSvg(input: String) {
        buildPathList(SVGHelper.extractPathData(input))
    }

    // TODO: KanjiVG files are small, but probably not very efficient
    fun loadSvg(stream: InputStream) = loadSvg(stream.bufferedReader().use { it.readText() })

    // TODO: KanjiVG files are small, but probably not very efficient
    fun loadSvg(file: File) = loadSvg(file.readText())

    @JvmOverloads
    fun startDrawAnimation(delay: Long = 0) {
        if (animStarted) {
            drawAnimation?.cancel()
            drawAnimation = null
        }
        strokeIndex = 0
        if (strokeCount > 0)
            startStrokeAnimation(delay)
    }

    fun highlightStroke(stroke: Int) {
        if (stroke in 0..(strokeCount - 1) && highlightedStrokeList.add(stroke))
            invalidate()
    }

    fun highlightStrokes(strokeList: List<Int>) {
        strokeList.filterTo(highlightedStrokeList) { it in 0..(strokeCount - 1) }
        invalidate()
    }

    fun clearHighlightStrokList() {
        highlightedStrokeList.clear()
        invalidate()
    }

    private fun startStrokeAnimation(delay: Long) {
        val length = pathMeasureList[strokeIndex].length
        drawAnimation = ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener { animation ->
                if (drawAnimation != this || strokeIndex >= strokeCount) {
                    cancel()
                    return@addUpdateListener
                }

                dashPathEffect = DashPathEffect(floatArrayOf(length, length),
                                                length * (1f - animation.animatedFraction))

                pathMeasureList[strokeIndex].getPosTan(length * animation.animatedFraction,
                                                       pos, null)
                fingerPosition.set(pos[0], pos[1])
                invalidate()
            }

            addListener(object : AnimatorListenerAdapter() {

                private var cancelled = false

                override fun onAnimationCancel(animation: Animator?) {
                    cancelled = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    if (cancelled) return

                    if (++strokeIndex < strokeCount) {
                        startStrokeAnimation(0)
                    } else {
                        this@KanjiStrokeView.drawAnimation = null
                        dashPathEffect = null
                    }
                }
            })

            // Panoramix' formula
            duration = (rawPathLengthList[strokeIndex] * 10).toLong()
        }
        drawAnimation!!.startDelay = delay
        drawAnimation!!.start()
    }

    private fun applyTransformMatrix() {
        if (pathList.isEmpty())
            pathList.addAll(List(rawPathList.size, { Path() }))
        rawPathList.forEachIndexed { i, path ->
            path.transform(transformMatrix, pathList[i])
        }
        pathMeasureList.clear()
        pathList.mapTo(pathMeasureList) { PathMeasure(it, false) }
    }

    private fun buildPathList(strokePathData: List<String>?) {
        clear()
        if (strokePathData.orEmpty().isNotEmpty()) {
            strokePathData!!.mapTo(rawPathList) { SVGHelper.buildPath(it) }
            rawPathList.mapTo(rawPathLengthList) { PathMeasure(it, false).length }
            applyTransformMatrix()
        }
        invalidate()
    }

    private fun clear() {
        rawPathList.clear()
        rawPathLengthList.clear()
        highlightedStrokeList.clear()
        pathList.clear()
        pathMeasureList.clear()
        strokeIndex = 0
        drawAnimation?.cancel()
    }

    companion object {
        private const val TAG = "KanjiStrokeView"

        // Constant from KanjiVG, used to compute the transform matrix
        private val inputRect = RectF(0f, 0f, 109f, 109f)

        // Kanji 月 for preview
        private val previewStrokePathData: List<String> by lazy {
            listOf("M34.25,16.25c1,1,1.48,2.38,1.5,4c0.38,33.62,2.38,59.38-11,73.25",
                   "M36.25,19c4.12-0.62,31.49-4.78,33.25-5c4-0.5,5.5,1.12,5.5,4.75c0,2.76-0.5,49.25-0.5,69.5c0,13-6.25,4-8.75,1.75",
                   "M37.25,38c10.25-1.5,27.25-3.75,36.25-4.5",
                   "M37,58.25c8.75-1.12,27-3.5,36.25-4")
        }
    }
}
