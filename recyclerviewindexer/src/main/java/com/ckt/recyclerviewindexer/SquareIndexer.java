package com.ckt.recyclerviewindexer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;

/**
 * This class is used to draw square indicator.
 */

public class SquareIndexer extends SimpleIndexer {
    private RectF mSquareRect;
    private TextPaint mSquareTextPaint;
    private Paint mSquarePaint;

    public SquareIndexer(Builder builder) {
        super(builder);
        mSquarePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSquarePaint.setStyle(Paint.Style.FILL);
        mSquarePaint.setColor(mIndicatorBgColor);

        mSquareTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mSquareTextPaint.setColor(Color.WHITE);
        mSquareTextPaint.setTextSize(mIndexerTextSize * 2);

        Paint.FontMetrics fontMetrics = mSquareTextPaint.getFontMetrics();
        float balloonBoundSize = fontMetrics.bottom - fontMetrics.top;
        float diameter = (float) Math.hypot(balloonBoundSize, balloonBoundSize);

        mSquareRect = new RectF(0, 0, diameter, diameter);
    }

    @Override
    public void drawIndicator(Canvas c, RectF outer, float indicatorBaseY, String indicatorChar) {
        c.translate((c.getWidth() - mSquareRect.width()) / 2.f,
                (c.getHeight() - mSquareRect.height()) / 2.f);
        float radius = mSquareRect.width() / 8.f;
        c.drawRoundRect(mSquareRect, radius, radius, mSquarePaint);

        mSquareTextPaint.getTextBounds(indicatorChar, 0, indicatorChar.length(), mTmpTextBound);
        c.drawText(indicatorChar, mSquareRect.width() / 2.f - mTmpTextBound.width() / 2.f,
                mSquareRect.width() / 2.f + mTmpTextBound.height() / 2.f, mSquareTextPaint);
    }
}
