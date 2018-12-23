package com.geekholt.tvfocusdemo.widget;

import android.content.Context;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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

    private View customRootView;

    //用户自定义的xml中的viewGroup与borderview的间距
    private int margin = 8;
    //borderview样式
    private int drawableRes;
    //指定默认聚焦的id
    private int specialViewId;

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
        getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                if (isFocused()) {
                    //找到用户指定focusBorderview内默认聚焦的view
                    View specifiedView = focusSpecifiedView(R.id.view4);
                    if (specifiedView != null) {
                        specifiedView.requestFocus();
                        Loger.i("special view focus" + specifiedView.toString());
                    }
                }
                //判断是否自身被聚焦或者存在子view被聚焦
                if (isFocused() || getFocusedChild() != null) {
                    focusEnter();
                } else {
                    focusLeave();
                }
            }
        });

    }

    /**
     * 布局完之后，设置用户自定义的xml中的viewGroup与borderview的间距
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int childCount = getChildCount();
        if (childCount == 2) {
            //firstview is the borderview
            //second view is the first viewGroup that was found in the customer xml
            customRootView = getChildAt(1);
            if (customRootView instanceof ViewGroup) {
                FrameLayout.LayoutParams lp = (LayoutParams) customRootView.getLayoutParams();
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
    public View focusSearch(View focused, int direction) {
        View findFocus = FocusFinder.getInstance().findNextFocus(this, focused, direction);
        //根据原生焦点流程默认找到的view，如果该方向上有可聚焦的view则找到该view
        //如果没有则会调用viewgroup的focusSearch方法，找到最近的另一个view或viewgroup
        View nextFocus = super.focusSearch(focused, direction);
        if (findFocus == null) {
            //当前focusBorderView内，在direction方向上已经没有可聚焦的view，说明焦点将进入另一个viewgroup
            if (nextFocus != null) {
                focusLeave();
            } else {
                nextFocus = focused;
            }
        }
        return nextFocus;
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        animator.cancel();
    }


    /**
     * 聚焦到指定的view
     */
    public View focusSpecifiedView(@IdRes int viewId) {
        View specifedView = null;
        if (customRootView != null) {
            specifedView = customRootView.findViewById(viewId);
        }
        return specifedView;
    }

    /**
     * 自身或者子view存在焦点
     */
    public void focusEnter() {
        if (focusBorderImg != null && focusBorderImg.getVisibility() == GONE) {
            Loger.i("focusEnter");
            focusBorderImg.setVisibility(VISIBLE);
            animator.start();
        }
    }

    /**
     * 自身或者子view都不存在焦点
     */
    public void focusLeave() {
        if (focusBorderImg != null && focusBorderImg.getVisibility() == VISIBLE) {
            Loger.i("focusLeave");
            focusBorderImg.setVisibility(GONE);
            animator.cancel();
        }
    }
}
