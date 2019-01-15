package com.geekholt.tvfocusdemo.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.View;
import android.view.ViewGroup;

import com.geekholt.tvfocusdemo.util.Loger;

import java.util.ArrayList;

/**
 * @Author：wuhaoteng
 * @Date:2018/12/23
 * @Desc：支持 1.选中项居中 2.焦点记忆 3.可以控制是否横竖向移除
 */
public class TvRecyclerView extends RecyclerView {
    //焦点是否居中
    private boolean mSelectedItemCentered = true;

    //是否可以纵向移出
    private boolean mCanFocusOutVertical = false;
    //是否可以横向移出
    private boolean mCanFocusOutHorizontal = true;
    //焦点移出recyclerview的事件监听
    private FocusLostListener mFocusLostListener;
    //焦点移入recyclerview的事件监听
    private FocusGainListener mFocusGainListener;
    //最后一次聚焦的位置
    private int mLastFocusPosition = 0;
    private View mLastFocusView = null;


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
        setItemAnimator(null);
        /**
         * 该属性是当一个为view获取焦点时，定义viewGroup和其子控件两者之间的关系。
         *
         * 属性的值有三种：
         * beforeDescendants：viewgroup会优先其子类控件而获取到焦点
         * afterDescendants：viewgroup只有当其子类控件不需要获取焦点时才获取焦点
         * blocksDescendants：viewgroup会覆盖子类控件而直接获得焦点
         * */
        setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        this.setFocusable(true);
    }

    @Override
    public View focusSearch(View focused, int direction) {
        View realNextFocus = super.focusSearch(focused, direction);
        View nextFocus = FocusFinder.getInstance().findNextFocus(this, focused, direction);
        Loger.i("focused = " + focused);
        Loger.i("nextFocus = " + nextFocus);
        Loger.i("realNextFocus = " + realNextFocus);
        //canScrollVertically(1)  true表示能滚动，false表示已经滚动到底部
        //canScrollVertically(-1) true表示能滚动，false表示已经滚动到顶部
        Loger.i("canScrollVertically(-1) = " + canScrollVertically(-1));
        Loger.i("canScrollVertically(1) = " + canScrollVertically(1));
        Loger.i("canScrollHorizontally(-1) = " + canScrollHorizontally(-1));
        Loger.i("canScrollHorizontally(1) = " + canScrollHorizontally(1));
        switch (direction) {
            case FOCUS_RIGHT:
                //调用移出的监听
                if (nextFocus == null) {
                    if (mCanFocusOutHorizontal) {
                        if (mFocusLostListener != null) {
                            mFocusLostListener.onFocusLost(focused, direction);
                        }
                        return realNextFocus;
                    } else {
                        return null;
                    }
                }
                break;
            case FOCUS_LEFT:
                //调用移出的监听
                if (nextFocus == null) {
                    if (mCanFocusOutHorizontal) {
                        if (mFocusLostListener != null) {
                            mFocusLostListener.onFocusLost(focused, direction);
                        }
                        return realNextFocus;
                    } else {
                        return null;
                    }
                }
                break;
            case FOCUS_UP:
                if (nextFocus == null && !canScrollVertically(-1)) {
                    //滑动到顶部
                    if (mCanFocusOutVertical) {
                        return realNextFocus;
                    } else {
                        return null;
                    }
                }
                break;
            case FOCUS_DOWN:
                if (nextFocus == null && !canScrollVertically(1)) {
                    //滑动到底部
                    if (mCanFocusOutVertical) {
                        return realNextFocus;
                    } else {
                        return null;
                    }
                }
                break;
        }
        return realNextFocus;
    }

    /**
     * 通过ViewParent#requestChildFocus通知父控件即将获取焦点
     *
     * @param child   下一个要获得焦点的recyclerview item
     * @param focused 当前聚焦的view
     */
    @Override
    public void requestChildFocus(View child, View focused) {
        if (null != child) {
            Loger.i("nextchild = " + child + ",focused = " + focused);
            if (!hasFocus()) {
                //recyclerview 子view 重新获取焦点，调用移入焦点的事件监听
                if (mFocusGainListener != null) {
                    mFocusGainListener.onFocusGain(child, focused);
                }
            }

            //执行过super.requestChildFocus之后hasFocus会变成true
            super.requestChildFocus(child, focused);
            //取得获得焦点的item的position
            mLastFocusView = focused;
            mLastFocusPosition = getChildViewHolder(child).getAdapterPosition();
            Loger.i("focusPos = " + mLastFocusPosition);
        }
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        View lastFocusedItem = getLayoutManager().findViewByPosition(mLastFocusPosition);
        lastFocusedItem.requestFocus();
        return false;

    }

    /**
     * 通过该方法设置选中的item居中
     * <p>
     * 当ViewGroup的某个子View需要被定位在屏幕的某个矩形范围时，调用此方法。重载此方法的ViewGroup可确认以下几点：
     * 1.子View将是ViewGroup里的直系子项
     * 2.矩形将在子View的坐标体系中
     * 重载此方法的ViewGroup应该支持以下几点：
     * 1.若矩形已经是可见的，则没有东西会改变
     * 2.为使矩形区域全部可见，视图将可以被滚动显示
     * <p>
     * 最终计算出的dy，dx的实际意义就是在滚动中下滑和左右滑动的距离
     *
     * @param child     发出请求的子View
     * @param rect      子View坐标系内的矩形，即此子View希望在屏幕上的定位
     * @param immediate 设为true，则禁止动画和平滑移动滚动条
     * @return 进行了滚动操作的这个ViewGroup，是否处理此操作
     */
    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        //计算控制recyclerview 选中item的居中从参数
        int selectedItemOffsetStart = 0;
        int selectedItemOffsetEnd = 0;
        if (mSelectedItemCentered) {
            selectedItemOffsetStart = !isVertical() ? (getFreeWidth() - child.getWidth()) : (getFreeHeight() - child.getHeight());
            selectedItemOffsetStart /= 2;
            selectedItemOffsetEnd = selectedItemOffsetStart;
        }

        Loger.i("selectedItemOffsetStart = " + selectedItemOffsetStart);
        Loger.i("selectedItemOffsetEnd = " + selectedItemOffsetEnd);
        final int parentLeft = getPaddingLeft();
        final int parentTop = getPaddingTop();
        final int parentRight = getWidth() - getPaddingRight();
        final int parentBottom = getHeight() - getPaddingBottom();
        final int childLeft = child.getLeft() + rect.left - child.getScrollX();
        final int childTop = child.getTop() + rect.top - child.getScrollY();
        final int childRight = childLeft + rect.width();
        final int childBottom = childTop + rect.height();


        final int offScreenLeft = Math.min(0, childLeft - parentLeft - selectedItemOffsetStart);
        final int offScreenRight = Math.max(0, childRight - parentRight + selectedItemOffsetEnd);

        final int offScreenTop = Math.min(0, childTop - parentTop - selectedItemOffsetStart);
        final int offScreenBottom = Math.max(0, childBottom - parentBottom + selectedItemOffsetEnd);

        // Favor the "start" layout direction over the end when bringing one side or the other
        // of a large rect into view. If we decide to bring in end because start is already
        // visible, limit the scroll such that start won't go out of bounds.
        final int dx;
        if (getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            dx = offScreenRight != 0 ? offScreenRight
                    : Math.max(offScreenLeft, childRight - parentRight);
        } else {
            dx = offScreenLeft != 0 ? offScreenLeft
                    : Math.min(childLeft - parentLeft, offScreenRight);
        }

        // Favor bringing the top into view over the bottom. If top is already visible and
        // we should scroll to make bottom visible, make sure top does not go out of bounds.
        final int dy = offScreenTop != 0 ? offScreenTop
                : Math.min(childTop - parentTop, offScreenBottom);

        if (dx != 0 || dy != 0) {
            if (immediate) {
                scrollBy(dx, dy);
            } else {
                smoothScrollBy(dx, dy);
            }
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


    /**
     * 是否可以垂直滚动
     **/
    public boolean isCanFocusOutVertical() {
        return mCanFocusOutVertical;
    }

    /**
     * 设置可以垂直滚动
     **/
    public void setCanFocusOutVertical(boolean canFocusOutVertical) {
        mCanFocusOutVertical = canFocusOutVertical;
    }

    /**
     * 是否可以水平滚动
     **/
    public boolean isCanFocusOutHorizontal() {
        return mCanFocusOutHorizontal;
    }

    /**
     * 设置是否可以水平滚动
     **/
    public void setCanFocusOutHorizontal(boolean canFocusOutHorizontal) {
        mCanFocusOutHorizontal = canFocusOutHorizontal;
    }


    /**
     * 设置焦点丢失监听
     */
    public void setFocusLostListener(FocusLostListener focusLostListener) {
        this.mFocusLostListener = focusLostListener;
    }

    public interface FocusLostListener {
        void onFocusLost(View lastFocusChild, int direction);
    }

    /**
     * 设置焦点获取监听
     */
    public void setGainFocusListener(FocusGainListener focusListener) {
        this.mFocusGainListener = focusListener;
    }


    public interface FocusGainListener {
        void onFocusGain(View child, View focued);
    }


}
