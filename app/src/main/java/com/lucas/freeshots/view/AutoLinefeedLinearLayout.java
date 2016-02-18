package com.lucas.freeshots.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class AutoLinefeedLinearLayout extends ViewGroup {
    public AutoLinefeedLinearLayout(Context context) {
        super(context);
    }

    public AutoLinefeedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoLinefeedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int count = getChildCount();

        if(count == 0) {
            setMeasuredDimension(0, 0);
            return;
        }

        int minWidth = 0;

        for(int i = 0; i < count; i++) {
            View v = getChildAt(i);
            if(v != null && v.getVisibility() != View.GONE) {
                minWidth = Math.max(minWidth, v.getMeasuredWidth());
            }
        }

        if(widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(minWidth, calMeasuredHeight(minWidth));
        } else if(widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(minWidth, calMeasuredHeight(minWidth));
        } else if(heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, calMeasuredHeight(widthSize));
        } else {
            setMeasuredDimension(widthSize, calMeasuredHeight(widthSize));
        }
    }

    /**
     * 计算 MeasuredHeight
     * @param measuredWidth MeasuredWidth
     * @return MeasuredHeight
     */
    private int calMeasuredHeight(int measuredWidth) {
        int count = getChildCount();
        if(count == 0) {
            return 0;
        }

        int left = 0;
        int measuredHeight = 0;
        int singleRowHeight = 0;

        for(int i = 0; i < count; i++) {
            View v = getChildAt(i);
            if(v != null && v.getVisibility() != View.GONE) {
                int w = v.getMeasuredWidth();
                if((w + left > measuredWidth)) {
                    // 一行满了
                    left = w;
                    measuredHeight += singleRowHeight;
                    singleRowHeight = v.getMeasuredHeight();
                } else {
                    singleRowHeight = Math.max(singleRowHeight, v.getMeasuredHeight());
                    left += w;
                }
            }
        }

        measuredHeight += singleRowHeight; // 加上最后未满一行的高度
        return measuredHeight + getPaddingTop() + getPaddingBottom();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();

        int childTop = getPaddingTop();
        int childLeft = getPaddingLeft();
        final int linearWidth = getMeasuredWidth();

        for(int i = 0; i < count; i++) {
            View v = getChildAt(i);
            if(v != null && v.getVisibility() != View.GONE) {
                int w = v.getMeasuredWidth();
                if(w > linearWidth - childLeft) {
                    childTop += v.getMeasuredHeight();
                    childLeft = getPaddingLeft();
                }
                v.layout(childLeft, childTop, childLeft + w, childTop + v.getMeasuredHeight());
                childLeft += w;
            }
        }
    }
}
