package view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * 时间尺控件
 * Created by king on 2016/9/18.
 */

public class TimeScaleView extends View {
    private int viewWidth;
    private int viewHeight;
    private Paint linePaint = new Paint();
    private Paint midPaint = new Paint();
    private Paint textPaint = new Paint();
    private Paint timePaint = new Paint();
    private Paint bgPaint = new Paint();
    //时间间隔用小时计算
    private int timeScale = 1;
    //时间的长度
    private int totalTime = 1;
    //滚动
    private Scroller scroller;
    float lastX = 0;
    //选中的数据
    private List<TimePart> data;
    //矩形
    private Rect rect;
    //选中时间片段颜色
    private String timePartColor = "#02A7DD";
    //背景颜色，可以修改
    private String bgColor = "#20000000";

    //滚动监听
    private OnScrollListener scrollListener;

    public TimeScaleView(Context context) {
        super(context);
    }

    public TimeScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.BLACK);
        linePaint.setTextAlign(Paint.Align.CENTER);
        linePaint.setTextSize(25);

        timePaint.setAntiAlias(true);
        timePaint.setColor(Color.parseColor(timePartColor));

        midPaint.setAntiAlias(true);
        midPaint.setStrokeWidth(3);
        midPaint.setColor(Color.RED);

        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLUE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(28);

        bgPaint.setAntiAlias(true);
        bgPaint.setColor(Color.parseColor(bgColor));

        //数据设置
        data = new ArrayList<>();
        rect = new Rect();

        scroller = new Scroller(context);
    }

    public TimeScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 画背景
     *
     * @param canvas 画布
     */
    public void drawBg(Canvas canvas) {
        rect.set(-1, 0, timeScale * 24 + 1, viewHeight);
        canvas.drawRect(rect, bgPaint);
    }

    /**
     * 画刻度
     *
     * @param canvas 画布
     */
    public void drawLines(Canvas canvas) {
        //底部的线
        canvas.drawLine(0, (float) (viewHeight * 0.9), totalTime,
                (float) (viewHeight * 0.9), linePaint);
        for (int i = 0; i <= totalTime; i++) {
            if (i % timeScale == 0) {
                canvas.drawLine(i, (float) (viewHeight * 0.7), i,
                        (float) (viewHeight * 0.9), linePaint);
                //画刻度值
                canvas.drawText(
                        formatString(i / timeScale, 0, 0), i, (float) (viewHeight * 0.6), linePaint);
            }
        }
    }

    /**
     * 画时间片段
     *
     * @param canvas
     */
    public void drawTimeRect(Canvas canvas) {
        for (TimePart temp : data) {
            int seconds1 = temp.sHour * 3600 + temp.sMinute * 60 + temp.sSeconds;
            int seconds2 = temp.eHour * 3600 + temp.eMinute * 60 + temp.eSeconds;
            //如果是先除以3600小数点的数据会被舍去 位置就不准确了
            int x1 = seconds1 * timeScale / 3600;
            int x2 = seconds2 * timeScale / 3600;
            rect.set(x1, 0, x2, (int) (viewHeight * 0.9));
            canvas.drawRect(rect, timePaint);
        }
    }

    /**
     * 画指针
     *
     * @param canvas 画布
     */
    public void drawMidLine(Canvas canvas) {
        //移动的距离整个view内容移动的距离
        int finalX = scroller.getFinalX();
        //表示每一个屏幕刻度的一半的总秒数，每一个屏幕有6格
        int sec = 3 * 3600;
        //滚动的秒数
        int temsec = (int) Math.rint((double) finalX / (double) timeScale * 3600);
        sec += temsec;
        //获取的时分秒
        int thour = sec / 3600;
        int tmin = (sec - thour * 3600) / 60;
        int tsec = sec - thour * 3600 - tmin * 60;
        //滚动时的监听
        if (scrollListener != null) {
            scrollListener.onScroll(thour, tmin, tsec);
        }
        //画指针
        canvas.drawLine(timeScale * 3 + finalX, 0,
                timeScale * 3 + finalX, viewHeight, midPaint);
        //画数字
        canvas.drawText(formatString(thour, tmin, tsec), timeScale * 3 + finalX,
                (float) (viewHeight * 0.3), textPaint);
    }

    /**
     * 对时间进行格式化
     *
     * @param hour 小时
     * @param min  分钟
     * @param sec  秒
     * @return 字符串数字
     */
    public String formatString(int hour, int min, int sec) {
        StringBuilder builder = new StringBuilder();
        if (hour < 10) {
            builder.append("0").append(hour).append(":");
        } else {
            builder.append(hour).append(":");
        }
        if (min < 10 && min >= 0) {
            builder.append("0").append(min).append(":");
        } else {
            builder.append(min).append(":");
        }
        if (sec < 10 && sec >= 0) {
            builder.append("0").append(sec);
        } else {
            builder.append(sec);
        }
        return builder.toString();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (scroller != null && !scroller.isFinished()) {
                    scroller.abortAnimation();
                }
                lastX = x;
                return true;
            case MotionEvent.ACTION_MOVE:
                float dataX = lastX - x;
                int finalx = scroller.getFinalX();
                //右边
                if (dataX < 0) {
                    if (finalx < -viewWidth / 2) {
                        return super.onTouchEvent(event);
                    }
                }
                if (dataX > 0) {
                    if (finalx > timeScale * 21) {
                        return super.onTouchEvent(event);
                    }
                }
//                Log.d("--startScroll--","getFinalX "+scroller.getFinalX()+"getFinalY "+scroller.getFinalY());
                scroller.startScroll(scroller.getFinalX(), scroller.getFinalY(), (int) dataX, 0);
                lastX = x;
                postInvalidate();
                return true;
            case MotionEvent.ACTION_UP:
                int finalx1 = scroller.getFinalX();
                if (finalx1 < -viewWidth / 2) {
                    scroller.setFinalX(-viewWidth / 2);
                }
                if (finalx1 > timeScale * 21) {
                    scroller.setFinalX(timeScale * 21);
                }
                if (scrollListener != null) {
                    int finalX = scroller.getFinalX();
                    //表示每一个屏幕刻度的一半的总秒数，每一个屏幕有6格
                    int sec = 3 * 3600;
                    //滚动的秒数
                    int temsec = (int) Math.rint((double) finalX / (double) timeScale * 3600);
                    sec += temsec;
                    //获取的时分秒
                    int thour = sec / 3600;
                    int tmin = (sec - thour * 3600) / 60;
                    int tsec = sec - thour * 3600 - tmin * 60;
                    scrollListener.onScrollFinish(thour, tmin, tsec);
                }
                postInvalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        viewWidth = getWidth();
        viewHeight = getHeight();
        //每小时的刻度一个屏幕分成6格
        timeScale = viewWidth / 6;
        //总的时间刻度距离
        totalTime = timeScale * 24;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBg(canvas);
        //选中的时间
        drawTimeRect(canvas);
        drawLines(canvas);
        drawMidLine(canvas);
    }

    //滚动监听类
    public interface OnScrollListener {
        public void onScroll(int hour, int min, int sec);

        public void onScrollFinish(int hour, int min, int sec);
    }

    public OnScrollListener getScrollListener() {
        return scrollListener;
    }

    public void setScrollListener(OnScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    //添加时间片段到容器中
    public void addTimePart(List<TimePart> temp) {
        if (temp != null) {
            data.addAll(temp);
            postInvalidate();
        }
    }

    //清除所有的时间片段数据
    public void clearData() {
        data.clear();
        postInvalidate();
    }

    public String getTimePartColor() {
        return timePartColor;
    }

    //设置时间片段的颜色
    public void setTimePartColor(String timePartColor) {
        this.timePartColor = timePartColor;
        postInvalidate();
    }

    //时间片段 用于标记选中的时间
    public static class TimePart {
        //开始的时间
        public int sHour, sMinute, sSeconds;
        //结束的时间
        public int eHour, eMinute, eSeconds;

        public TimePart(int sHour, int sMinute, int sSeconds, int eHour, int eMinute, int eSeconds) {
            this.sHour = sHour;
            this.sMinute = sMinute;
            this.sSeconds = sSeconds;
            this.eHour = eHour;
            this.eMinute = eMinute;
            this.eSeconds = eSeconds;
        }
    }
}
