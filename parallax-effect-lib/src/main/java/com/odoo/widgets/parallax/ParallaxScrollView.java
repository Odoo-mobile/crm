package com.odoo.widgets.parallax;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

public class ParallaxScrollView extends ScrollView {

    private static final int DEFAULT_PARALLAX_VIEWS = 1;
    private static final float DEFAULT_INNER_PARALLAX_FACTOR = 1.9F;
    private static final float DEFAULT_PARALLAX_FACTOR = 1.9F;
    private static final float DEFAULT_ALPHA_FACTOR = -1F;
    private int numOfParallaxViews = DEFAULT_PARALLAX_VIEWS;
    private float innerParallaxFactor = DEFAULT_PARALLAX_FACTOR;
    private float parallaxFactor = DEFAULT_PARALLAX_FACTOR;
    private float alphaFactor = DEFAULT_ALPHA_FACTOR;
    private ArrayList<ParallaxedView> parallaxedViews = new ArrayList<ParallaxedView>();
    private OnParallaxScrollView mOnParallaxScrollView;
    private int parallax_overlay_color = Color.BLACK;
    private float parallax_actionbar_height = 0.0f;
    private float parallax_actionbar_title_font_size = 0.0f;
    private Resources res;
    private ActionBar mActionBar = null;

    public ParallaxScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public ParallaxScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ParallaxScrollView(Context context) {
        super(context);
    }

    public interface OnParallaxScrollView {
        void onScrollChanged(ParallaxScrollView v, int l, int t, int oldl,
                             int oldt);
    }

    public void setOnScrollViewListener(OnParallaxScrollView l) {
        this.mOnParallaxScrollView = l;
    }

    protected void init(Context context, AttributeSet attrs) {
        res = context.getResources();
        ActionBarActivity activity = (ActionBarActivity) context;
        mActionBar = activity.getSupportActionBar();

        TypedArray typeArray = context.obtainStyledAttributes(attrs,
                R.styleable.ParallaxScroll);
        this.parallaxFactor = typeArray.getFloat(
                R.styleable.ParallaxScroll_parallax_factor,
                DEFAULT_PARALLAX_FACTOR);
        this.alphaFactor = typeArray.getFloat(
                R.styleable.ParallaxScroll_alpha_factor, DEFAULT_ALPHA_FACTOR);
        this.innerParallaxFactor = typeArray.getFloat(
                R.styleable.ParallaxScroll_inner_parallax_factor,
                DEFAULT_INNER_PARALLAX_FACTOR);
        this.numOfParallaxViews = typeArray.getInt(
                R.styleable.ParallaxScroll_parallax_views_num,
                DEFAULT_PARALLAX_VIEWS);
        parallax_overlay_color = typeArray.getColor(
                R.styleable.ParallaxScroll_parallax_overlay_color, Color.BLACK);
        parallax_actionbar_height = typeArray
                .getDimension(
                        R.styleable.ParallaxScroll_parallax_actionbar_height,
                        getContext().getResources().getDimension(
                                R.dimen.actionBarSize));
        parallax_actionbar_title_font_size = typeArray.getDimension(
                R.styleable.ParallaxScroll_parallax_actionbar_title_font_size,
                getContext().getResources()
                        .getDimension(R.dimen.actionFontSize));
        typeArray.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        makeViewsParallax();
    }

    public void setParallaxOverLayColor(int color) {
        parallax_overlay_color = color;
    }

    public void setActionBar(ActionBar actionBar) {
        mActionBar = actionBar;
        if (mActionBar != null) {
            mActionBar.setTitle("");
            mActionBar.setBackgroundDrawable(res
                    .getDrawable(R.drawable.action_bar_shade));
        }
    }

    TextView titleBar = null;
    float titleBarFontSize = 0;

    private void makeViewsParallax() {
        parallaxedViews.clear();
        if (getChildCount() > 0 && getChildAt(0) instanceof ViewGroup) {
            ViewGroup viewsHolder = (ViewGroup) getChildAt(0);
            int numOfParallaxViews = Math.min(this.numOfParallaxViews,
                    viewsHolder.getChildCount());
            for (int i = 0; i < numOfParallaxViews; i++) {
                ParallaxedView parallaxedView = new ScrollViewParallaxedItem(
                        viewsHolder.getChildAt(i));
                parallaxedViews.add(parallaxedView);
            }

            View titleViewHolder = viewsHolder.findViewById(android.R.id.title);
            if (titleViewHolder != null && titleBar == null) {
                titleBar = (TextView) titleViewHolder;
                titleBar.setBackgroundResource(R.drawable.title_shade);
                titleBarFontSize = titleBar.getTextSize();
                if (mActionBar != null) {
                    mActionBar.setTitle("");
                    mActionBar.setBackgroundDrawable(res
                            .getDrawable(R.drawable.action_bar_shade));
                }
            }
        }

    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if (mOnParallaxScrollView != null)
            mOnParallaxScrollView.onScrollChanged(this, l, t, oldl, oldt);

        int action_color = parallax_overlay_color;
        int r = (action_color >> 16) & 0xFF;
        int g = (action_color >> 8) & 0xFF;
        int b = (action_color >> 0) & 0xFF;

        super.onScrollChanged(l, t, oldl, oldt);
        float parallax = parallaxFactor;
        float alpha = alphaFactor;
        t = (t < 0) ? 0 : t;
        for (ParallaxedView parallaxedView : parallaxedViews) {
            FrameLayout frameLayout = (FrameLayout) parallaxedView.getView();
            int action_bar = (int) (parallaxedView.getView().getBottom() - parallax_actionbar_height);
            if (titleBar != null) {
                float min = parallax_actionbar_title_font_size;
                float max = titleBarFontSize;
                float decrease = ((min * action_bar) / t);
                decrease = (decrease > max) ? max : decrease;
                decrease = (decrease < min) ? min : decrease;
                titleBar.setTextSize(TypedValue.DENSITY_DEFAULT, decrease);
            }
            int image_overlay = ((t * 255) / action_bar);
            image_overlay = (image_overlay > 255) ? 255 : image_overlay;
            frameLayout.setForeground(new ColorDrawable(Color.argb(image_overlay, r, g, b)));
            if (mActionBar != null) {

                if (image_overlay >= 255) {
                    mActionBar.setBackgroundDrawable(new ColorDrawable(
                            action_color));
                    if (titleBar != null) {
                        mActionBar.setTitle(titleBar.getText());
                        titleBar.setVisibility(View.INVISIBLE);
                    }
                } else {
                    mActionBar.setBackgroundDrawable(new ColorDrawable(Color
                            .parseColor("#00000000")));
                    if (titleBar != null) {
                        mActionBar.setTitle("");
                        titleBar.setVisibility(View.VISIBLE);
                        if (image_overlay > 100) {
                            titleBar.setBackgroundColor(Color.parseColor("#00000000"));
                        } else {
                            titleBar.setBackgroundResource(R.drawable.title_shade);
                        }
                    }
                }
                if (image_overlay <= 10) {
                    mActionBar.setBackgroundDrawable(res
                            .getDrawable(R.drawable.action_bar_shade));
                }
            }
            float offset = ((float) t / parallax);
            parallaxedView.setOffset(offset);
            parallax *= innerParallaxFactor;
            if (alpha != DEFAULT_ALPHA_FACTOR) {
                float fixedAlpha = (t <= 0) ? 1 : (100 / ((float) t * alpha));
                parallaxedView.setAlpha(fixedAlpha);
                alpha /= alphaFactor;
            }
            parallaxedView.animateNow();
        }
    }

    protected class ScrollViewParallaxedItem extends ParallaxedView {

        public ScrollViewParallaxedItem(View view) {
            super(view);
        }

        @Override
        protected void translatePreICS(View view, float offset) {
            view.offsetTopAndBottom((int) offset - lastOffset);
            lastOffset = (int) offset;
        }
    }

}
