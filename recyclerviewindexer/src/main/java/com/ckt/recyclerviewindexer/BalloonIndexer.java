package com.ckt.recyclerviewindexer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.Log;

/**
 * This class is used to draw balloon style indicator of SimpleIndexer.
 */

public class BalloonIndexer extends SimpleIndexer {

    private TextPaint mBalloonTextPaint;
    private RectF mBalloonIndicatorRect;
    private Path mBalloonPath;
    private float mOutlineMinMarginTop;
    private Paint mBalloonPaint;

    public BalloonIndexer(Builder builder) {
        super(builder);
        mBalloonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBalloonPaint.setStyle(Paint.Style.FILL);
        mBalloonPaint.setColor(mIndicatorBgColor);

        mBalloonTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mBalloonTextPaint.setColor(Color.WHITE);
        mBalloonTextPaint.setTextSize(mIndexerTextSize * 2);

        Paint.FontMetrics fontMetrics = mBalloonTextPaint.getFontMetrics();
        float balloonBoundSize = fontMetrics.bottom - fontMetrics.top;
        float diameter = (float) Math.hypot(balloonBoundSize, balloonBoundSize);

        mBalloonIndicatorRect = new RectF(0, 0, diameter, diameter);

        mBalloonPath = new Path();
        mBalloonPath.addArc(mBalloonIndicatorRect, 90, 270);
        mBalloonPath.rLineTo(0, mBalloonIndicatorRect.height() / 2);
        mBalloonPath.rLineTo(-mBalloonIndicatorRect.width() / 2, 0);

        mOutlineMinMarginTop = diameter - mCellHeight * 3.f / 2 - mPadding;
        if (mOutlineMinMarginTop < 0) {
            mOutlineMinMarginTop = 0;
        }
    }

    @Override
    public void drawIndicator(Canvas c, RectF outer, float indicatorBaseY, String indicatorChar) {
        if ((mRecyclerViewHeight - outer.height()) / 2.f <= mOutlineMinMarginTop) {
            return;
        }

        c.save();

        float dy = indicatorBaseY - mBalloonIndicatorRect.height();
        c.translate(outer.left - mBalloonIndicatorRect.width(),
                dy);
        c.drawPath(mBalloonPath, mBalloonPaint);
        mBalloonTextPaint.getTextBounds(indicatorChar, 0, indicatorChar.length(), mTmpTextBound);
        c.drawText(indicatorChar, mBalloonIndicatorRect.width() / 2.f - mTmpTextBound.width() / 2.f,
                mBalloonIndicatorRect.width() / 2.f + mTmpTextBound.height() / 2.f, mBalloonTextPaint);

        c.restore();
    }
}
