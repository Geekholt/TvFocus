package com.geekholt.tvfocusdemo.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;

import com.geekholt.tvfocusdemo.util.Loger;

/**
 * @Author：wuhaoteng
 * @Date:2018/12/23
 * @Desc：
 */
public class TvRecyclerView extends RecyclerView {
    //焦点是否居中
    private boolean mSelectedItemCentered = true;
    private int mSelectedItemOffsetStart = 0;
    private int mSelectedItemOffsetEnd = 0;

    public TvRecyclerView(Context context) {
        super(context);
        init();
    }

    public TvRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TvRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    public void init() {
    }

    @Override
    public View focusSearch(int direction) {
        return super.focusSearch(direction);
    }

    @Override
    public View focusSearch(View focused, int direction) {
        View nextFocus = FocusFinder.getInstance().findNextFocus(this, focused, direction);
        View realNextFocus = super.focusSearch(focused, direction);
        Loger.i("focused = " + (focused != null ? focused.toString() : "null"));
        Loger.i("nextFocus = " + (nextFocus != null ? nextFocus.toString() : "null"));
        Loger.i("realNextFocus = " + (realNextFocus != null ? realNextFocus.toString() : "null"));
        //canScrollVertically(1)  true表示能滚动，false表示已经滚动到底部
        //canScrollVertically(-1) true表示能滚动，false表示已经滚动到顶部
        Loger.i("canScrollVertically(-1) = " + canScrollVertically(-1));
        Loger.i("canScrollVertically(1) = " + canScrollVertically(1));
        Loger.i("canScrollHorizontally(-1) = " + canScrollHorizontally(-1));
        Loger.i("canScrollHorizontally(1) = " + canScrollHorizontally(1));
        switch (direction) {
            case FOCUS_RIGHT:
                //TODO
                break;
            case FOCUS_LEFT:
                //TODO
                break;
            case FOCUS_UP:
                if (nextFocus == null && !canScrollVertically(-1)) {
                    //滑动到顶部
                    return null;
                }

                break;
            case FOCUS_DOWN:
                if (nextFocus == null && !canScrollVertically(1)) {
                    //滑动到底部
                    return null;
                }
                break;
        }
        return realNextFocus;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void requestChildFocus(View child, View focused) {
        if (null != child) {
            if (mSelectedItemCentered) {
                mSelectedItemOffsetStart = !isVertical() ? (getFreeWidth() - child.getWidth()) : (getFreeHeight() - child.getHeight());
                mSelectedItemOffsetStart /= 2;
                mSelectedItemOffsetEnd = mSelectedItemOffsetStart;
            }
        }
        Loger.i("mSelectedItemOffsetStart = " + mSelectedItemOffsetStart);
        Loger.i("mSelectedItemOffsetEnd = " + mSelectedItemOffsetEnd);
        super.requestChildFocus(child, focused);
    }


    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        final int parentLeft = getPaddingLeft();
        final int parentRight = getWidth() - getPaddingRight();

        final int parentTop = getPaddingTop();
        final int parentBottom = getHeight() - getPaddingBottom();

        final int childLeft = child.getLeft() + rect.left;
        final int childTop = child.getTop() + rect.top;

        final int childRight = childLeft + rect.width();
        final int childBottom = childTop + rect.height();

        final int offScreenLeft = Math.min(0, childLeft - parentLeft - mSelectedItemOffsetStart);
        final int offScreenRight = Math.max(0, childRight - parentRight + mSelectedItemOffsetEnd);

        final int offScreenTop = Math.min(0, childTop - parentTop - mSelectedItemOffsetStart);
        final int offScreenBottom = Math.max(0, childBottom - parentBottom + mSelectedItemOffsetEnd);


        final boolean canScrollHorizontal = getLayoutManager().canScrollHorizontally();
        final boolean canScrollVertical = getLayoutManager().canScrollVertically();

        // Favor the "start" layout direction over the end when bringing one side or the other
        // of a large rect into view. If we decide to bring in end because start is already
        // visible, limit the scroll such that start won't go out of bounds.
        final int dx;
        if (canScrollHorizontal) {
            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                dx = offScreenRight != 0 ? offScreenRight
                        : Math.max(offScreenLeft, childRight - parentRight);
            } else {
                dx = offScreenLeft != 0 ? offScreenLeft
                        : Math.min(childLeft - parentLeft, offScreenRight);
            }
        } else {
            dx = 0;
        }

        // Favor bringing the top into view over the bottom. If top is already visible and
        // we should scroll to make bottom visible, make sure top does not go out of bounds.
        final int dy;
        if (canScrollVertical) {
            dy = offScreenTop != 0 ? offScreenTop : Math.min(childTop - parentTop, offScreenBottom);
        } else {
            dy = 0;
        }


        if (dx != 0 || dy != 0) {
            Loger.i("dx = " + dx);
            Loger.i("dy = " + dy);
            if (immediate) {
                scrollBy(dx, dy);
            } else {
                smoothScrollBy(dx, dy);
            }
            // 重绘是为了选中item置顶，具体请参考getChildDrawingOrder方法
            postInvalidate();
            return true;
        }


        return false;
    }

    /**
     * 判断是垂直，还是横向.
     */
    private boolean isVertical() {
        LayoutManager manager = getLayoutManager();
        if (manager != null) {
            LinearLayoutManager layout = (LinearLayoutManager) getLayoutManager();
            return layout.getOrientation() == LinearLayoutManager.VERTICAL;

        }
        return false;
    }

    private int getFreeWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int getFreeHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }


}
