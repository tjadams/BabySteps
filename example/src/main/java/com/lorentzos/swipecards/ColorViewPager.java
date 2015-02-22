package com.lorentzos.swipecards;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Window;
import android.view.WindowManager;

import java.util.jar.Attributes;

/**
 * Created by anthony on 15-02-22.
 */
public class ColorViewPager extends ViewPager {

    private ActionBar actionBar;
    private PagerTabStrip pagerTabStrip;
    private Window window;

    public ColorViewPager(Context context) {
        super(context);
    }

    public ColorViewPager(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

        @Override
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
        if(actionBar != null) {
            if(position == 0) {
                int color = Color.HSVToColor(new float[] { 90f - offset * 30f, .9f, 1f });
                int darkColor = Color.HSVToColor(new float[] { 90f - offset * 30f, .7f, .6f });
                actionBar.setBackgroundDrawable(new ColorDrawable(color));
                pagerTabStrip.setBackgroundDrawable(new ColorDrawable(color));
                window.setStatusBarColor(darkColor);
            } else if(position == 1) {
                int color = Color.HSVToColor(new float[] { 60f - offset * 60f, .9f, 1f });
                int darkColor = Color.HSVToColor(new float[] { 60f - offset * 60f, .7f, .6f });
                actionBar.setBackgroundDrawable(new ColorDrawable(color));
                pagerTabStrip.setBackgroundDrawable(new ColorDrawable(color));
                window.setStatusBarColor(darkColor);
            } else {
                int color = Color.HSVToColor(new float[] { 0f, 1f, 1f });
                int darkColor = Color.HSVToColor(new float[] { 0f, .7f, .6f });
                actionBar.setBackgroundDrawable(new ColorDrawable(color));
                pagerTabStrip.setBackgroundDrawable(new ColorDrawable(color));
                window.setStatusBarColor(darkColor);
            }
        }
    }

    public void setActionBar(ActionBar actionBar) {
        this.actionBar = actionBar;
        pagerTabStrip = (PagerTabStrip)findViewById(R.id.pager_header);
    }

    public void setWindow(Window window) {
        this.window = window;
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }
}
