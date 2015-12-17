package com.asdzheng.smradarscanview;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 * Created by asdzheng
 */
public class RadarScanView extends View {
    private static final int DEFAULT_WIDTH = 80;
    private static final int DEFAULT_HEIGHT = 80;

    private int defaultWidth;
    private int defaultHeight;
    private int startScanDegree = 270;
    private int startClearDegree = 0;
    private int centerX;
    private int centerY;
    private int radarRadius;

    //OutSiseCicleColor
    private int circleColor = Color.parseColor("#7197ED");
    private int innerCircleColor = Color.parseColor("#678EE6");
    private int layerColor = Color.parseColor("#30FAFAFA");

    private int innerTextColor = Color.parseColor("#FFFFFF");
    private int innerTextSize = dip2px(getContext(), 15);

    private int shaderColor1 = Color.parseColor("#00FAFAFA");
    private int shaderColor2 = Color.parseColor("#59FAFAFA");
    private int radarLineColor = Color.WHITE;

    private int borderWidth = dip2px(getContext(), 10);

    private Paint mPaintCircle;
    private Paint mPaintInnerCircle;
    private Paint mPaintStroke;

    private Paint mPaintFillOutSize;
    private Paint mPaintStrokeOutSize;

    private Paint mPaintRadar;
    private Paint mPaintRadarLine;

    private Paint mPaintText;

    private Matrix scanMatrix;

    private Paint mPaintClear;

    private boolean isPutWhiteLayer = false;
    private Canvas layerCanvas;

    private Bitmap layerBitmap;

    private Handler handler = new Handler();

    private RectF clearRect;

    private boolean isClearing = false;
    private boolean isScanning = false;

    private float textY;

    private boolean isShowText = true;

    private double collectionNum;
    private double pieceOfNum;
    private String unit = "M";

    //默认清除时间为3.6s
    private int clearTime = 3600;

    private Runnable run = new Runnable() {
        @Override
        public void run() {
            scanMatrix.reset();
            startScanDegree += 2;
            scanMatrix.postRotate(startScanDegree, centerX, centerY);

            postInvalidate();
            if (isScanning) {
                handler.postDelayed(run, 10);
            }

        }
    };

    private Runnable clearRun = new Runnable() {
        @Override
        public void run() {
            if (isClearing && startClearDegree > -360) {
                startClearDegree -= 1;
                if (collectionNum > 0) {
                    collectionNum = collectionNum - pieceOfNum;
                } else {
                    return;
                }
                postInvalidate();
                handler.postDelayed(clearRun, clearTime / 360);
            } else {
                isClearing = false;
            }
        }
    };

    public RadarScanView(Context context) {
        super(context);
        init(null, context);
    }

    public RadarScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, context);
    }

    public RadarScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, context);
    }

    @TargetApi(21)
    public RadarScanView(Context context, AttributeSet attrs, int defStyleAttr,
                         int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Log.i("Radar", w + "");

        centerX = w / 2;
        centerY = h / 2;
        //drawText高度会有一些偏差，这样的设置会让文字居中显示
        textY = centerY - ((mPaintText.descent() + mPaintText.ascent()) / 2);

        radarRadius = Math.min(centerX, centerY) - 2 * borderWidth;
        //多一层白色蒙层
        layerBitmap = Bitmap.createBitmap(2 * radarRadius, 2 * radarRadius, Bitmap.Config.ARGB_8888);
        layerCanvas = new Canvas(layerBitmap);
        layerCanvas.drawColor(layerColor);

        clearRect = new RectF();
        clearRect.bottom = 2 * radarRadius;
        clearRect.top = 0;
        clearRect.left = 0;
        clearRect.right = 2 * radarRadius;
    }

    private void init(AttributeSet attrs, Context context) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs,
                    R.styleable.RadarScanView);
            circleColor = ta.getColor(R.styleable.RadarScanView_circleColor, circleColor);
            innerCircleColor = ta.getColor(R.styleable.RadarScanView_innerCircleColor, innerCircleColor);
            layerColor = ta.getColor(R.styleable.RadarScanView_layerColor, layerColor);

            innerTextColor = ta.getColor(R.styleable.RadarScanView_innerTextColor, innerTextColor);
            innerTextSize = ta.getInteger(R.styleable.RadarScanView_innerTextSize, innerTextSize);

            shaderColor1 = ta.getColor(R.styleable.RadarScanView_radarShaderColor1, shaderColor1);
            shaderColor2 = ta.getColor(R.styleable.RadarScanView_radarShaderColor2, shaderColor2);
            radarLineColor = ta.getColor(R.styleable.RadarScanView_radarLineColor, radarLineColor);

            borderWidth = ta.getInteger(R.styleable.RadarScanView_borderWidth, borderWidth);

            ta.recycle();
        }

        defaultWidth = dip2px(context, DEFAULT_WIDTH);
        defaultHeight = dip2px(context, DEFAULT_HEIGHT);

        initPaint();
//        colors = new int[]{Color.parseColor("#00FAFAFA"),
//                Color.parseColor("#59FAFAFA")};
        //positions = new float[]{0, 1.0f};

        scanMatrix = new Matrix();
    }

    private void initPaint() {
        mPaintCircle = new Paint();
        mPaintCircle.setColor(circleColor);
        mPaintCircle.setAntiAlias(true);//抗锯齿
        mPaintCircle.setStyle(Paint.Style.FILL);//设置实心

        mPaintInnerCircle = new Paint();
        mPaintInnerCircle.setColor(innerCircleColor);
        mPaintCircle.setAntiAlias(true);//抗锯齿
        mPaintCircle.setStyle(Paint.Style.FILL);//设置实心

        mPaintRadar = new Paint();
        mPaintRadar.setAntiAlias(true);

        mPaintRadarLine = new Paint();
        mPaintRadarLine.setColor(radarLineColor);
        mPaintRadarLine.setStrokeWidth(3);
        mPaintRadarLine.setAntiAlias(true);

        //whiteStokeCicle
        mPaintStroke = new Paint();
        mPaintStroke.setColor(Color.parseColor("#AEC4F4"));
        mPaintStroke.setAntiAlias(true);//抗锯齿
        mPaintStroke.setStyle(Paint.Style.STROKE);//设置为空心

        mPaintFillOutSize = new Paint();
        mPaintFillOutSize.setColor(Color.parseColor("#FEFAFA"));
        mPaintFillOutSize.setAntiAlias(true);//抗锯齿
        mPaintFillOutSize.setStyle(Paint.Style.FILL);//设置为空心

        mPaintStrokeOutSize = new Paint();
        mPaintStrokeOutSize.setColor(Color.parseColor("#C8CCD7"));
        mPaintStrokeOutSize.setAntiAlias(true);//抗锯齿
        mPaintStrokeOutSize.setStyle(Paint.Style.STROKE);//设置为空心
        mPaintStrokeOutSize.setStrokeWidth(2);

        //ClearPaint
        mPaintClear = new Paint();
        mPaintClear.setAlpha(0);
        mPaintClear.setColor(Color.BLACK); // 此处不能为透明色
        mPaintClear.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mPaintClear.setStyle(Paint.Style.FILL);
        mPaintClear.setAntiAlias(true);

        //TextColor
        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText.setTextSize(innerTextSize);
        mPaintText.setColor(innerTextColor);
        mPaintText.setTextAlign(Paint.Align.CENTER);
        mPaintText.setFakeBoldText(false);
        mPaintText.setTypeface(Typeface.SANS_SERIF);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int resultWidth = 0;
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);

        if (modeWidth == MeasureSpec.EXACTLY) {
            resultWidth = sizeWidth;
        } else {
            resultWidth = defaultWidth;
            if (modeWidth == MeasureSpec.AT_MOST) {
                resultWidth = Math.min(resultWidth, sizeWidth);
            }
        }

        int resultHeight = 0;
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (modeHeight == MeasureSpec.EXACTLY) {
            resultHeight = sizeHeight;
        } else {
            resultHeight = defaultHeight;
            if (modeHeight == MeasureSpec.AT_MOST) {
                resultHeight = Math.min(resultHeight, sizeHeight);
            }
        }

        setMeasuredDimension(resultWidth, resultHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(centerX, centerY, radarRadius + borderWidth, mPaintFillOutSize);
        canvas.drawCircle(centerX, centerY, radarRadius + borderWidth, mPaintStrokeOutSize);

        canvas.drawCircle(centerX, centerY, radarRadius, mPaintCircle);

        //分别绘制四个圆
        canvas.drawCircle(centerX, centerY, 3 * radarRadius / 5, mPaintInnerCircle);

        canvas.drawCircle(centerX, centerY, radarRadius / 5, mPaintStroke);
        canvas.drawCircle(centerX, centerY, 2 * radarRadius / 5, mPaintStroke);
        canvas.drawCircle(centerX, centerY, 3 * radarRadius / 5, mPaintStroke);
        canvas.drawCircle(centerX, centerY, 4 * radarRadius / 5, mPaintStroke);

        canvas.drawLine(centerX - radarRadius, centerY, centerX + radarRadius, centerY, mPaintStroke);
        canvas.drawLine(centerX, centerY - radarRadius, centerX, centerY + radarRadius, mPaintStroke);

        if (isPutWhiteLayer) {
            canvas.drawBitmap(layerBitmap, centerX - radarRadius, centerY - radarRadius, null);
        }

        if (isClearing) {
            Log.i("Radar ", startClearDegree + "");
            layerCanvas.drawArc(clearRect, 270, startClearDegree, true, mPaintClear);
        }

        if (isShowText) {
            canvas.drawText(getShowNum(), centerX, textY, mPaintText);
        }

        if (isScanning && !isClearing) {
            //设置颜色渐变从透明到不透明
            SweepGradient shader = new SweepGradient(centerX, centerY, shaderColor1,
                    shaderColor2);
            mPaintRadar.setShader(shader);
            canvas.concat(scanMatrix);
            canvas.drawLine(centerX, centerY, centerX + radarRadius, centerY, mPaintRadarLine);
            canvas.drawCircle(centerX, centerY, radarRadius, mPaintRadar);
        }

        canvas.restore();
    }

    private String getShowNum() {
        String num;
        if (collectionNum > 0 && collectionNum < 100) {
            BigDecimal bd = new BigDecimal(collectionNum);
            bd = bd.setScale(1, RoundingMode.HALF_UP);
            num = bd.toString() + unit;
        } else if (collectionNum > 100) {
            num = ((int) collectionNum) + unit;
        } else {
            num = ((int) collectionNum) + unit;
        }

        return num;
    }

    private int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public void startScan() {
        isScanning = true;
        handler.post(run);
    }

    public void stopScan() {
        isScanning = false;
    }

    public void startClear() {
        if (isPutWhiteLayer && startClearDegree > -360) {
            pieceOfNum = collectionNum / 360;
            isClearing = true;
            handler.post(clearRun);
        }
    }

    public void stopClear() {
        isClearing = false;
    }

    public boolean isPutWhiteLayer() {
        return isPutWhiteLayer;
    }

    public void setWhiteLayer(boolean putWhiteLayer) {
        isPutWhiteLayer = putWhiteLayer;
        postInvalidate();
    }

    public int getClearTime() {
        return clearTime;
    }

    public void setClearTime(int clearTime) {
        this.clearTime = clearTime;
    }

    public double getCollectionNum() {
        return collectionNum;
    }

    public void setCollectionNum(double collectionNum) {
        this.collectionNum = collectionNum;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setShaderColor(int color1, int color2) {
        shaderColor1 = color1;
        shaderColor2 = color2;
    }

    public int getInnerTextColor() {
        return innerTextColor;
    }

    public void setInnerTextColor(int innerTextColor) {
        this.innerTextColor = innerTextColor;
    }

    public int getInnerTextSize() {
        return innerTextSize;
    }

    public void setInnerTextSize(int innerTextSize) {
        this.innerTextSize = innerTextSize;
    }

    public int getLayerColor() {
        return layerColor;
    }

    public void setLayerColor(int layerColor) {
        this.layerColor = layerColor;
    }

    public int getInnerCircleColor() {
        return innerCircleColor;
    }

    public void setInnerCircleColor(int innerCircleColor) {
        this.innerCircleColor = innerCircleColor;
    }

    public int getCircleColor() {
        return circleColor;
    }

    public void setCircleColor(int circleColor) {
        this.circleColor = circleColor;
    }

    public int getRadarLineColor() {
        return radarLineColor;
    }

    public void setRadarLineColor(int radarLineColor) {
        this.radarLineColor = radarLineColor;
    }
}
