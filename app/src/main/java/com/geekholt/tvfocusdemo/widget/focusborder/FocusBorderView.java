package com.geekholt.tvfocusdemo.widget.focusborder;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.geekholt.tvfocusdemo.R;
import com.geekholt.tvfocusdemo.animator.FocusValueAnimator;


/**
 * @Author：wuhaoteng
 * @Date:2018/12/20
 * @Desc：
 */
public class FocusBorderView extends FrameLayout {
    private Context context;
    private ImageView focusStatusImg;
    private RelativeLayout containerView;
    private AttributeSet attrs;

    public FocusBorderView(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public FocusBorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;
        initView();
    }

    public FocusBorderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        this.attrs = attrs;
        initView();
    }

    public void initView() {
        LayoutInflater.from(context).inflate(R.layout.layout_border_view, this, true);
        focusStatusImg = (ImageView) findViewById(R.id.img_focus_status);
        containerView = (RelativeLayout) findViewById(R.id.layout_container_view);
        setFocusable(true);
        bindAnimator();
    }

    public void addContent(View view) {
        containerView.addView(view);
    }

    public RelativeLayout getContainerView() {
        return containerView;
    }

    public AttributeSet getAttrs() {
        return attrs;
    }

    /**
     * 加上动画效果
     */
    public void bindAnimator() {
        final FocusValueAnimator animator = new FocusValueAnimator(this);
        setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                View focusedChild = getFocusedChild();
                if (focusedChild != null || hasFocus) {
                    //自身或者子view被聚焦
                    focusStatusImg.setVisibility(VISIBLE);
                    animator.start();

                } else {
                    focusStatusImg.setVisibility(GONE);
                    animator.cancel();
                }
            }
        });

        addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                animator.cancel();
            }
        });
    }

}
