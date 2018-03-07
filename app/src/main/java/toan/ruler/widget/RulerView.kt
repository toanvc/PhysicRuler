package toan.ruler.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View

import toan.ruler.R
import toan.ruler.utils.RulerUtils
import toan.ruler.widget.RulerView.RulerType


/**
 * Created by Toan Vu on 4/1/16.
 */
class RulerView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    //2 objects
    private var touch1: Coordinate? = null
    private var touch2: Coordinate? = null

    private var rulerType = RulerType.INCH
    private var mActivePointers: SparseArray<PointF>? = null

    //this value controls touchable size for 2 lines
    private var delta: Int = 0

    private var result: Double = 0.toDouble()
    private var pixelPerMillimeter: Float = 0.toFloat()
    private var pixelPerInch: Float = 0.toFloat()

    //ruler will use it for start pixel value on screen
    private var startPoint = 20f

    //paint
    private var rulerInchPaint: Paint? = null
    private var textPaint: Paint? = null
    private var rulerPaint: Paint? = null
    private var linePaint: Paint? = null
    private var highLinePaint: Paint? = null


    private var scaleLineLevel1: Int = 0
    private var scaleLineLevel2: Int = 0
    private var scaleLineLevel3: Int = 0
    private var scaleLineLevel4: Int = 0
    private var scaleLineLevel5: Int = 0
    private var textStartPoint: Int = 0

    enum class RulerType {
        INCH, CM
    }

    //Object contains properties of 2 lines on ruler
    private inner class Coordinate(internal var y_axis: Int) {
        internal var movable: Boolean = false
        internal var eventId: Int = 0

    }

    init {
        if (!isInEditMode) {
            init(context)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (touch1 == null && touch2 == null) {
            //initial value for 2 lines
            touch1 = Coordinate(height / 4)
            touch2 = Coordinate(height * 3 / 4)
        }
    }

    public override fun onDraw(canvas: Canvas) {
        if (rulerType == RulerType.INCH) {
            drawInch(canvas)
        } else {
            drawCm(canvas)
        }

        canvas.drawText("Measure: " + RulerUtils.formatNumber(result) + getUnit(rulerType), 30f, (height - 20 + 8).toFloat(), textPaint!!)

        if (touch1!!.movable) {
            canvas.drawLine(0f, touch1!!.y_axis.toFloat(), width.toFloat(), touch1!!.y_axis.toFloat(), highLinePaint!!)
        } else {
            canvas.drawLine(0f, touch1!!.y_axis.toFloat(), width.toFloat(), touch1!!.y_axis.toFloat(), linePaint!!)
        }
        if (touch2!!.movable) {
            canvas.drawLine(0f, touch2!!.y_axis.toFloat(), width.toFloat(), touch2!!.y_axis.toFloat(), highLinePaint!!)
        } else {
            canvas.drawLine(0f, touch2!!.y_axis.toFloat(), width.toFloat(), touch2!!.y_axis.toFloat(), linePaint!!)
        }
    }

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
                // We have a new pointer. Lets add it to the list of pointers
                val f = PointF()
                f.x = event.getX(pointerIndex)
                f.y = event.getY(pointerIndex)
                mActivePointers!!.put(pointerId, f)
                //check lines touch or not
                if (Math.abs(f.y - touch1!!.y_axis) < delta) {
                    touch1!!.movable = true
                    touch1!!.eventId = pointerId
                } else if (Math.abs(f.y - touch2!!.y_axis) < delta) {
                    touch2!!.movable = true
                    touch2!!.eventId = pointerId
                }
            }
            MotionEvent.ACTION_MOVE -> { // a pointer was moved
                val size = event.pointerCount
                var i = 0
                while (i < size) {
                    val point = mActivePointers!!.get(event.getPointerId(i))
                    if (point != null) {
                        point.x = event.getX(i)
                        point.y = event.getY(i)

                        //moving line
                        if (touch1!!.movable && event.getPointerId(i) == touch1!!.eventId) {
                            touch1!!.y_axis = event.getY(i).toInt()

                        }
                        if (touch2!!.movable && event.getPointerId(i) == touch2!!.eventId) {
                            touch2!!.y_axis = event.getY(i).toInt()
                        }
                    }

                    result = calculateMeasure()
                    i++

                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                //release touched lines
                mActivePointers!!.remove(pointerId)
                if (touch1!!.movable && pointerId == touch1!!.eventId) {
                    touch1!!.movable = false
                }
                if (touch2!!.movable && pointerId == touch2!!.eventId) {
                    touch2!!.movable = false
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

    private fun init(context: Context) {
        mActivePointers = SparseArray()
        val dm = resources.displayMetrics
        //divide inch by 16 because we use each unit = 1/16 inch
        pixelPerInch = dm.ydpi / 16
        //use unit by mm
        pixelPerMillimeter = dm.ydpi / 25.4f

        rulerPaint = Paint()
        rulerPaint!!.style = Paint.Style.STROKE
        rulerPaint!!.strokeWidth = 0f
        rulerPaint!!.isAntiAlias = false
        rulerPaint!!.color = Color.WHITE

        rulerInchPaint = Paint()
        rulerInchPaint!!.style = Paint.Style.STROKE
        rulerInchPaint!!.strokeWidth = 2f
        rulerInchPaint!!.isAntiAlias = false
        rulerInchPaint!!.color = Color.WHITE

        textPaint = TextPaint()
        val typeface = Typeface.createFromAsset(context.assets, "fonts/Roboto-Regular.ttf")
        textPaint!!.typeface = typeface
        textPaint!!.style = Paint.Style.STROKE
        textPaint!!.strokeWidth = 0f
        textPaint!!.isAntiAlias = true
        textPaint!!.textSize = resources.getDimension(R.dimen.txt_size)
        textPaint!!.color = Color.WHITE

        linePaint = initPaintLine(context, R.color.yellow)
        highLinePaint = initPaintLine(context, R.color.cyan)

        delta = resources.getDimension(R.dimen.touch_size).toInt()

        scaleLineLevel1 = resources.getDimension(R.dimen.scale_line_1).toInt()
        scaleLineLevel2 = resources.getDimension(R.dimen.scale_line_2).toInt()
        scaleLineLevel3 = resources.getDimension(R.dimen.scale_line_3).toInt()
        scaleLineLevel4 = resources.getDimension(R.dimen.scale_line_4).toInt()
        scaleLineLevel5 = resources.getDimension(R.dimen.scale_line_5).toInt()
        textStartPoint = resources.getDimension(R.dimen.text_start_point).toInt()


    }

    private fun drawCm(canvas: Canvas) {
        startPoint = 20f
        var i = 0
        while (true) {
            if (startPoint > height - 20) {
                break
            }

            val size = if (i % 10 == 0) scaleLineLevel3 else if (i % 5 == 0) scaleLineLevel2 else scaleLineLevel1
            canvas.drawLine((width - size).toFloat(), startPoint, width.toFloat(), startPoint, rulerPaint!!)
            if (i % 10 == 0) {

                canvas.drawText((i / 10).toString() + getUnit(rulerType), (width - textStartPoint).toFloat(), startPoint + 8, textPaint!!)
            }
            startPoint = startPoint + pixelPerMillimeter
            i++
        }
    }

    //initial line with normal state and touched state
    private fun initPaintLine(context: Context, color: Int): Paint {
        val strokeWidth = resources.getDimension(R.dimen.line_size)

        val linePaint = Paint()
        linePaint.style = Paint.Style.FILL_AND_STROKE
        linePaint.color = ContextCompat.getColor(context, color)
        linePaint.strokeJoin = Paint.Join.ROUND
        linePaint.strokeCap = Paint.Cap.ROUND
        linePaint.strokeWidth = strokeWidth
        linePaint.pathEffect = CornerPathEffect(10f)
        linePaint.isAntiAlias = true
        return linePaint
    }

    private fun drawInch(canvas: Canvas) {
        startPoint = 20f
        var i = 0
        while (true) {
            if (startPoint > height - 20) {
                break
            }
            val size: Int
            var paint: Paint = rulerPaint!!

            if (i % 16 == 0) {
                size = scaleLineLevel5
                paint = rulerInchPaint!!
                canvas.drawText((i / 16).toString() + getUnit(rulerType), (width - textStartPoint).toFloat(), startPoint + 8, textPaint!!)
            } else if (i % 8 == 0) {
                size = scaleLineLevel4
                canvas.drawText((i / 8).toString() + "/2", (width - textStartPoint).toFloat(), startPoint + 8, textPaint!!)
            } else if (i % 4 == 0) {
                size = scaleLineLevel3
            } else {
                size = if (i % 2 == 0) scaleLineLevel2 else scaleLineLevel1

            }
            canvas.drawLine((width - size).toFloat(), startPoint, width.toFloat(), startPoint, paint)

            startPoint = startPoint + pixelPerInch
            i++
        }
    }


    /**
     * Calculate measure
     *
     * @return result in inch or cm
     */
    private fun calculateMeasure(): Double {

        val distance = Math.abs(touch1!!.y_axis - touch2!!.y_axis).toDouble()
        return if (rulerType == RulerType.CM) {
            distance / pixelPerMillimeter.toDouble() / 10.0
        } else {
            distance / pixelPerInch.toDouble() / 16.0
        }
    }

    private fun getUnit(type: RulerType): String {
        return if (type == RulerType.CM) context.getString(R.string.cm) else context.getString(R.string.inch)
    }


}