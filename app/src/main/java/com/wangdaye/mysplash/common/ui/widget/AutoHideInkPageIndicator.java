package com.wangdaye.mysplash.common.ui.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;

import com.pixelcan.inkpageindicator.InkPageIndicator;

/**
 * Auto hide ink page indicator.
 *
 * A {@link InkPageIndicator} that can hide itself automatically.
 *
 * */

public class AutoHideInkPageIndicator extends InkPageIndicator {

    private ObjectAnimator showAnimator;
    private ObjectAnimator dismissAnimator;

    public AutoHideInkPageIndicator(Context context) {
        super(context);
    }

    public AutoHideInkPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoHideInkPageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDisplayState(boolean show) {
        if (dismissAnimator != null) {
            dismissAnimator.cancel();
        }
        if (show) {
            if (showAnimator != null) {
                showAnimator.cancel();
            }
            showAnimator = ObjectAnimator.ofFloat(this, "alpha", getAlpha(), 0.7F)
                    .setDuration(100);
            showAnimator.start();
        } else {
            dismissAnimator = ObjectAnimator.ofFloat(this, "alpha", getAlpha(), 0)
                    .setDuration(200);
            dismissAnimator.setStartDelay(600);
            dismissAnimator.start();
        }
    }
}
