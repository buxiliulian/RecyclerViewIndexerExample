package com.ckt.recyclerviewindexerexample;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;

/**
 * This class is used to draw item's sticky header and divider. But now, it only support
 * VERTICAL orientation. If you want to do another, you can extends this class,
 * and override drawHorizontalDivider(Canvas, RecyclerView) and drawHorizontalHeader(Canvas, RecyclerView).
 *
 * @author David Chow
 */
public class StickyHeaderDecoration extends RecyclerView.ItemDecoration {
    private static final String TAG = "StickyHeaderDecoration";

    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL = LinearLayout.VERTICAL;
    private int mOrientation;

    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};
    private Drawable mDivider;

    // header height, it't up to header text size
    private int mHeaderHeight;
    // header text size. (small: 14sp, medium: 18sp, Large:22sp)
    private static final int DEFAULT_HEADER_TEXT_SIZE = 14; // sp
    // header background paint
    private Paint mHeaderBgPaint;
    // header text paint
    private TextPaint mHeaderTextPaint;


    public StickyHeaderDecoration(Context context, int orientation) {
        initDividerDecoration(context, orientation);
        initHeaderDecoration(context);
    }

    private void initDividerDecoration(Context context, int orientation) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        if (mDivider == null) {
            Log.w(TAG, "@android:attr/listDivider was not set in the theme used for this "
                    + "DividerItemDecoration. Please set that attribute all call setDrawable()");
        }
        a.recycle();
        setOrientation(orientation);
    }

    /**
     * Sets the orientation for this divider. This should be called if
     * RecyclerView.LayoutManager changes orientation.
     *
     * @param orientation #HORIZONTAL or #VERTICAL
     */
    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw new IllegalArgumentException(
                    "Invalid orientation. It should be either HORIZONTAL or VERTICAL");
        }
        mOrientation = orientation;
    }

    /**
     * Sets the Drawable for this divider.
     *
     * @param drawable Drawable that should be used as a divider.
     */
    public void setDrawable(@NonNull Drawable drawable) {
        if (drawable == null) {
            throw new IllegalArgumentException("Drawable cannot be null.");
        }
        mDivider = drawable;
    }


    private void initHeaderDecoration(Context context) {
        mHeaderBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHeaderBgPaint.setColor(Color.LTGRAY);

        mHeaderTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mHeaderTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_HEADER_TEXT_SIZE,
                context.getResources().getDisplayMetrics()));
        Paint.FontMetrics fontMetrics = mHeaderTextPaint.getFontMetrics();
        mHeaderHeight = Math.round(fontMetrics.descent - fontMetrics.ascent);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        getDividerItemOffsets(outRect, view, parent, state);
        getIndexerItemOffsets(outRect, view, parent, state);
    }

    private void getDividerItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (mDivider == null) {
            outRect.set(0, 0, 0, 0);
            return;
        }
        if (mOrientation == VERTICAL) {
            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
        } else {
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
        }
    }

    private void getIndexerItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (!(layoutManager instanceof LinearLayoutManager)) {
            throw new IllegalStateException("HeaderItemDecoration only support LinearLayoutManager");
        }
        int currentPos = parent.getChildAdapterPosition(view);
        if (hasHeader(parent, currentPos)) {
            outRect.top += mHeaderHeight;
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == VERTICAL) {
            drawVerticalHeader(c, parent);
            drawVerticalDivider(c, parent);
        } else {
            drawHorizontalHeader(c, parent);
            drawHorizontalDivider(c, parent);
        }
    }

    private void drawVerticalHeader(Canvas c, RecyclerView parent) {
        c.save();

        final int left, right;
        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
        } else {
            left = 0;
            right = parent.getWidth();
        }

        Rect childBound = new Rect();
        Rect textBound = new Rect();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            parent.getDecoratedBoundsWithMargins(child, childBound);
            int top = childBound.top;
            int currentPos = parent.getChildAdapterPosition(child);
            if (hasHeader(parent, currentPos)) {
                int bottom = top + mHeaderHeight;
                // draw header's background
                c.drawRect(left, top, right, bottom, mHeaderBgPaint);
                // draw header text
                // 1. get section character
                String section = getSectionCharacter(parent, currentPos);
                // 2. get text bound
                mHeaderTextPaint.getTextBounds(section, 0, section.length(), textBound);
                // 3. draw text
                c.drawText(section,
                        child.getLeft() + child.getPaddingLeft(),
                        (top + bottom) / 2 + textBound.height() / 2,
                        mHeaderTextPaint);
            }
        }

        c.restore();
    }

    protected void drawHorizontalHeader(Canvas canvas, RecyclerView parent) {

    }

    private void drawVerticalDivider(Canvas canvas, RecyclerView parent) {
        canvas.save();
        Rect childBound = new Rect();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);
            if (position == RecyclerView.NO_POSITION) {
                continue;
            }
            String childSection = getSectionCharacter(parent, position);
            if (position + 1 == parent.getAdapter().getItemCount()) {
                continue;
            }
            String nextChildSection = getSectionCharacter(parent, position + 1);
            if (!childSection.equals(nextChildSection)) {
                continue;
            }
            parent.getDecoratedBoundsWithMargins(child, childBound);
            final int bottom = childBound.bottom + Math.round(child.getTranslationY());
            final int top = bottom - mDivider.getIntrinsicHeight();
            mDivider.setBounds(child.getLeft() + child.getPaddingLeft(),
                    top,
                    child.getRight() - child.getPaddingRight(),
                    bottom);
            mDivider.draw(canvas);
        }
        canvas.restore();
    }

    protected void drawHorizontalDivider(Canvas c, RecyclerView parent) {

    }

    /**
     * get section character for position
     *
     * @param parent   RecyclerView
     * @param position the position within the adapter for which to return the
     *                 corresponding section index
     * @return section character
     */
    private String getSectionCharacter(RecyclerView parent, int position) {
        String sectionChar = null;
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter instanceof SectionIndexer) {
            SectionIndexer sectionIndexer = (SectionIndexer) adapter;
            int section = sectionIndexer.getSectionForPosition(position);
            sectionChar = (String) sectionIndexer.getSections()[section];
        }
        return sectionChar;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        c.save();

        final int left, top, right;
        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            top = parent.getPaddingTop();
            right = parent.getWidth() - parent.getPaddingRight();
        } else {
            left = 0;
            top = 0;
            right = parent.getWidth();
        }

        LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
        int firstVisiblePos = layoutManager.findFirstVisibleItemPosition();
        if (firstVisiblePos == RecyclerView.NO_POSITION) {
            return;
        }
        View child = layoutManager.findViewByPosition(firstVisiblePos);
        Rect childBound = new Rect();
        parent.getDecoratedBoundsWithMargins(child, childBound);

        // decide whether to translate canvas
        String firstVisibleSection = getSectionCharacter(parent, firstVisiblePos);
        String nextVisibleSection = null;
        if (firstVisiblePos != parent.getAdapter().getItemCount() - 1) {
            nextVisibleSection = getSectionCharacter(parent, firstVisiblePos + 1);
        }
        if (!firstVisibleSection.equals(nextVisibleSection)) {
            if (childBound.bottom <= mHeaderHeight) {
                c.translate(0, childBound.bottom - mHeaderHeight);
            }
        }
        // draw header's background
        c.drawRect(left, top, right, mHeaderHeight, mHeaderBgPaint);
        // draw header text
        String section = getSectionCharacter(parent, firstVisiblePos);
        Rect textBound = new Rect();
        mHeaderTextPaint.getTextBounds(section, 0, section.length(), textBound);
        c.drawText(section,
                child.getLeft() + child.getPaddingLeft(),
                mHeaderHeight / 2 + textBound.height() / 2,
                mHeaderTextPaint);

        c.restore();
    }

    /**
     * decide whether current position item has a header
     *
     * @param parent          RecyclerView
     * @param currentPosition position of item
     * @return true, has header. false, no header.
     */
    private boolean hasHeader(RecyclerView parent, int currentPosition) {
        boolean hasHeader = false;
        if (currentPosition == 0) {
            hasHeader = true;
        } else if (currentPosition > 0) {
            RecyclerView.Adapter adapter = parent.getAdapter();
            if (adapter instanceof SectionIndexer) {
                SectionIndexer sectionIndexer = (SectionIndexer) adapter;
                int currentSection = sectionIndexer.getSectionForPosition(currentPosition);
                int preSection = sectionIndexer.getSectionForPosition(currentPosition - 1);
                if (currentSection != preSection) {
                    hasHeader = true;
                }
            }
        }
        return hasHeader;
    }
}
