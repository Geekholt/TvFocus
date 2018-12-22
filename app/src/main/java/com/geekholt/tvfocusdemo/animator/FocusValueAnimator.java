package com.geekholt.tvfocusdemo.animator;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.LinearInterpolator;

/**
 * @Author：wuhaoteng
 * @Date:2018/12/19
 * @Desc：
 */
public class FocusValueAnimator extends ValueAnimator {
    private final int ANIMATOR_DURATION = 300;
    private final float START_VALUE = 1.0f;
    private final float END_VALUE = 1.08f;
    private View itemView;

    public FocusValueAnimator(final View itemView) {
        this.itemView = itemView;
        setDuration(ANIMATOR_DURATION);
        setFloatValues(START_VALUE, END_VALUE);
        setInterpolator(new LinearInterpolator());
        addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                updateUI((Float) animation.getAnimatedValue());
                invalidatePath(itemView);
            }
        });
        addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                invalidatePath(itemView);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                updateUI(START_VALUE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @Override
    public void cancel() {
        super.cancel();
        updateUI(START_VALUE);
    }

    public void updateUI(float scale) {
        if (itemView != null) {
            itemView.setPivotX(itemView.getWidth() / 2);
            itemView.setPivotY(itemView.getHeight() / 2);
            itemView.setScaleX(scale);
            itemView.setScaleY(scale);
        }
    }

    /**
     * 有些盒子存在动画缓存，所以刷新一下
     */
    private void invalidatePath(View itemView) {
        if (itemView != null) {
            itemView.invalidate();
            ViewParent vp = itemView.getParent();
            int loop = 5;
            while (vp != null) {
                loop--;
                if (loop < 0) {
                    break;
                }
                if (vp instanceof View) {
                    ((View) vp).invalidate();
                }
                vp = vp.getParent();
            }
        }
    }
}
