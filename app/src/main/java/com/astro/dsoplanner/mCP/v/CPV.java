/*
 * Copyright (C) 2010 Daniel Nilsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.astro.dsoplanner.mCP.v;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.astro.dsoplanner.SettingsActivity;

import java.util.Random;

/**
 * former ColorPanelView
 *
 * @author leonid
 */
public class CPV extends View {

    /**
     * The width in pixels of the border
     * surrounding the color panel.
     */
    private final static float BORDER_WIDTH_PX = 1;

    private static float mDensity = 1f;

    private int mBorderColor = 0xff6E6E6E;
    private int mColor = 0xff000000;

    private Paint mBorderPaint;
    private Paint mColorPaint;

    private RectF mDrawingRect;
    private RectF mColorRect;

    private AlphaPatternDrawable mAlphaPattern;


    public CPV(Context context) {
        this(context, null);
    }

    public CPV(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CPV(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        mBorderPaint = new Paint();
        mColorPaint = new Paint();
        mDensity = getContext().getResources().getDisplayMetrics().density;
    }


    @Override
    protected void onDraw(Canvas canvas) {

        final RectF rect = mColorRect;

        mBorderPaint.setColor(mBorderColor);
        canvas.drawRect(mDrawingRect, mBorderPaint);

        mColorPaint.setColor(mColor);
        mColorPaint.setAntiAlias(true);
        //Thin Lines
        Paint linePaint = new Paint(mColorPaint);
        linePaint.setStyle(Paint.Style.STROKE);

        //Thick Lines
        Paint stripePaint = new Paint(mColorPaint);
        stripePaint.setStyle(Paint.Style.STROKE);

        //Galaxies
        Paint galaxyPaint = new Paint(mColorPaint);
        galaxyPaint.setStyle(Paint.Style.STROKE);
        galaxyPaint.setStrokeWidth(2);

        //sky
        Paint skyPaint = new Paint(mColorPaint);
        skyPaint.setColor(0xff000000);
        canvas.drawRect(rect, skyPaint);

        //stars
        Paint starPaint = new Paint(mColorPaint);
        starPaint.setColor(0xffffffff);

        //Behind slant line
        int dheight = (int) (rect.bottom - rect.top) / 2; //shift top and bottom to crop ugly stripe ends
        stripePaint.setStrokeWidth((rect.bottom - rect.top) / 2);
        canvas.drawLine(rect.right - (rect.right - rect.left) / 5, rect.top - dheight, rect.right - (rect.right - rect.left) / 3, rect.bottom + dheight, stripePaint);

        Random R0 = new Random();
        float cx;
        float cy;
        float radius;
        for (int i = 0; i < 30; i++) {
            cx = rect.left + (rect.right - rect.left) * R0.nextInt(100) / 100f;
            cy = rect.top + (rect.bottom - rect.top) * R0.nextInt(100) / 100f;
            if (i < 8) { //8 Galaxies
                Path gxy = new Path();
                radius = SettingsActivity.dso_Scale() * (R0.nextInt(20) + 12);
                RectF oval = new RectF(cx - radius, cy - radius / 2, cx + radius, cy + radius / 2);
                gxy.addOval(oval, Direction.CW);
                Matrix matrix = new Matrix();
                matrix.setRotate(45, cx, cy);
                gxy.transform(matrix);
                canvas.drawPath(gxy, galaxyPaint);
            } else { //Star
                radius = R0.nextInt((int) (7 * mDensity));
                canvas.drawCircle(cx, cy, radius, starPaint);
            }
        }

        //Crossed Lines
        canvas.drawLine(rect.left, rect.top + (rect.bottom - rect.top) / 2, rect.right, rect.top + (rect.bottom - rect.top) / 2, linePaint);
        canvas.drawLine(rect.left + (rect.right - rect.left) / 2, rect.top, rect.left + (rect.right - rect.left) / 2, rect.bottom, linePaint);

        //Slant line over
        stripePaint.setStrokeWidth((rect.bottom - rect.top) / 6);
        canvas.drawLine(rect.left + (rect.right - rect.left) / 6, rect.top - dheight, rect.left + (rect.right - rect.left) / 3, rect.bottom + dheight, stripePaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mDrawingRect = new RectF();
        mDrawingRect.left = getPaddingLeft();
        mDrawingRect.right = w - getPaddingRight();
        mDrawingRect.top = getPaddingTop();
        mDrawingRect.bottom = h - getPaddingBottom();

        setUpColorRect();

    }

    private void setUpColorRect() {
        final RectF dRect = mDrawingRect;

        float left = dRect.left + BORDER_WIDTH_PX;
        float top = dRect.top + BORDER_WIDTH_PX;
        float bottom = dRect.bottom - BORDER_WIDTH_PX;
        float right = dRect.right - BORDER_WIDTH_PX;

        mColorRect = new RectF(left, top, right, bottom);

        mAlphaPattern = new AlphaPatternDrawable((int) (5 * mDensity));

        mAlphaPattern.setBounds(Math.round(mColorRect.left),
                Math.round(mColorRect.top),
                Math.round(mColorRect.right),
                Math.round(mColorRect.bottom));

    }

    /**
     * Set the color that should be shown by this view.
     *
     * @param color
     */
    public void setColor(int color) {
        mColor = color;
        invalidate();
    }

    /**
     * Get the color currently show by this view.
     *
     * @return
     */
    public int getColor() {
        return mColor;
    }

    /**
     * Set the color of the border surrounding the panel.
     *
     * @param color
     */
    public void setBorderColor(int color) {
        mBorderColor = color;
        invalidate();
    }

    /**
     * Get the color of the border surrounding the panel.
     */
    public int getBorderColor() {
        return mBorderColor;
    }

}
