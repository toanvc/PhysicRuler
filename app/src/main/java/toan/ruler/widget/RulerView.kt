package toan.ruler.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

import toan.ruler.R
import toan.ruler.utils.RulerUtils


/**
 * Created by Toan Vu on 4/1/16.
 */
class RulerView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        const val BORDER_LIMIT = 20f
        const val TEXT_MARGIN = 20f
    }

    //2 objects
    private var touch1 = Coordinate()
    private var touch2 = Coordinate()

    private var rulerType = RulerType.INCH

    //this value controls touchable size for 2 lines
    private var delta = resources.getDimension(R.dimen.touch_size).toInt()

    private var result: Float = 0f
    private var pixelPerMillimeter: Float = 0f
    private var pixelPerInch: Float = 0f

    //ruler will use it for start pixel value on screen
    private var startPoint = 20f

    //paint
    private val rulerInchPaint = generatePaint(2f)
    private val textSmallPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        //        typeface = Typeface.createFromAsset(context.assets, "fonts/Roboto-Regular.ttf")
        style = Paint.Style.STROKE
        textSize = resources.getDimension(R.dimen.txt_size)
        color = Color.WHITE
    }

    private val textBigPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        textSize = resources.getDimension(R.dimen.txt_big_size)
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val rulerPaint = generatePaint(0f)


    private val scaleLineLevel1 = resources.getDimension(R.dimen.scale_line_1).toInt()
    private val scaleLineLevel2 = resources.getDimension(R.dimen.scale_line_2).toInt()
    private val scaleLineLevel3 = resources.getDimension(R.dimen.scale_line_3).toInt()
    private val scaleLineLevel4 = resources.getDimension(R.dimen.scale_line_4).toInt()
    private val scaleLineLevel5 = resources.getDimension(R.dimen.scale_line_5).toInt()
    private val textStartPoint = resources.getDimension(R.dimen.text_start_point).toInt()

    init {
        if (!isInEditMode) {
            val dm = resources.displayMetrics
            //divide inch by 16 because we use each unit = 1/16 inch
            pixelPerInch = dm.ydpi / 16
            //use unit by mm
            pixelPerMillimeter = dm.ydpi / 25.4f
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        //initial value for 2 lines
        touch1.y_axis = height / 4f
        touch2.y_axis = height * 3f / 4
    }

    public override fun onDraw(canvas: Canvas) {
        if (rulerType == RulerType.INCH) {
            drawInch(canvas)
        } else {
            drawCm(canvas)
        }
        canvas.drawText("Measure: " + RulerUtils.formatNumber(result) + getUnit(rulerType), 30f, height - 12f, textSmallPaint)

        canvas.drawLine(0f, touch1.y_axis, width.toFloat(), touch1.y_axis, touch1.getPaint())
        canvas.drawLine(0f, touch2.y_axis, width.toFloat(), touch2.y_axis, touch2.getPaint())
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // get pointer index from the event object
        val pointerIndex = event.actionIndex

        // get pointer ID
        val pointerId = event.getPointerId(pointerIndex)

        // get masked (not specific to a pointer) action
        val maskedAction = event.actionMasked
        when (maskedAction) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_POINTER_DOWN -> {
                // We have a new pointer
                val y = event.getY(pointerIndex)
                //check lines touch or not
                if (Math.abs(y - touch1.y_axis) < delta) {
                    touch1.active = true
                    touch1.eventId = pointerId
                } else if (Math.abs(y - touch2.y_axis) < delta) {
                    touch2.active = true
                    touch2.eventId = pointerId
                }
            }
            MotionEvent.ACTION_MOVE -> { // a pointer was moved
                val size = event.pointerCount
                var i = 0
                while (i < size) {
                    //moving line
                    if (touch1.active && event.getPointerId(i) == touch1.eventId) {
                        touch1.y_axis = event.getY(i)

                    }
                    if (touch2.active && event.getPointerId(i) == touch2.eventId) {
                        touch2.y_axis = event.getY(i)
                    }
                    i++
                }

                result = calculateMeasure()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                //update touches
                if (touch1.active && pointerId == touch1.eventId) {
                    touch1.active = false
                }
                if (touch2.active && pointerId == touch2.eventId) {
                    touch2.active = false
                }
            }
        }
        this.postInvalidate()
        return true
    }

    fun setRulerType(type: RulerType) {
        this.rulerType = type
        //recalculate the result
        result = calculateMeasure()
    }


    /**
     * Draw Cm type
     */
    private fun drawCm(canvas: Canvas) {
        startPoint = BORDER_LIMIT
        var i = 0
        while (true) {
            if (startPoint > height - BORDER_LIMIT) {
                break
            }

            val size = if (i % 10 == 0) scaleLineLevel3 else if (i % 5 == 0) scaleLineLevel2 else scaleLineLevel1
            canvas.drawLine((width - size).toFloat(), startPoint, width.toFloat(), startPoint, rulerPaint)
            if (i % 10 == 0) {
                val textX = width - scaleLineLevel3 - TEXT_MARGIN
                val textY = startPoint
                drawRotateText(canvas, (i / 10).toString(), textX, textY)
            }
            startPoint += pixelPerMillimeter
            i++
        }
    }

    /**
     * Draw Inch type
     */
    private fun drawInch(canvas: Canvas) {
        startPoint = BORDER_LIMIT
        var i = 0
        while (true) {
            if (startPoint > height - BORDER_LIMIT) {
                break
            }
            val size: Int
            var paint: Paint = rulerPaint
            var textX = width - TEXT_MARGIN
            val textY = startPoint
            when {
                i % 16 == 0 -> {
                    size = scaleLineLevel5
                    paint = rulerInchPaint
                    textX -= size
                    drawRotateText(canvas, (i / 16).toString(), textX, textY, textBigPaint)
                }
                i % 8 == 0 -> {
                    size = scaleLineLevel4
                    textX -= size
                    drawRotateText(canvas, "${i / 8}/2", textX, textY)
                }
                i % 4 == 0 -> size = scaleLineLevel3
                else -> size = if (i % 2 == 0) scaleLineLevel2 else scaleLineLevel1
            }
            canvas.drawLine((width - size).toFloat(), startPoint, width.toFloat(), startPoint, paint)

            startPoint += pixelPerInch
            i++
        }
    }

    private fun drawRotateText(canvas: Canvas, text: String, x: Float, y: Float, paint: TextPaint = textSmallPaint) {
        //need to recalculate text size, and draw on accurate position
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val textHeight = bounds.height()
        val textWidth = bounds.left + bounds.width()
        //because we need to draw it in rotate 90 degree,
        //then textWidth and textHeight from bounds are swapped
        val accurateY = y - textWidth / 2
        val accurateX = x - textHeight
        canvas.save()
        canvas.rotate(90f, accurateX, accurateY)
        canvas.drawText(text, accurateX, accurateY, paint)
        canvas.restore()
    }

    /**
     * Calculate measure
     *
     * @return result in inch or cm
     */
    private fun calculateMeasure(): Float {
        val distance = Math.abs(touch1.y_axis - touch2.y_axis)
        return if (rulerType == RulerType.CM) {
            distance / pixelPerMillimeter / 10
        } else {
            distance / pixelPerInch / 16
        }
    }

    /**
     * Return cm or inch
     */
    private fun getUnit(type: RulerType): String {
        return if (type == RulerType.CM) context.getString(R.string.cm) else context.getString(R.string.inch)
    }

    private fun generatePaint(stroke: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        color = Color.WHITE
    }

    //initial line with normal state and touched state
    private fun generateBorderPaint(color: Int) = Paint().apply {
        val strokeWidth = resources.getDimension(R.dimen.line_size)
        style = Paint.Style.FILL_AND_STROKE
        setColor(ContextCompat.getColor(context, color))
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        this.strokeWidth = strokeWidth
        pathEffect = CornerPathEffect(10f)
    }

    enum class RulerType {
        INCH, CM
    }

    //Object contains properties of 2 lines on ruler
    private inner class Coordinate(var active: Boolean = false,
                                   var eventId: Int = 0) {
        var y_axis: Float = 0f
            set(value) {
                //limited touch area, in screen only
                field = when {
                    value < BORDER_LIMIT -> BORDER_LIMIT
                    value > height - BORDER_LIMIT -> height - BORDER_LIMIT
                    else -> value
                }

            }
        private val linePaint = generateBorderPaint(R.color.yellow)
        private val highLinePaint = generateBorderPaint(R.color.cyan)
        fun getPaint() = if (active) highLinePaint else linePaint
    }
}