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
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Class is used to draw indexer of RecyclerView.
 *
 * @author wei.zhou
 */
public abstract class SimpleIndexer extends RecyclerView.ItemDecoration {
    private static final String TAG = SimpleIndexer.class.getSimpleName();
    private int mScaledTouchSlop;

    private RecyclerView mRecyclerView;
    protected int mRecyclerViewWidth, mRecyclerViewHeight;

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
    protected int mCellHeight, mCellWidth;


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
    public static final int DEFAULT_PADDING_DP = 5;

    protected int mPadding;

    /**
     * Decide whether to show indicator.
     */
    private boolean mShowIndicator;
    /**
     * Indicator's base y.
     */
    private float mIndexerBaseY;
    /**
     * Balloon's default background color.
     */
    public static final int DEFAULT_INDICATOR_BG_COLOR = 0xee3F51B5;


    /**
     * Rect used to measure text bound.
     */
    protected Rect mTmpTextBound;


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
    private RectF mOuter;
    protected int mIndexerTextSize;
    protected int mIndicatorBgColor;


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


    public SimpleIndexer(Builder builder) {
        mIndexerString = builder.mIndexerString;
        if (TextUtils.isEmpty(mIndexerString)) {
            Log.w(TAG, "You have not set indexer string.");
            return;
        }

        DisplayMetrics displayMetrics = builder.mContext.getResources().getDisplayMetrics();
        ViewConfiguration viewConfiguration = ViewConfiguration.get(builder.mContext);
        mScaledTouchSlop = viewConfiguration.getScaledTouchSlop();

        mIndexerTextSize = builder.mIndexerTextSize <= DEFAULT_INDEXER_TEXT_SIZE_SP ?
                DEFAULT_INDEXER_TEXT_SIZE_SP : builder.mIndexerTextSize;
        mIndexerTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mIndexerTextSize,
                displayMetrics);

        int padding = builder.mPadding <= 0 ?
                DEFAULT_PADDING_DP : builder.mPadding;
        mPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, padding,
                displayMetrics);

        mIndexerTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mIndexerTextPaint.setTextSize(mIndexerTextSize);

        Paint.FontMetrics fontMetrics = mIndexerTextPaint.getFontMetrics();
        float fontMetricsHeight = fontMetrics.bottom - fontMetrics.top;
        mCellWidth = mCellHeight = (int) Math.ceil(fontMetricsHeight);

        mOutlineRect = new RectF();
        mOutlineRect.right = mCellWidth;
        mOutlineRect.bottom = mCellHeight / 2.f + mCellHeight * mIndexerString.length() + mCellHeight / 2.f;

        mOutlinePath = new Path();
        mOutlinePath.addArc(mOutlineRect.left, mOutlineRect.top, mOutlineRect.width(), mCellHeight,
                180, 180);
        mOutlinePath.rLineTo(0, mOutlineRect.height() - mCellHeight);
        mOutlinePath.addArc(mOutlineRect.left, mOutlineRect.height() - mCellHeight,
                mOutlineRect.width(), mOutlineRect.height(), 0, 180);
        mOutlinePath.lineTo(0, mCellHeight / 2.f);

        mOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setColor(Color.BLACK);
        int outlineStrokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_OUTLINE_STROKE_WIDTH_DP, displayMetrics);
        mOutlinePaint.setStrokeWidth(outlineStrokeWidth);

        mOuter = new RectF();
        offsetOuter();

        mIndicatorBgColor = builder.mIndicatorColor <= 0 ?
                DEFAULT_INDICATOR_BG_COLOR : builder.mIndicatorColor;

        mTmpTextBound = new Rect();

        mMaxTranslationX = mOutlineRect.width() + mPadding;

        mTranslateAnimator = ValueAnimator.ofFloat(0, 1);
        mTranslateAnimator.setDuration(ANIMATION_DURATION_MS);
        mTranslateAnimator.addUpdateListener(mUpdateListener);
        mTranslateAnimator.addListener(mAnimatorListener);
    }

    private void offsetOuter() {
        mOuter.left = mOutlineRect.left - mPadding;
        mOuter.top = mOutlineRect.top - mPadding;
        mOuter.right = mOutlineRect.right + mPadding;
        mOuter.bottom = mOutlineRect.bottom + mPadding;
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
        Context mContext;
        int mIndexerTextSize; // sp
        int mIndicatorColor;
        int mPadding; //dp
        String mIndexerString;

        public Builder(Context context, String indexerString) {
            mContext = context;
            mIndexerString = indexerString;
        }

        public Builder indexerTextSize(int spSize) {
            mIndexerTextSize = spSize;
            return this;
        }

        public Builder indicatorColor(int color) {
            mIndicatorColor = color;
            return this;
        }

        public Builder padding(int dpPadding) {
            mPadding = dpPadding;
            return this;
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

            if ((mRecyclerViewHeight - mOutlineRect.height()) / 2.f < mPadding) {
                Log.w(TAG, "Couldn't show indexer. RecyclerView must have enough height!!!");
                mHasEnoughSpace = false;
            } else {
                mHasEnoughSpace = true;
            }
        }

        if (!mHasEnoughSpace || mTranslationX == 0) {
            return;
        }

        // If translate, adjust outline and outer's rect.
        mOutlineRect.offsetTo(parent.getWidth() - mTranslationX,
                parent.getHeight() / 2.f - mOutlineRect.height() / 2.f);
        offsetOuter();

        drawOutlineAndIndexer(c);

        if (mShowIndicator && mSection != null) {
            drawIndicator(c, mOuter, mIndexerBaseY, mSection);
        }
    }

    /**
     * Draw outline and indexer string.
     *
     * @param c canvas used to draw.
     */
    private void drawOutlineAndIndexer(Canvas c) {
        c.save();
        // 1. Draw outline.
        c.translate(mOutlineRect.left, mOutlineRect.top);
        c.drawPath(mOutlinePath, mOutlinePaint);

        // 2. Draw indexer.
        for (int i = 0; i < mIndexerString.length(); i++) {
            String character = String.valueOf(mIndexerString.charAt(i));
            mIndexerTextPaint.getTextBounds(character, 0, character.length(), mTmpTextBound);
            float left = mCellWidth / 2.f - mTmpTextBound.width() / 2.f;
            float top = mCellHeight * (i + 1) + mTmpTextBound.height() / 2.f;
            c.drawText(character, left, top, mIndexerTextPaint);
        }

        c.restore();
    }


    public abstract void drawIndicator(Canvas c, RectF outer, float indicatorBaseY, String indicatorChar);

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
                updateIndicatorState(e.getY());
                handled = true;
            }
            return handled;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    updateIndicatorState(e.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    mIsDragging = false;
                    setIndicatorState(false);
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

    private void updateIndicatorState(float y) {
        int index = (int) ((y - mOutlineRect.top - mCellHeight / 2.f) / mCellHeight);
        index = Math.max(Math.min(index, mIndexerString.length() - 1), 0);

        // Callback.
        if (mListener != null) {
            mListener.onScrolled(mRecyclerView, index);
        }

        mIndexerBaseY = (index + 1) * mCellHeight + mCellHeight / 2.f + mOutlineRect.top;

        mSection = String.valueOf(mIndexerString.charAt(index));

        setIndicatorState(true);
    }

    private void setIndicatorState(boolean show) {
        mShowIndicator = show;
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
            if (Math.abs(dy) >= mScaledTouchSlop) {
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
