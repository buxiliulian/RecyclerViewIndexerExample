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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.animation.Interpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * This ItemDecoration is used to draw indexer of RecyclerView.
 *
 * @author wei.zhou
 */
public class IndexerDecoration extends RecyclerView.ItemDecoration implements RecyclerView.OnItemTouchListener {
    private static final String TAG = IndexerDecoration.class.getSimpleName();

    private RecyclerView mRecyclerView;
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
    private float mOutlineMinMarginTop;

    private int mRecyclerViewWidth, mRecyclerViewHeight;

    /**
     * 索引的字符。
     */
    private String mSection;

    /**
     * 滑动索引条的监听事件。
     */
    private onScrollListener mListener;

    /**
     * 是否有足够的空间用于绘制索引条。
     */
    private boolean mHasEnoughSpace;


    @IntDef({ANIMATION_STATE_OUT, ANIMATION_STATE_TRANSLATING_IN, ANIMATION_STATE_IN,
            ANIMATION_STATE_TRANSLATING_OUT})
    @Retention(RetentionPolicy.SOURCE)
    private @interface AnimationState {
    }

    /**
     * 位移动画的状态。
     */
    private static final int ANIMATION_STATE_OUT = 0;
    private static final int ANIMATION_STATE_TRANSLATING_IN = 1;
    private static final int ANIMATION_STATE_IN = 2;
    private static final int ANIMATION_STATE_TRANSLATING_OUT = 3;
    @AnimationState
    private int mAnimationState = ANIMATION_STATE_OUT;


    /**
     * 执行位移动画的相关参数
     */
    private ValueAnimator mTranslateAnimator;
    private Interpolator mShowInterpolator = new FastOutLinearInInterpolator();
    private Interpolator mHideInterpolator = new LinearOutSlowInInterpolator();
    private static final int ANIMATION_DURATION_MS = 500;

    /**
     * 用于在索引条显示后，执行隐藏动画的任务。
     */
    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * 在索引条显示后，执行隐藏任务的延迟时间，单位ms。
     */
    private static final int HIDE_DELAY_AFTER_VISIBLE_MS = 1500;

    /**
     * 索引条从屏幕右侧向左移动的最大距离。
     */
    private float mMaxTranslationX;

    /**
     * 索引条在x轴向左偏移的距离。
     */
    private float mTranslationX;


    /**
     * 用Builder参数构造实例
     *
     * @param builder 构建所需的Builder参数
     */
    public IndexerDecoration(Builder builder) {

        // 获取选中索引条滑动监听事件
        mListener = builder.mListener;

        // 获取索引字符串
        mIndexerString = builder.mIndexerString;
        if (mIndexerString == null) {
            Log.w(TAG, "You have not set indexer string.");
            return;
        }

        DisplayMetrics displayMetrics = builder.mContext.getResources().getDisplayMetrics();

        // 获取索引字体大小，并转换为 px
        int sectionSize = builder.mSectionSize;
        if (builder.mSectionSize <= 0) {
            sectionSize = DEFAULT_SECTION_TEXT_SIZE;
        }
        int sectionTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sectionSize,
                displayMetrics);

        // 获取水平间距
        int horizontalPadding = builder.mHorizontalPadding;
        if (horizontalPadding <= 0) {
            horizontalPadding = DEFAULT_HORIZONTAL_PADDING;
        }
        mHorizontalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, horizontalPadding,
                displayMetrics);

        // 初始化字母表的画笔，并设置字体大小
        mAlphabetTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mAlphabetTextPaint.setTextSize(sectionTextSize);

        // 根据字体的大小，获取字体的高(宽高相等)
        Paint.FontMetrics fontMetrics = mAlphabetTextPaint.getFontMetrics();
        float fontMetricsHeight = fontMetrics.bottom - fontMetrics.top;
        mCharWidth = mCharHeight = (int) Math.ceil(fontMetricsHeight);

        // 根据字体的宽高以及索引字符串的长度，获取轮廓的矩形区域
        mOutlineRect = new RectF();
        mOutlineRect.right = mCharWidth;
        mOutlineRect.bottom = mCharHeight * mIndexerString.length() + mCharHeight;

        // 获取轮廓的rect计算path
        mOutlinePath = new Path();
        mOutlinePath.addArc(mOutlineRect.left, mOutlineRect.top, mOutlineRect.width(), mCharHeight, 180, 180);
        mOutlinePath.rLineTo(0, mOutlineRect.height() - mCharHeight);
        mOutlinePath.addArc(0, mOutlineRect.height() - mCharHeight,
                mOutlineRect.width(), mOutlineRect.height(), 0, 180);
        mOutlinePath.lineTo(0, mCharHeight / 2.f);

        // 初始化轮廓的画笔
        mOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setColor(Color.BLACK);
        int outlineStrokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_OUTLINE_STROKE_WIDTH, displayMetrics);
        mOutlinePaint.setStrokeWidth(outlineStrokeWidth);

        // 初始化气泡的画笔
        mBalloonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBalloonPaint.setDither(true);
        mBalloonPaint.setStyle(Paint.Style.FILL);
        int balloonColor = builder.mBalloonColor;
        if (balloonColor <= 0) {
            balloonColor = DEFAULT_BALLOON_COLOR;
        }
        mBalloonPaint.setColor(balloonColor);

        // 初始化气泡中索引的画笔，其中字体大小设置为索引字母表中字体大小的2倍
        mBalloonTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mBalloonTextPaint.setColor(Color.WHITE);
        mBalloonTextPaint.setTextSize(sectionTextSize * 2);

        // 根据气泡中字体的大小，计算气泡的直径
        fontMetrics = mBalloonTextPaint.getFontMetrics();
        float balloonBoundSize = fontMetrics.bottom - fontMetrics.top;
        float diameter = (float) Math.hypot(balloonBoundSize, balloonBoundSize);

        // 根据直径计算气泡的rect区域
        mBalloonRect = new RectF(0, 0, diameter, diameter);
        // 根据气泡的rect区域计算气泡的path
        mBalloonPath = new Path();
        mBalloonPath.addArc(mBalloonRect, 90, 270);
        mBalloonPath.rLineTo(0, mBalloonRect.height() / 2);
        mBalloonPath.rLineTo(-mBalloonRect.width() / 2, 0);

        // 轮廓距离顶部的最小间距(为了满足气泡能在合理的位置绘制)
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
     * 绑定RecyclerView，并为其添加索引条和回调
     *
     * @param recyclerView 被绑定的RecyclerView对象
     */
    public void attachToRecyclerView(RecyclerView recyclerView) {
        if (mRecyclerView == recyclerView) {
            return;
        }

        if (mRecyclerView != null) {
            mRecyclerView.removeItemDecoration(this);
            mRecyclerView.removeOnItemTouchListener(this);
            mRecyclerView.removeOnScrollListener(mOnScrollListener);
        }

        mRecyclerView = recyclerView;

        if (mRecyclerView != null) {
            mRecyclerView.addItemDecoration(this);
            mRecyclerView.addOnItemTouchListener(this);
            mRecyclerView.addOnScrollListener(mOnScrollListener);
        }
    }


    public static class Builder {
        private Context mContext;
        private int mSectionSize; // sp
        private int mBalloonColor;
        private int mHorizontalPadding; //dp
        private String mIndexerString;
        private onScrollListener mListener;

        public Builder(Context context, String indexerString) {
            mContext = context;
            mIndexerString = indexerString;
        }

        public Builder onScrollListener(onScrollListener listener) {
            mListener = listener;
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


    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {

        // 如果果索引字符串为null或者处于动画结束状态就不绘制
        if (mIndexerString == null || mAnimationState == ANIMATION_STATE_OUT) {
            return;
        }

        // if width or height changed , check whether RecyclerView has enough space to draw indexer
        // 如果宽高改变了，检查RecyclerView是否有足够的空间来绘制索引条
        if (mRecyclerViewWidth != parent.getWidth() || mRecyclerViewHeight != parent.getHeight()) {
            mRecyclerViewWidth = parent.getWidth();
            mRecyclerViewHeight = parent.getHeight();

            // 如果没有足够的空间就不绘制索引
            if (mRecyclerViewHeight - mOutlineRect.height() <= mOutlineMinMarginTop) {
                mHasEnoughSpace = false;
                Log.w(TAG, "Couldn't show indexer. RecyclerView must have enough height!!!");
                return;
            } else {
                mHasEnoughSpace = true;
            }
        }

        // 当RecyclerView的宽高没有改变的时候，如果还是没有足够的空间，就不绘制
        if (!mHasEnoughSpace) {
            return;
        } else {
            // 如果有足够的空间就需要调整轮廓的位置
            mOutlineRect.offsetTo(parent.getWidth() - mTranslationX,
                    parent.getHeight() / 2.f - mOutlineRect.height() / 2.f);
        }


        // 1. draw outline and alphabet
        // 绘制轮廓和索引条
        drawOutlineAndAlphabet(c);

        // 2. draw balloon and section
        // 如果需要绘制气泡，并且所以字母不为null，就绘制气泡
        if (mShowBalloon && mSection != null) {
            drawBalloon(c);
        }

    }

    private void drawOutlineAndAlphabet(Canvas c) {
        c.save();
        // Draw outline
        c.translate(mOutlineRect.left, mOutlineRect.top);
        c.drawPath(mOutlinePath, mOutlinePaint);

        // Draw indexer
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
            dy = mOutlineRect.top - mOutlineMinMarginTop;
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
        // 只要手指在轮廓的范围内，我都认为需要截断事件
        return isPointInsideOutline(e.getX(), e.getY());
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        Log.d(TAG, "onTouchEvent");
        mRecyclerView.removeCallbacks(mHideRunnable);
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 如果正在执行隐藏动画，那么就取消当前动画，并执行显示动画
                if (mAnimationState == ANIMATION_STATE_TRANSLATING_OUT) {
                    // TODO: show 完后会添加一个隐藏任务，如果手指不移动，还是会隐藏索引栏
                    show();
                }
            case MotionEvent.ACTION_MOVE:
                updateAndShowSection(rv, e.getY());
                break;

            case MotionEvent.ACTION_UP:
                hideSection(rv);
                // 如果手指离开索引条，就再次加入隐藏任务
                mRecyclerView.postDelayed(mHideRunnable, HIDE_DELAY_AFTER_VISIBLE_MS);
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

        if (mListener != null) {
            mListener.onSectionSelected(rv, index);
        }

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

    public interface onScrollListener {
        void onSectionSelected(RecyclerView rv, int sectionIndex);
    }


    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (Math.abs(dy) > 0) {
                // 只要有滚动就显示索引条
                show();
            }
        }
    };


    private void show() {
        switch (mAnimationState) {
            case ANIMATION_STATE_TRANSLATING_OUT:
                // 如果正在向右位移出屏幕，就取消动画，然后执行下面的显示动画
                Log.d(TAG, "cancel translation out animation, and execute show animation");
                mTranslateAnimator.cancel();
            case ANIMATION_STATE_OUT:
                Log.d(TAG, "start show indexer animation");
                mAnimationState = ANIMATION_STATE_TRANSLATING_IN;
                mTranslateAnimator.setFloatValues((float) mTranslateAnimator.getAnimatedValue(), 1);
                mTranslateAnimator.setInterpolator(mShowInterpolator);
                mTranslateAnimator.start();
                break;
        }
    }


    private void hide() {
        switch (mAnimationState) {
            case ANIMATION_STATE_TRANSLATING_IN:
                // 如果正在进行位移进入动画，就取消当前动画，执行下面的隐藏动画
                // 防止 RecyclerView 大小改变而导致需要隐藏的情况
                mTranslateAnimator.cancel();
            case ANIMATION_STATE_IN:
                mAnimationState = ANIMATION_STATE_TRANSLATING_OUT;
                mTranslateAnimator.setFloatValues((float) mTranslateAnimator.getAnimatedValue(), 0);
                mTranslateAnimator.setInterpolator(mHideInterpolator);
                mTranslateAnimator.start();
                break;
        }
    }


    /**
     * 动画更监听器
     * 执行动画的时候，计算需要位移的值，然后重绘
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
     * 用于监听动画的取消和完成。
     */
    private Animator.AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {
        private boolean mCanceled;

        @Override
        public void onAnimationCancel(Animator animation) {
            mCanceled = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            // 如果取消了，就不需要执行后面的逻辑
            if (mCanceled) {
                mCanceled = false;
                return;
            }

            float animatedValue = (float) mTranslateAnimator.getAnimatedValue();
            if (animatedValue == 0) { // 隐藏完毕
                mAnimationState = ANIMATION_STATE_OUT;
            } else { // 显示完毕
                mAnimationState = ANIMATION_STATE_IN;
                mRecyclerView.postDelayed(mHideRunnable, HIDE_DELAY_AFTER_VISIBLE_MS);
            }
        }
    };


    /**
     * RecyclerView 进行重新绘制
     */
    private void redraw() {
        mRecyclerView.invalidate();
    }
}
