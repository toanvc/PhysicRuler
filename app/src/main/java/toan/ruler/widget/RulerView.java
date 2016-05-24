package toan.ruler.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import toan.ruler.R;
import toan.ruler.utils.RulerUtils;


/**
 * Created by Toan Vu on 4/1/16.
 */
public class RulerView extends View {

    public enum RulerType {
        INCH, CM
    }

    //Object contains properties of 2 lines on ruler
    private class Coordinate {
        int y_axis;
        boolean movable;
        int eventId;

        public Coordinate(int y) {
            this.y_axis = y;
        }

    }

    //2 objects 
    Coordinate touch1;
    Coordinate touch2;

    private RulerType rulerType = RulerType.INCH;
    private SparseArray<PointF> mActivePointers;

    //this value controls touchable size for 2 lines
    private int delta;

    private int screenSize;
    private double result;
    private float pixelPerMillimeter;
    private float pixelPerInch;
    private int width;
    private int height;

    //ruler will use it for start pixel value on screen
    private float startPoint = 20;

    //paint
    private Paint rulerInchPaint;
    private Paint textPaint;
    private Paint rulerPaint;
    private Paint linePaint;
    private Paint highLinePaint;


    private int scaleLineLevel1;
    private int scaleLineLevel2;
    private int scaleLineLevel3;
    private int scaleLineLevel4;
    private int scaleLineLevel5;
    private int textStartPoint;

    public RulerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context);
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = w;
        height = h;
        screenSize = h;

        if (touch1 == null && touch2 == null) {
            //initial value for 2 lines
            touch1 = new Coordinate(height / 4);
            touch2 = new Coordinate(height * 3 / 4);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (rulerType == RulerType.INCH) {
            drawInch(canvas);
        } else {
            drawCm(canvas);
        }

        canvas.drawText("Measure: " + RulerUtils.formatNumber(result) + getUnit(rulerType), 30, height - 20 + 8, textPaint);

        if (touch1.movable) {
            canvas.drawLine(0f, touch1.y_axis, width, touch1.y_axis, highLinePaint);
        } else {
            canvas.drawLine(0f, touch1.y_axis, width, touch1.y_axis, linePaint);
        }
        if (touch2.movable) {
            canvas.drawLine(0f, touch2.y_axis, width, touch2.y_axis, highLinePaint);
        } else {
            canvas.drawLine(0f, touch2.y_axis, width, touch2.y_axis, linePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // get pointer index from the event object
        int pointerIndex = event.getActionIndex();

        // get pointer ID
        int pointerId = event.getPointerId(pointerIndex);

        // get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();

        switch (maskedAction) {

            case MotionEvent.ACTION_DOWN:

            case MotionEvent.ACTION_POINTER_DOWN: {
                // We have a new pointer. Lets add it to the list of pointers
                PointF f = new PointF();
                f.x = event.getX(pointerIndex);
                f.y = event.getY(pointerIndex);
                mActivePointers.put(pointerId, f);
                //check lines touch or not
                if (Math.abs(f.y - touch1.y_axis) < delta) {
                    touch1.movable = true;
                    touch1.eventId = pointerId;
                } else if (Math.abs(f.y - touch2.y_axis) < delta) {
                    touch2.movable = true;
                    touch2.eventId = pointerId;
                }

                break;
            }
            case MotionEvent.ACTION_MOVE: { // a pointer was moved
                for (int size = event.getPointerCount(), i = 0; i < size; i++) {
                    PointF point = mActivePointers.get(event.getPointerId(i));
                    if (point != null) {
                        point.x = event.getX(i);
                        point.y = event.getY(i);

                        //moving line
                        if (touch1.movable && event.getPointerId(i) == touch1.eventId) {
                            touch1.y_axis = (int) event.getY(i);

                        }
                        if (touch2.movable && event.getPointerId(i) == touch2.eventId) {
                            touch2.y_axis = (int) event.getY(i);
                        }
                    }

                    result = calculateMeasure();

                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {
                //release touched lines
                mActivePointers.remove(pointerId);
                if (touch1.movable && pointerId == touch1.eventId) {
                    touch1.movable = false;
                }
                if (touch2.movable && pointerId == touch2.eventId) {
                    touch2.movable = false;
                }
                break;
            }
        }
        this.postInvalidate();
        return true;
    }

    public void setRulerType(RulerType type) {
        this.rulerType = type;
        //recalculate the result
        result = calculateMeasure();
    }

    private void init(Context context) {
        mActivePointers = new SparseArray<>();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        //divide inch by 16 because we use each unit = 1/16 inch
        pixelPerInch = dm.ydpi / 16;
        //use unit by mm
        pixelPerMillimeter = dm.ydpi / 25.4f;

        rulerPaint = new Paint();
        rulerPaint.setStyle(Paint.Style.STROKE);
        rulerPaint.setStrokeWidth(0);
        rulerPaint.setAntiAlias(false);
        rulerPaint.setColor(Color.WHITE);

        rulerInchPaint = new Paint();
        rulerInchPaint.setStyle(Paint.Style.STROKE);
        rulerInchPaint.setStrokeWidth(2);
        rulerInchPaint.setAntiAlias(false);
        rulerInchPaint.setColor(Color.WHITE);

        textPaint = new TextPaint();
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
        textPaint.setTypeface(typeface);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(0);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(getResources().getDimension(R.dimen.txt_size));
        textPaint.setColor(Color.WHITE);

        linePaint = initPaintLine(context, R.color.yellow);
        highLinePaint = initPaintLine(context, R.color.cyan);

        delta = (int) getResources().getDimension(R.dimen.touch_size);

        scaleLineLevel1 = (int) getResources().getDimension(R.dimen.scale_line_1);
        scaleLineLevel2 = (int) getResources().getDimension(R.dimen.scale_line_2);
        scaleLineLevel3 = (int) getResources().getDimension(R.dimen.scale_line_3);
        scaleLineLevel4 = (int) getResources().getDimension(R.dimen.scale_line_4);
        scaleLineLevel5 = (int) getResources().getDimension(R.dimen.scale_line_5);
        textStartPoint = (int) getResources().getDimension(R.dimen.text_start_point);


    }

    private void drawCm(Canvas canvas) {
        startPoint = 20;
        int i = 0;
        while (true) {
            if (startPoint > screenSize - 20) {
                break;
            }

            int size = (i % 10 == 0) ? scaleLineLevel3 : (i % 5 == 0) ? scaleLineLevel2 : scaleLineLevel1;
            canvas.drawLine(width - size, startPoint, width, startPoint, rulerPaint);
            if (i % 10 == 0) {

                canvas.drawText((i / 10) + getUnit(rulerType), width - textStartPoint, startPoint + 8, textPaint);
            }
            startPoint = startPoint + pixelPerMillimeter;
            i++;
        }
    }

    //initial line with normal state and touched state
    private Paint initPaintLine(Context context, int color) {
        float strokeWidth = getResources().getDimension(R.dimen.line_size);

        Paint linePaint = new Paint();
        linePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        linePaint.setColor(ContextCompat.getColor(context, color));
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeWidth(strokeWidth);
        linePaint.setPathEffect(new CornerPathEffect(10));
        linePaint.setAntiAlias(true);
        return linePaint;
    }

    private void drawInch(Canvas canvas) {
        startPoint = 20;
        int i = 0;
        while (true) {
            if (startPoint > screenSize - 20) {
                break;
            }
            int size;
            Paint paint = rulerPaint;

            if (i % 16 == 0) {
                size = scaleLineLevel5;
                paint = rulerInchPaint;
                canvas.drawText((i / 16) + getUnit(rulerType), width - textStartPoint, startPoint + 8, textPaint);
            } else if (i % 8 == 0) {
                size = scaleLineLevel4;
                canvas.drawText((i / 8) + "/2", width - textStartPoint, startPoint + 8, textPaint);
            } else if (i % 4 == 0) {
                size = scaleLineLevel3;
            } else {
                size = i % 2 == 0 ? scaleLineLevel2 : scaleLineLevel1;

            }
            canvas.drawLine(width - size, startPoint, width, startPoint, paint);

            startPoint = startPoint + pixelPerInch;
            i++;
        }
    }



    /**
     * Calculate measure
     *
     * @return result in inch or cm
     */
    private double calculateMeasure() {

        double distance = Math.abs(touch1.y_axis - touch2.y_axis);
        if (rulerType == RulerType.CM) {
            return distance / pixelPerMillimeter / 10;
        } else {
            return distance / pixelPerInch / 16;
        }
    }

    private String getUnit(RulerType type) {
        String out = type == RulerType.CM ? getContext().getString(R.string.cm) : getContext().getString(R.string.inch);
        return out;
    }


}