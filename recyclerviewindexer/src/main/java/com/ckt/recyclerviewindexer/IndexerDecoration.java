package com.ckt.recyclerviewindexer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;

import java.util.Objects;


/**
 * This ItemDecoration is used to draw indexer of RecyclerView.
 *
 * @author wei.zhou
 */
public class IndexerDecoration extends RecyclerView.ItemDecoration implements RecyclerView.OnItemTouchListener {
    private static final String TAG = "IndexerDecoration";

    private static final String DEFAULT_INDEXER = ContactsIndexer.DEFAULT_INDEXER_CHARACTERS;
    private String mIndexerString;

    // Text size of character in alphabet.
    public static final int DEFAULT_SECTION_TEXT_SIZE = 14;// sp

    private TextPaint mAlphabetTextPaint;

    /**
     * Used for outline.
     */
    private Paint mOutlinePaint;
    private RectF mOutlineRect;
    private Path mOutlinePath;

    // Outline's stroke width.
    private static final int DEFAULT_OUTLINE_STROKE_WIDTH = 1; //dp

    // Outline's horizontal padding.
    public static final int DEFAULT_HORIZONTAL_PADDING = 5; //dp
    private int mHorizontalPadding;

    // Character's height and width in alphabet.
    private int mCharHeight, mCharWidth;

    /**
     * Used for balloon
     */
    private Paint mBalloonPaint;
    private RectF mBalloonRect;
    private Path mBalloonPath;
    private TextPaint mBalloonTextPaint;
    private boolean mShowBalloon;
    private float mBalloonY;
    public static final int DEFAULT_BALLOON_COLOR = 0xee3F51B5;


    // Used to get text bound.
    private Rect mTextBound;

    // Outline's minimum padding top.
    private float mMinPaddingTop;

    private int mRecyclerViewWidth, mRecyclerViewHeight;

    private String mSection;

    private onSectionSelectedListener mListener;


    public IndexerDecoration(Builder builder) {
        mListener = builder.mListener;

        mIndexerString = builder.mIndexerString;
        if (mIndexerString == null) {
            mIndexerString = DEFAULT_INDEXER;
        }

        DisplayMetrics displayMetrics = builder.mContext.getResources().getDisplayMetrics();

        int sectionSize = builder.mSectionSize;
        if (builder.mSectionSize <= 0) {
            sectionSize = DEFAULT_SECTION_TEXT_SIZE;
        }
        int sectionTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sectionSize,
                displayMetrics);

        int horizontalPadding = builder.mHorizontalPadding;
        if (horizontalPadding <= 0) {
            horizontalPadding = DEFAULT_HORIZONTAL_PADDING;
        }
        mHorizontalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, horizontalPadding,
                displayMetrics);

        mAlphabetTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mAlphabetTextPaint.setTextSize(sectionTextSize);

        Paint.FontMetrics fontMetrics = mAlphabetTextPaint.getFontMetrics();
        float fontMetricsHeight = fontMetrics.bottom - fontMetrics.top;
        mCharWidth = mCharHeight = (int) Math.ceil(fontMetricsHeight);

        mOutlineRect = new RectF();
        mOutlineRect.right = mCharWidth;
        mOutlineRect.bottom = mCharHeight * mIndexerString.length() + mCharHeight;

        mOutlinePath = new Path();
        mOutlinePath.addArc(mOutlineRect.left, mOutlineRect.top, mOutlineRect.width(), mCharHeight, 180, 180);
        mOutlinePath.rLineTo(0, mOutlineRect.height() - mCharHeight);
        mOutlinePath.addArc(0, mOutlineRect.height() - mCharHeight,
                mOutlineRect.width(), mOutlineRect.height(), 0, 180);
        mOutlinePath.lineTo(0, mCharHeight / 2.f);

        mOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setColor(Color.BLACK);
        int outlineStrokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_OUTLINE_STROKE_WIDTH, displayMetrics);
        mOutlinePaint.setStrokeWidth(outlineStrokeWidth);


        mBalloonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBalloonPaint.setDither(true);
        mBalloonPaint.setStyle(Paint.Style.FILL);
        int balloonColor = builder.mBalloonColor;
        if (balloonColor <= 0) {
            balloonColor = DEFAULT_BALLOON_COLOR;
        }
        mBalloonPaint.setColor(balloonColor);


        mBalloonTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mBalloonTextPaint.setColor(Color.WHITE);
        mBalloonTextPaint.setTextSize(sectionTextSize * 2);

        fontMetrics = mBalloonTextPaint.getFontMetrics();
        float balloonBoundSize = fontMetrics.bottom - fontMetrics.top;
        float diameter = (float) Math.hypot(balloonBoundSize, balloonBoundSize);

        mBalloonRect = new RectF(0, 0, diameter, diameter);
        mBalloonPath = new Path();
        mBalloonPath.addArc(mBalloonRect, 90, 270);
        mBalloonPath.rLineTo(0, mBalloonRect.height() / 2);
        mBalloonPath.rLineTo(-mBalloonRect.width() / 2, 0);

        mMinPaddingTop = diameter - mCharHeight * 3.f / 2;
        mTextBound = new Rect();
    }


    public static class Builder {
        private Context mContext;
        private int mSectionSize; // sp
        private int mBalloonColor;
        private int mHorizontalPadding; //dp
        private String mIndexerString;
        private onSectionSelectedListener mListener;

        public Builder(Context context, onSectionSelectedListener listener) {
            mContext = context;
            mListener = Objects.requireNonNull(listener);
        }

        public Builder indexer(String indexerString) {
            mIndexerString = indexerString;
            return this;
        }

        public Builder sectionTextSize(int spSize) {
            mSectionSize = spSize;
            return this;
        }

        public Builder balloonColor(int color) {
            mBalloonColor = color;
            return this;
        }

        public Builder horizontalPadding(int dpPadding) {
            mHorizontalPadding = dpPadding;
            return this;
        }

        public IndexerDecoration build() {
            return new IndexerDecoration(this);
        }

    }

    private boolean mNeedDrawIndexer = true;

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mRecyclerViewWidth != parent.getWidth() || mRecyclerViewHeight != parent.getHeight()) {
            mRecyclerViewWidth = parent.getWidth();
            mRecyclerViewHeight = parent.getHeight();

            if (mRecyclerViewHeight - mOutlineRect.height() <= mMinPaddingTop) {
                mNeedDrawIndexer = false;
                Log.w(TAG, "Couldn't show indexer. RecyclerView must have enough height!!!");
                return;
            } else {
                mNeedDrawIndexer = true;
                // When RecyclerView's size changed, adjust outline rect.
                Log.v(TAG, "RecyclerView's size has changed, so adjust alphabet outline rect.");
                mOutlineRect.offsetTo(parent.getWidth() - mHorizontalPadding - mOutlineRect.width(),
                        parent.getHeight() / 2.f - mOutlineRect.height() / 2.f);
            }
        }

        if (!mNeedDrawIndexer) {
            return;
        }

        parent.addOnItemTouchListener(this);

        // 1. draw outline and alphabet
        drawOutlineAndAlphabet(c);

        // 2. draw balloon and section
        if (mShowBalloon && mSection != null) {
            drawBalloon(c);
        }

    }

    private void drawOutlineAndAlphabet(Canvas c) {
        c.save();
        // Draw outline
        c.translate(mOutlineRect.left, mOutlineRect.top);
        c.drawPath(mOutlinePath, mOutlinePaint);

        // Draw alphabet
        for (int i = 0; i < mIndexerString.length(); i++) {
            String character = String.valueOf(mIndexerString.charAt(i));
            mAlphabetTextPaint.getTextBounds(character, 0, character.length(), mTextBound);
            float left = mCharWidth / 2.f - mTextBound.width() / 2.f;
            float top = mCharHeight * (i + 1) + mTextBound.height() / 2;
            c.drawText(character, left, top, mAlphabetTextPaint);
        }
        c.restore();
    }

    private void drawBalloon(Canvas c) {
        c.save();
        float dy;
        if (mBalloonY == 0) {
            dy = mOutlineRect.top - mMinPaddingTop;
        } else {
            dy = mBalloonY - mBalloonRect.height();
        }
        c.translate(mOutlineRect.left - mHorizontalPadding - mBalloonRect.width(),
                dy);
        c.drawPath(mBalloonPath, mBalloonPaint);
        mBalloonTextPaint.getTextBounds(mSection, 0, mSection.length(), mTextBound);
        c.drawText(mSection, mBalloonRect.width() / 2 - mTextBound.width() / 2,
                mBalloonRect.width() / 2 + mTextBound.height() / 2, mBalloonTextPaint);
        c.restore();
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return isPointInsideOutline(e.getX(), e.getY());
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                updateAndShowSection(rv, e.getY());
                break;

            case MotionEvent.ACTION_UP:
                hideSection(rv);
                break;
        }
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    /**
     * check whether the point is inside outline.
     *
     * @param x X axis of a motion event.
     * @param y Y axis of a motion event.
     * @return true, if inside, or in verse.
     */
    private boolean isPointInsideOutline(float x, float y) {
        boolean inside = false;
        if (x >= mOutlineRect.left && x <= mOutlineRect.right
                && y >= mOutlineRect.top && y <= mOutlineRect.bottom) {
            inside = true;
        }
        return inside;
    }

    /**
     * Get section indexer and show it .
     *
     * @param rv RecyclerView used to show section indexer.
     * @param y  axis of a motion event.
     */
    private void updateAndShowSection(RecyclerView rv, float y) {
        // Get index of alphabet
        int index = (int) ((y - mOutlineRect.top - mCharHeight / 2.f) / mCharHeight);
        index = Math.max(Math.min(index, mIndexerString.length() - 1), 0);

        mListener.onSectionSelected(rv, index);

        mBalloonY = (index + 1) * mCharHeight + mCharHeight / 2.f + mOutlineRect.top;

        // Show section indexer
        mSection = String.valueOf(mIndexerString.charAt(index));
        mShowBalloon = true;
        rv.invalidate();
    }

    /**
     * Hide section indexer.
     *
     * @param rv RecyclerView used to hide section indexer.
     */
    private void hideSection(RecyclerView rv) {
        mShowBalloon = false;
        rv.invalidate();
    }

    public interface onSectionSelectedListener {
        void onSectionSelected(RecyclerView rv, int sectionIndex);
    }

}
