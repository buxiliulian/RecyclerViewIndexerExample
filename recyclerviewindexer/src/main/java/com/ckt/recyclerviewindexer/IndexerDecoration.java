package com.ckt.recyclerviewindexer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.IntDef;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.animation.Interpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Class is used to draw indexer of RecyclerView.
 *
 * @author wei.zhou
 */
public class IndexerDecoration extends RecyclerView.ItemDecoration {
    private static final String TAG = IndexerDecoration.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private int mRecyclerViewWidth, mRecyclerViewHeight;

    /**
     * Indexer string, for example, alphabet.
     */
    private String mIndexerString;

    /**
     * Default text size of indexer character.
     */
    public static final int DEFAULT_INDEXER_TEXT_SIZE_SP = 14;

    /**
     * Paint used to draw indexer character.
     */
    private TextPaint mIndexerTextPaint;

    /**
     * Height and width of character in indexer string.
     */
    private int mCharHeight, mCharWidth;


    /**
     * Paint used to draw outline.
     */
    private Paint mOutlinePaint;
    /**
     * Outline's rect.
     */
    private RectF mOutlineRect;
    /**
     * Outline's path.
     */
    private Path mOutlinePath;

    /**
     * Outline's default stroke width.
     */
    private static final int DEFAULT_OUTLINE_STROKE_WIDTH_DP = 1;

    /**
     * Outline's default horizontal padding.
     */
    public static final int DEFAULT_OUTLINE_HORIZONTAL_PADDING_DP = 5;
    /**
     * Outline's horizontal padding.
     */
    private int mHorizontalPadding;

    /**
     * Outline's minimum margin top.
     */
    private float mOutlineMinMarginTop;

    /**
     * Paint used to draw balloon.
     */
    private Paint mBalloonPaint;
    /**
     * Balloon's rect.
     */
    private RectF mBalloonRect;
    /**
     * Balloon's path.
     */
    private Path mBalloonPath;
    /**
     * Paint used to draw text in balloon.
     */
    private TextPaint mBalloonTextPaint;
    /**
     * Decide whether to show balloon.
     */
    private boolean mShowBalloon;
    /**
     * Balloon's coordinate of X axis.
     */
    private float mBalloonY;
    /**
     * Balloon's default background color.
     */
    public static final int DEFAULT_BALLOON_BG_COLOR = 0xee3F51B5;


    /**
     * Rect used to measure text bound.
     */
    private Rect mTextBound;


    /**
     * String of the section within indexer string.
     */
    private String mSection;

    /**
     * Scroll listener.
     */
    private onScrollListener mListener;

    /**
     * Indicate whether RecyclerView has enough space to draw indexer.
     */
    private boolean mHasEnoughSpace;


    /**
     * Annotation of animation state.
     */
    @IntDef({ANIMATION_STATE_OUT, ANIMATION_STATE_TRANSLATING_IN, ANIMATION_STATE_IN,
            ANIMATION_STATE_TRANSLATING_OUT})
    @Retention(RetentionPolicy.SOURCE)
    private @interface AnimationState {
    }

    /**
     * Animation state.
     */
    private static final int ANIMATION_STATE_OUT = 0;
    private static final int ANIMATION_STATE_TRANSLATING_IN = 1;
    private static final int ANIMATION_STATE_IN = 2;
    private static final int ANIMATION_STATE_TRANSLATING_OUT = 3;
    @AnimationState
    private int mAnimationState = ANIMATION_STATE_OUT;


    /**
     * Translate animator.
     */
    private ValueAnimator mTranslateAnimator;
    /**
     * Interpolator used for translate in animation.
     */
    private Interpolator mInInterpolator = new FastOutLinearInInterpolator();
    /**
     * Interpolator used for translate out animation.
     */
    private Interpolator mOutInterpolator = new LinearOutSlowInInterpolator();
    /**
     * Duration of translate in and out .
     */
    private static final int ANIMATION_DURATION_MS = 500;

    /**
     * Delay time used to execute translate out animation after indexer is visible.
     */
    private static final int TRANSLATE_OUT_DELAY_AFTER_VISIBLE_MS = 1500;

    /**
     * Runnable used to execute translate out animation.
     */
    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            translateOut();
        }
    };

    /**
     * The maximum translation x .
     */
    private float mMaxTranslationX;

    /**
     * The translation x from end to start.
     */
    private float mTranslationX;


    /**
     * Indicate indexer is dragging.
     */
    private boolean mIsDragging;


    public IndexerDecoration(Builder builder) {
        mIndexerString = builder.mIndexerString;
        if (TextUtils.isEmpty(mIndexerString)) {
            Log.w(TAG, "You have not set indexer string.");
            return;
        }

        DisplayMetrics displayMetrics = builder.mContext.getResources().getDisplayMetrics();

        int indexerTextSize = builder.mIndexerTextSize <= DEFAULT_INDEXER_TEXT_SIZE_SP ?
                DEFAULT_INDEXER_TEXT_SIZE_SP : builder.mIndexerTextSize;
        indexerTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, indexerTextSize,
                displayMetrics);

        int horizontalPadding = builder.mHorizontalPadding <= 0 ?
                DEFAULT_OUTLINE_HORIZONTAL_PADDING_DP : builder.mHorizontalPadding;
        mHorizontalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, horizontalPadding,
                displayMetrics);

        mIndexerTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mIndexerTextPaint.setTextSize(indexerTextSize);

        Paint.FontMetrics fontMetrics = mIndexerTextPaint.getFontMetrics();
        float fontMetricsHeight = fontMetrics.bottom - fontMetrics.top;
        mCharWidth = mCharHeight = (int) Math.ceil(fontMetricsHeight);

        mOutlineRect = new RectF();
        mOutlineRect.right = mCharWidth;
        mOutlineRect.bottom = mCharHeight * mIndexerString.length() + mCharHeight;

        mOutlinePath = new Path();
        mOutlinePath.addArc(mOutlineRect.left, mOutlineRect.top, mOutlineRect.width(), mCharHeight,
                180, 180);
        mOutlinePath.rLineTo(0, mOutlineRect.height() - mCharHeight);
        mOutlinePath.addArc(mOutlineRect.left, mOutlineRect.height() - mCharHeight,
                mOutlineRect.width(), mOutlineRect.height(), 0, 180);
        mOutlinePath.lineTo(0, mCharHeight / 2.f);

        mOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setColor(Color.BLACK);
        int outlineStrokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_OUTLINE_STROKE_WIDTH_DP, displayMetrics);
        mOutlinePaint.setStrokeWidth(outlineStrokeWidth);

        mBalloonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBalloonPaint.setStyle(Paint.Style.FILL);
        int balloonColor = builder.mBalloonColor <= 0 ?
                DEFAULT_BALLOON_BG_COLOR : builder.mBalloonColor;
        mBalloonPaint.setColor(balloonColor);

        mBalloonTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mBalloonTextPaint.setColor(Color.WHITE);
        mBalloonTextPaint.setTextSize(indexerTextSize * 2);

        fontMetrics = mBalloonTextPaint.getFontMetrics();
        float balloonBoundSize = fontMetrics.bottom - fontMetrics.top;
        float diameter = (float) Math.hypot(balloonBoundSize, balloonBoundSize);

        mBalloonRect = new RectF(0, 0, diameter, diameter);

        mBalloonPath = new Path();
        mBalloonPath.addArc(mBalloonRect, 90, 270);
        mBalloonPath.rLineTo(0, mBalloonRect.height() / 2);
        mBalloonPath.rLineTo(-mBalloonRect.width() / 2, 0);

        mOutlineMinMarginTop = diameter - mCharHeight * 3.f / 2;
        if (mOutlineMinMarginTop < 0) {
            mOutlineMinMarginTop = 0;
        }


        mTextBound = new Rect();

        mMaxTranslationX = mOutlineRect.width() + mHorizontalPadding;

        mTranslateAnimator = ValueAnimator.ofFloat(0, 1);
        mTranslateAnimator.setDuration(ANIMATION_DURATION_MS);
        mTranslateAnimator.addUpdateListener(mUpdateListener);
        mTranslateAnimator.addListener(mAnimatorListener);
    }


    /**
     * Bind to RecyclerView and add scroll listener.
     *
     * @param recyclerView RecyclerView used to be bound.
     * @param listener     scroll listener.
     */
    public void attachToRecyclerView(RecyclerView recyclerView, onScrollListener listener) {
        mListener = listener;

        if (mRecyclerView == recyclerView) {
            return;
        }

        if (mRecyclerView != null) {
            mRecyclerView.removeItemDecoration(this);
            mRecyclerView.removeOnItemTouchListener(mItemTouchListener);
            mRecyclerView.removeOnScrollListener(mOnScrollListener);
        }

        mRecyclerView = recyclerView;

        if (mRecyclerView != null) {
            mRecyclerView.addItemDecoration(this);
            mRecyclerView.addOnItemTouchListener(mItemTouchListener);
            mRecyclerView.addOnScrollListener(mOnScrollListener);
        }
    }


    public static class Builder {
        private Context mContext;
        private int mIndexerTextSize; // sp
        private int mBalloonColor;
        private int mHorizontalPadding; //dp
        private String mIndexerString;

        public Builder(Context context, String indexerString) {
            mContext = context;
            mIndexerString = indexerString;
        }

        public Builder indexerTextSize(int spSize) {
            mIndexerTextSize = spSize;
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


    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {

        // Check indexer string and animation state.
        if (TextUtils.isEmpty(mIndexerString) || mAnimationState == ANIMATION_STATE_OUT) {
            return;
        }

        // If width or height changed , check whether RecyclerView has enough space to draw indexer.
        if (mRecyclerViewWidth != parent.getWidth() || mRecyclerViewHeight != parent.getHeight()) {
            mRecyclerViewWidth = parent.getWidth();
            mRecyclerViewHeight = parent.getHeight();

            if (mRecyclerViewHeight - mOutlineRect.height() <= mOutlineMinMarginTop) {
                Log.w(TAG, "Couldn't show indexer. RecyclerView must have enough height!!!");
                mHasEnoughSpace = false;
            } else {
                mHasEnoughSpace = true;
            }
        }

        if (!mHasEnoughSpace || mTranslationX == 0) {
            return;
        }

        // Adjust outline's rect according to mTranslationX.
        mOutlineRect.offsetTo(parent.getWidth() - mTranslationX,
                parent.getHeight() / 2.f - mOutlineRect.height() / 2.f);

        drawOutlineAndAlphabet(c);

        if (mShowBalloon && mSection != null) {
            drawBalloon(c);
        }

    }

    /**
     * Draw outline and indexer string.
     *
     * @param c canvas used to draw.
     */
    private void drawOutlineAndAlphabet(Canvas c) {
        c.save();
        // 1. Draw outline.
        c.translate(mOutlineRect.left, mOutlineRect.top);
        c.drawPath(mOutlinePath, mOutlinePaint);

        // 2. Draw indexer.
        for (int i = 0; i < mIndexerString.length(); i++) {
            String character = String.valueOf(mIndexerString.charAt(i));
            mIndexerTextPaint.getTextBounds(character, 0, character.length(), mTextBound);
            float left = mCharWidth / 2.f - mTextBound.width() / 2.f;
            float top = mCharHeight * (i + 1) + mTextBound.height() / 2.f;
            c.drawText(character, left, top, mIndexerTextPaint);
        }

        c.restore();
    }

    /**
     * Draw balloon.
     *
     * @param c canvas used to draw.
     */
    private void drawBalloon(Canvas c) {
        c.save();

        float dy;
        if (mBalloonY == 0) {
            dy = mOutlineRect.top - mOutlineMinMarginTop;
        } else {
            dy = mBalloonY - mBalloonRect.height();
        }
        c.translate(mOutlineRect.left - mHorizontalPadding - mBalloonRect.width(),
                dy);
        c.drawPath(mBalloonPath, mBalloonPaint);
        mBalloonTextPaint.getTextBounds(mSection, 0, mSection.length(), mTextBound);
        c.drawText(mSection, mBalloonRect.width() / 2.f - mTextBound.width() / 2.f,
                mBalloonRect.width() / 2.f + mTextBound.height() / 2.f, mBalloonTextPaint);

        c.restore();
    }

    private RecyclerView.SimpleOnItemTouchListener mItemTouchListener = new RecyclerView.SimpleOnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            boolean handled = false;
            // Intercept it as long as pointer is in outline rect.
            if (isPointInsideOutline(e.getX(), e.getY())) {
                Log.i(TAG, "Pointer is in outline, so intercept it.");
                mIsDragging = true;
                if (mAnimationState == ANIMATION_STATE_IN) {
                    cancelHideRunnable();
                } else if (mAnimationState == ANIMATION_STATE_TRANSLATING_OUT) {
                    translateIn();
                }
                updateAndShowBalloon(e.getY());
                handled = true;
            }
            return handled;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    updateAndShowBalloon(e.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    mIsDragging = false;
                    hideBalloon();
                    postHideRunnableDelayed(TRANSLATE_OUT_DELAY_AFTER_VISIBLE_MS);
                    break;
            }
        }
    };


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
     * Get string of section within indexer string and show it .
     *
     * @param y axis of a motion event.
     */
    private void updateAndShowBalloon(float y) {
        // Get index of section within indexer string.
        int index = (int) ((y - mOutlineRect.top - mCharHeight / 2.f) / mCharHeight);
        index = Math.max(Math.min(index, mIndexerString.length() - 1), 0);

        // Invoke callback.
        if (mListener != null) {
            mListener.onScrolled(mRecyclerView, index);
        }

        mBalloonY = (index + 1) * mCharHeight + mCharHeight / 2.f + mOutlineRect.top;

        mSection = String.valueOf(mIndexerString.charAt(index));
        mShowBalloon = true;
        redraw();
    }

    /**
     * Hide balloon.
     */
    private void hideBalloon() {
        mShowBalloon = false;
        redraw();
    }

    public interface onScrollListener {
        /**
         * Callback method to be invoked when indexer is touched.
         *
         * @param sectionIndex index of section within indexer string.
         */
        void onScrolled(RecyclerView rv, int sectionIndex);
    }


    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (Math.abs(dy) > 0) {
                translateIn();
            }
        }
    };



    /**
     * Translate indexer in from right to left.
     */
    private void translateIn() {
        switch (mAnimationState) {
            case ANIMATION_STATE_TRANSLATING_OUT:
                // If animation is translating out, cancel it and execute translate in animation.
                mTranslateAnimator.cancel(); // fall through
            case ANIMATION_STATE_OUT:
                mAnimationState = ANIMATION_STATE_TRANSLATING_IN;
                mTranslateAnimator.setFloatValues((float) mTranslateAnimator.getAnimatedValue(), 1);
                mTranslateAnimator.setInterpolator(mInInterpolator);
                mTranslateAnimator.start();
                break;
        }
    }


    /**
     * Translate indexer out from left to right.
     */
    private void translateOut() {
        switch (mAnimationState) {
            case ANIMATION_STATE_TRANSLATING_IN:
                // If animation is translating in, cancel it and execute translate out animation.
                mTranslateAnimator.cancel();// fall through
            case ANIMATION_STATE_IN:
                mAnimationState = ANIMATION_STATE_TRANSLATING_OUT;
                mTranslateAnimator.setFloatValues((float) mTranslateAnimator.getAnimatedValue(), 0);
                mTranslateAnimator.setInterpolator(mOutInterpolator);
                mTranslateAnimator.start();
                break;
        }
    }


    /**
     * Animator update listener used to update variable mTranslationX and request RecyclerView
     * to redraw.
     */
    private ValueAnimator.AnimatorUpdateListener mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float animatedValue = (float) animation.getAnimatedValue();
            mTranslationX = animatedValue * mMaxTranslationX;
            redraw();
        }
    };


    /**
     * Animator listener used to listen for cancel event and end event.
     */
    private Animator.AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {
        private boolean mCanceled;

        @Override
        public void onAnimationCancel(Animator animation) {
            mCanceled = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            // If canceled, do nothing.
            if (mCanceled) {
                mCanceled = false;
                return;
            }

            float animatedValue = (float) mTranslateAnimator.getAnimatedValue();
            if (animatedValue == 0) { // translate out complete.
                mAnimationState = ANIMATION_STATE_OUT;
            } else { // translate in complete.
                mAnimationState = ANIMATION_STATE_IN;
                // If is not dragging, post a hide runnable within RecyclerView.
                if (!mIsDragging) {
                    Log.d("david", "complete");
                    postHideRunnableDelayed(TRANSLATE_OUT_DELAY_AFTER_VISIBLE_MS);
                }
            }
        }
    };


    /**
     * Request RecyclerView to redraw.
     */
    private void redraw() {
        mRecyclerView.invalidate();
    }


    /**
     * post a delay hide runnable after indexer is visible.
     *
     * @param delay delay time.
     */
    private void postHideRunnableDelayed(int delay) {
        mRecyclerView.postDelayed(mHideRunnable, delay);
    }


    /**
     * Cancel hide runnable.
     */
    private void cancelHideRunnable() {
        mRecyclerView.removeCallbacks(mHideRunnable);
    }
}
