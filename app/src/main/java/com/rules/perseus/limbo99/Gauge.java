package com.rules.perseus.limbo99;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;
import java.util.Random;

public final class Gauge extends View {

    private static final String TAG = Gauge.class.getSimpleName();

    // drawing tools
    private RectF rimRect;
    private Paint rimPaint;
    private Paint rimCirclePaint;

    private RectF faceRect;
    private Bitmap faceTexture;
    private Paint facePaint;
    private Paint rimShadowPaint;

    private Paint scalePaint;
    private RectF scaleRect;

    private Paint titlePaint;
    private Path titlePath;

    private Paint turdPaint;
    private Bitmap turdBitmap;
    private Matrix turdMatrix;
    private float turdScale;

    private Paint steamPaint;
    private Bitmap steamBitmap;
    private Matrix steamMatrix;
    private float steamScale;

    private Paint handPaint;
    private Path handPath;
    private Paint handScrewPaint;

    private Paint backgroundPaint;
    // end drawing tools

    private Bitmap background; // holds the cached static part

    // scale configuration
    private static final int totalNicks = 30;
    private static final float valuesPerNick = 180.0f / totalNicks;
    private static final int centerValue = 50; // the one in the top center (12 o'clock)
    private static final int minValue = 0;
    private static final int maxValue = 100;

    private boolean handInitialized = false;
    private float handPosition = centerValue;
    private float handTarget = centerValue; // of the animation itself (will changed when jittering)
    private float valueTarget = centerValue; // of the actual value (won't change when jittering)
    private float handVelocity = 0.0f;
    private float handAcceleration = 0.0f;
    private long lastHandMoveTime = -1L;

    private boolean jitterState = true;
    private boolean oscillationSign = true;

    // defines the max deflection when jittering
    int maxJitterDeflection = 4;

    // defines how fast the hand is moving:
    private float accFactorJitter = 75.0f;

    private float accFactorValue = 10.0f;

    // defines the size of the logo:
    private float logoScaleFactor = 0.375f;

    public Gauge(Context context) {
        super(context);
        init();

    }

    public Gauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

        setValueTarget(50.0f);
    }

    public Gauge(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        Parcelable superState = bundle.getParcelable("superState");
        super.onRestoreInstanceState(superState);

        handInitialized = bundle.getBoolean("handInitialized");
        handPosition = bundle.getFloat("handPosition");
        handPosition = bundle.getFloat("handPosition");
        handTarget = bundle.getFloat("handTarget");
        handVelocity = bundle.getFloat("handVelocity");
        handAcceleration = bundle.getFloat("handAcceleration");
        lastHandMoveTime = bundle.getLong("lastHandMoveTime");
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        Bundle state = new Bundle();
        state.putParcelable("superState", superState);
        state.putBoolean("handInitialized", handInitialized);
        state.putFloat("handPosition", handPosition);
        state.putFloat("handTarget", handTarget);
        state.putFloat("handVelocity", handVelocity);
        state.putFloat("handAcceleration", handAcceleration);
        state.putLong("lastHandMoveTime", lastHandMoveTime);
        return state;
    }

    private void init() {

        initDrawingTools();
    }

    private String getTitle() {
        return "abcdefgh";
    }

    private void initDrawingTools() {

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

//        rimRect = new RectF(0.1f, 0.1f, 0.9f, 0.9f);
        rimRect = new RectF(0.01f, 0.01f, 0.99f, 0.99f);

        // the linear gradient is a bit skewed for realism
        rimPaint = new Paint();
        rimPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        rimPaint.setShader(new LinearGradient(0.40f, 0.0f, 0.60f, 1.0f,
                Color.rgb(0xf0, 0xf5, 0xf0),
                Color.rgb(0x30, 0x31, 0x30),
                Shader.TileMode.CLAMP));

        rimCirclePaint = new Paint();
        rimCirclePaint.setAntiAlias(true);
        rimCirclePaint.setStyle(Paint.Style.STROKE);
        rimCirclePaint.setColor(Color.argb(0x4f, 0x33, 0x36, 0x33));
        rimCirclePaint.setStrokeWidth(0.005f);

        float rimSize = 0.02f;
        faceRect = new RectF();
        faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize,
                rimRect.right - rimSize, rimRect.bottom - rimSize);

        faceTexture = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.plastic);
        BitmapShader paperShader = new BitmapShader(faceTexture,
                Shader.TileMode.MIRROR,
                Shader.TileMode.MIRROR);
        Matrix paperMatrix = new Matrix();
        facePaint = new Paint();
        facePaint.setFilterBitmap(true);
        paperMatrix.setScale(1.0f / faceTexture.getWidth(),
                1.0f / faceTexture.getHeight());
        paperShader.setLocalMatrix(paperMatrix);
        facePaint.setStyle(Paint.Style.FILL);
        facePaint.setShader(paperShader);

        rimShadowPaint = new Paint();
        rimShadowPaint.setShader(new RadialGradient(0.5f, 0.5f, faceRect.width() / 2.0f,
                new int[] { 0x00000000, 0x00000500, 0x50000500 },
                new float[] { 0.96f, 0.96f, 0.99f },
                Shader.TileMode.MIRROR));
        rimShadowPaint.setStyle(Paint.Style.FILL);

        scalePaint = new Paint();
        scalePaint.setStyle(Paint.Style.STROKE);
        scalePaint.setColor(0x9f004d0f);
        scalePaint.setStrokeWidth(0.005f);
        scalePaint.setAntiAlias(true);

        scalePaint.setTextSize(0.05f); // was 0.045f
        scalePaint.setTextScaleX(0.8f);
        scalePaint.setTypeface(Typeface.SANS_SERIF);

        scalePaint.setTextAlign(Paint.Align.CENTER);
        scalePaint.setLinearText(true);

        float scalePosition = 0.035f;
        scaleRect = new RectF();
        scaleRect.set(faceRect.left + scalePosition, faceRect.top + scalePosition,
                faceRect.right - scalePosition, faceRect.bottom - scalePosition);

        titlePaint = new Paint();
        titlePaint.setColor(0xaf946109);
        titlePaint.setAntiAlias(true);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTextSize(0.05f);
        titlePaint.setTextScaleX(0.8f);

        titlePath = new Path();
        titlePath.addArc(new RectF(0.24f, 0.24f, 0.76f, 0.76f), -180.0f, -180.0f);

        turdPaint = new Paint();
        turdPaint.setFilterBitmap(true);
        turdBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.turd_transparent);
        turdMatrix = new Matrix();
        turdScale = (1.0f / turdBitmap.getWidth()) * logoScaleFactor;
        turdMatrix.setScale(turdScale, turdScale);

        steamPaint = new Paint();
        steamPaint.setFilterBitmap(true);
        steamBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.steam_only);
        steamMatrix = new Matrix();
        steamScale = (1.0f / steamBitmap.getWidth()) * logoScaleFactor;
        steamMatrix.setScale(steamScale, steamScale);

        handPaint = new Paint();
        handPaint.setAntiAlias(true);
        handPaint.setColor(0xff392f2c);
        handPaint.setShadowLayer(0.01f, -0.005f, -0.005f, 0x7f000000);
        handPaint.setStyle(Paint.Style.FILL);

        float handCenter = 0.5f;
        float handLength = 0.40f;
        float rearDim = 0.125f;

        handPath = new Path();
        handPath.moveTo(handCenter, handCenter + rearDim);
        handPath.lineTo(handCenter - 0.010f, handCenter + rearDim - 0.007f);
        handPath.lineTo(handCenter - 0.002f, handCenter - handLength);
        handPath.lineTo(handCenter + 0.002f, handCenter - handLength);
        handPath.lineTo(handCenter + 0.010f, handCenter + rearDim - 0.007f);
        handPath.lineTo(handCenter, handCenter + rearDim);
        handPath.addCircle(handCenter, handCenter, 0.025f, Path.Direction.CW);

        handScrewPaint = new Paint();
        handScrewPaint.setAntiAlias(true);
        handScrewPaint.setColor(0xff493f3c);
        handScrewPaint.setStyle(Paint.Style.FILL);

        backgroundPaint = new Paint();
        backgroundPaint.setFilterBitmap(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        Log.d(TAG, "Width spec: " + MeasureSpec.toString(widthMeasureSpec));
        Log.d(TAG, "Height spec: " + MeasureSpec.toString(heightMeasureSpec));

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int chosenWidth = chooseWidth(widthMode, widthSize);
        int chosenHeight = chooseHeight(heightMode, heightSize);

        setMeasuredDimension(chosenWidth, chosenHeight);
    }

    private int chooseWidth(int mode, int size) {
        if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
            return size;
        } else { // (mode == MeasureSpec.UNSPECIFIED)
            return getPreferredWidth();
        }
    }
    private int chooseHeight(int mode, int size) {
        if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
            return (int) (size/2 + size*0.25);
        } else { // (mode == MeasureSpec.UNSPECIFIED)
            return getPreferredHeight();
        }
    }

    // in case there is no size specified
    private int getPreferredWidth() {
        return 300;
    }
    private int getPreferredHeight() {
        return 200;
    }

    private void drawRim(Canvas canvas) {

        // first, draw the metallic body
//        canvas.drawOval(rimRect, rimPaint);
        canvas.drawArc(rimRect, 0, -180, true, rimPaint);


        // now the outer rim circle
//        canvas.drawOval(rimRect, rimCirclePaint);
        canvas.drawArc(rimRect, 0, -180, true, rimCirclePaint);
    }

    private void drawFace(Canvas canvas) {

//        canvas.drawOval(faceRect, facePaint);
        canvas.drawArc(faceRect, 0, -180, true, facePaint);


        // draw the inner rim circle
//        canvas.drawOval(faceRect, rimCirclePaint);
        canvas.drawArc(faceRect, 0, -180, true, rimCirclePaint);


        // draw the rim shadow inside the face
//        canvas.drawOval(faceRect, rimShadowPaint);
        canvas.drawArc(faceRect, 0, -180, true, rimCirclePaint);
    }

    private void drawScale(Canvas canvas) {

        canvas.save(Canvas.MATRIX_SAVE_FLAG);

        canvas.rotate(-90, 0.5f, 0.5f);

        for (int i = -(totalNicks/2); i < (totalNicks/2); ++i) {
            float y1 = scaleRect.top;
            float y2 = y1 - 0.02f;

            canvas.drawLine(0.5f, y1, 0.5f, y2, scalePaint);

            if (i % 5 == 0) {

                int value = i;

                if (value >= minValue && value <= maxValue) {

                    String valueString = Integer.toString(value);
//                    canvas.drawText(valueString, 0.5f, y2 - 0.015f, scalePaint); // not text because of Lollipop bug

                }
            }

            canvas.rotate(valuesPerNick, 0.5f, 0.5f);
        }

        canvas.restore();

    }

    private float valueToAngle(float value) {

        return (value - centerValue) * (90 / (float) centerValue);
    }

    private void drawTitle(Canvas canvas) {
        String title = getTitle();
//        canvas.drawTextOnPath(title, titlePath, 0.0f,0.0f, titlePaint);
    }

    private void drawTurd(Canvas canvas) {

        canvas.save(Canvas.MATRIX_SAVE_FLAG);

        canvas.translate(0.5f - turdBitmap.getWidth() * turdScale / 2.0f, 0.5f - turdBitmap.getHeight() * turdScale);

        /*
        Format of the add parameter:

        0 x 00     00   00    00
            alpha  red  green blue

        ->  xx << 8 changed green values
            xx << 16 changed red values
            xx changed blue values
         */

        int addColor = 0x00000000;
        int multColor = 0xff338822;
//        float position = getRelativePosition();
        float position = calculateColor();

        addColor |= ((int) ((0xf0) * position) << 16);

        LightingColorFilter turdFilter = new LightingColorFilter(multColor, addColor);
        turdPaint.setColorFilter(turdFilter);

        canvas.drawBitmap(turdBitmap, turdMatrix, turdPaint);
        canvas.restore();
    }

    private void drawSteam(Canvas canvas) {

        canvas.save(Canvas.MATRIX_SAVE_FLAG);

        canvas.translate(0.5f - steamBitmap.getWidth() * steamScale / 2.0f, 0.5f - steamBitmap.getHeight() * steamScale);

        canvas.drawBitmap(steamBitmap, steamMatrix, steamPaint);
        canvas.restore();
    }

    private void drawHand(Canvas canvas) {
        if (handInitialized) {

            float handAngle = valueToAngle(handPosition);
            canvas.save(Canvas.MATRIX_SAVE_FLAG);
            canvas.rotate(handAngle, 0.5f, 0.5f);
            canvas.drawPath(handPath, handPaint);
            canvas.restore();

            canvas.drawCircle(0.5f, 0.5f, 0.01f, handScrewPaint);
        }
    }

    private void drawBackground(Canvas canvas) {
        if (background == null) {
            Log.w(TAG, "Background not created");
        } else {
            canvas.drawBitmap(background, 0, 0, backgroundPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "Size changed to " + w + "x" + h);

        regenerateBackground();
    }

    private void regenerateBackground() {
        // free the old bitmap
        if (background != null) {
            background.recycle();
        }

        background = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas backgroundCanvas = new Canvas(background);
        float scale = (float) getWidth();
        backgroundCanvas.scale(scale, scale);

        drawRim(backgroundCanvas);
        drawFace(backgroundCanvas);
        drawScale(backgroundCanvas);
        drawTitle(backgroundCanvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);

        float scale = (float) getWidth();
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(scale, scale);

        drawTurd(canvas);
        drawSteam(canvas);
        drawHand(canvas);

        canvas.restore();

        moveHand();
    }

    private void moveHand() {

        if (lastHandMoveTime != -1L) {
            long currentTime = System.currentTimeMillis();
            float delta = (currentTime - lastHandMoveTime) / 1000.0f;

            float accelerationFactor;

            if (jitterState==true) {
                accelerationFactor = accFactorJitter;
            } else {
                accelerationFactor = accFactorValue;
            }

            if (Math.abs(handVelocity) < 90.0f) {
                handAcceleration = accelerationFactor * (handTarget - handPosition);
            } else {
                handAcceleration = 0.0f;
            }

            float direction = Math.signum(handVelocity);

            handPosition += handVelocity * delta;
            handVelocity += handAcceleration * delta;

            if ((handTarget - handPosition) * direction < 1e-6f * direction) {
                //Log.i(TAG, "handPosistion: " + handPosition + " handTarget: " + handTarget + " direction: " + direction);
                if (Math.abs(handTarget - handPosition) < 1e-6f) {
                    handPosition = handTarget;
                }
                handVelocity = 0.0f;
                handAcceleration = 0.0f;
                lastHandMoveTime = -1L;
            } else {
                lastHandMoveTime = System.currentTimeMillis();
            }

            invalidate();
        } else {
            lastHandMoveTime = System.currentTimeMillis();
            moveHand();
        }

        // When the target (valueTarget or jitterTarget) is reached:
        if (Math.abs(handPosition - handTarget) <= 1.0f) {
            if (!jitterState) {
                // The movement to the valueTarget is completed, i.e. start jittering:
                setJitterTarget();
                jitterState = true;
            } else {
                // Log.i(TAG, "Jitter target reached");
                // The movement to the intermediate target (for jittering) is completed, i.e. set new value:
                setJitterTarget();
            }
            return;
        }
    }

    /*
     Return the relative position between 0 and 1 for color calculation.

     maxJitterDeflection is added to avoid discontinuous behavior.
      */
    private float getRelativePosition() {

        float tmp = 0.0f;
        if (handPosition < centerValue) {
            tmp = - (centerValue - handPosition) / (float) (centerValue - (minValue - maxJitterDeflection));
        } else {
            tmp = (handPosition - centerValue) / (float) ((maxValue + maxJitterDeflection) - centerValue);
        }

        return (0.5f * tmp) + 0.5f;
    }

    /*
     We want the color to change less in the beginning (i.e. stay brown longer) and more in the end,
     so we apply a parabolic function on to of the relative position between 0 and 1:
     f(x) = x^2
     */
    private float calculateColor() {
        return ((float) Math.pow(getRelativePosition(), 2));
    }

    public void setValueTarget(float newValue) {
        jitterState = false;
        // Log.i(TAG, "setValueTarget");
        handTarget = newValue;
        valueTarget = newValue;
        handInitialized = true;
        invalidate();
    }

    private void setJitterTarget() {
        // Log.i(TAG, "setJitterTarget");

        Random rand = new Random();
        int oscillation = rand.nextInt(maxJitterDeflection);

        if (oscillationSign) {
            handTarget = valueTarget + oscillation;
            oscillationSign = false;
        } else {
            handTarget = valueTarget - oscillation;
            oscillationSign = true;
        }
        handInitialized = true;
        invalidate();
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }
}
