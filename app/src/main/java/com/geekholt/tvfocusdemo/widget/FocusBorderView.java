package com.geekholt.tvfocusdemo.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.geekholt.tvfocusdemo.R;
import com.geekholt.tvfocusdemo.animator.FocusValueAnimator;
import com.geekholt.tvfocusdemo.util.Loger;


/**
 * @Author：wuhaoteng
 * @Date:2018/12/20
 * @Desc：
 */
public class FocusBorderView extends FrameLayout {
    private Context context;
    private ImageView focusBorderImg;
    //borderview动画
    private FocusValueAnimator animator;

    //用户自定义的xml中的viewGroup与borderview的间距
    private int margin = 8;
    //borderview样式
    private int drawableRes;

    public FocusBorderView(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public FocusBorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    public FocusBorderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        initView();
    }

    public void initView() {
        setFocusable(true);
        addFocusBorder();
        animator = new FocusValueAnimator(this);

    }

    /**
     * 布局完之后，设置用户自定义的xml中的viewGroup与borderview的间距
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int childCount = getChildCount();
        View view = getChildAt(1);
        if (childCount == 2) {
            //firstview is the borderview
            //second view is the first viewGroup that was found in the customer xml
            if (view instanceof ViewGroup) {
                FrameLayout.LayoutParams lp = (LayoutParams) view.getLayoutParams();
                lp.setMargins(margin, margin, margin, margin);
            } else {
                throw new RuntimeException("The FocusBorderView must container one and the only one ViewGroup");
            }
        } else {
            throw new RuntimeException("The FocusBorderView must container one and the only one ViewGroup");
        }
    }

    /**
     * 添加borderview
     **/
    public void addFocusBorder() {
        focusBorderImg = new ImageView(context);
        focusBorderImg.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.good_bac_focus_select));
        this.addView(focusBorderImg);
        focusBorderImg.setVisibility(GONE);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        View focusedChild = getFocusedChild();
        if (focusedChild != null || isFocused()) {
            //自身或者子view被聚焦
            Loger.i("focus");
            focusBorderImg.setVisibility(VISIBLE);
            animator.start();
        } else {
            Loger.i("nofocus");
            focusBorderImg.setVisibility(GONE);
            animator.cancel();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        animator.cancel();
    }
}
