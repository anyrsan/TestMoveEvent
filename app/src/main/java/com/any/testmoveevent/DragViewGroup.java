package com.any.testmoveevent;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

/**
 * @author any
 * @date 2017/12/5
 * 上下滑动目标View
 */
public class DragViewGroup extends RelativeLayout {

    //目标view
    private View mTargetView;
    // 当前的移动距离
    private float mCurrentTargetTY;
    //高度
    private int mTargetHeight;
    //背景
    private Drawable backDrawable;
    //动画
    private ValueAnimator animator;

    private boolean interceptDrag = false;

    private static final int INVALID_POINTER = -1;
    private int mTouchSlop;
    private float mInitialDownY;
    private float mInitialDownX;
    private boolean mIsBeingDragged;
    private int mActivePointerId = INVALID_POINTER;


    public DragViewGroup(@NonNull Context context) {
        super(context);
    }

    public DragViewGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DragViewGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    private void initBase() {
        setWillNotDraw(false);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initBase();
        //注意实际上不能这样写
        mTargetView = getChildAt(0);
        backDrawable = getBackground();
        getBackground().setAlpha(0);
        mTargetView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mTargetHeight = mTargetView.getHeight();
                mCurrentTargetTY = mTargetView.getTranslationY();
                mTargetView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                openView();
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        super.onInterceptTouchEvent(ev);
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                interceptDrag = false;
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                float initialDownY = getMotionEventY(ev, mActivePointerId);
                float initialDownX = getMotionEventX(ev, mActivePointerId);
                if (initialDownY == -1 || initialDownX == -1) {
                    return false;
                }
                mInitialDownY = initialDownY;
                mInitialDownX = initialDownX;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                final float y = getMotionEventY(ev, mActivePointerId);
                final float x = getMotionEventX(ev, mActivePointerId);
                if (y == -1 || x == -1) {
                    return false;
                }
                final float xDiff = mInitialDownX - x;
                final float yDiff = mInitialDownY - y;
                //从这里开始处理

                if (Math.abs(xDiff) > mTouchSlop || Math.abs(xDiff) > Math.abs(yDiff) || interceptDrag) {
                    return false;
                }

                if (!mIsBeingDragged) {
                    mInitialDownY = y;
                    mIsBeingDragged = true;
                }
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_UP:
                finishSpinner();
                interceptDrag = mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
            case MotionEvent.ACTION_CANCEL:
                resetTargetView();
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }
        return mIsBeingDragged;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex = -1;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = true;
                break;
            case MotionEvent.ACTION_MOVE:
                float initialDownY = getMotionEventY(ev, mActivePointerId);
                if (initialDownY == -1) {
                    return false;
                }
                //降低滑动速率
                final float offset = (initialDownY - mInitialDownY) * 0.7f;
                mInitialDownY = initialDownY;
                // 不能处理时,应该返回false
                if (mIsBeingDragged) {
                    boolean isTrue = drag(offset);
                    if (!isTrue) {
                        resetMotionEvent(ev);
                    }
                }
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN:
                pointerIndex = MotionEventCompat.getActionIndex(ev);
                if (pointerIndex < 0) {
                    return false;
                }
                mActivePointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
                initialDownY = getMotionEventY(ev, mActivePointerId);
                if (initialDownY == -1) {
                    return false;
                }
                mInitialDownY = initialDownY;
                mIsBeingDragged = true;
                break;
            case MotionEvent.ACTION_UP:
                pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                finishSpinner();
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                return false;
        }
        return true;
    }

    private boolean drag(float offset) {
        mCurrentTargetTY = mTargetView.getTranslationY();
        mTargetView.setTranslationY(mCurrentTargetTY + offset);
        float mCurrY = Math.abs(mCurrentTargetTY);
        setBackDrawableAlpha(mCurrY);
        return true;
    }

    private void setBackDrawableAlpha(float mCurrY) {
        float percent = mCurrY / getHeight();
        percent = 1 - Math.min(1, Math.max(0, percent));
        int alpha = (int) (percent * 255);
        getBackground().setAlpha(alpha);

        if (alpha == 0 && null != icloseView) {
            icloseView.closeView();
        }
    }


    private void finishSpinner() {
        if (Math.abs(mCurrentTargetTY) < mTargetHeight / 4) {
            resetTargetView();
        } else {
            closeView();
        }
    }

    public void closeView() {
        cancelAnim();
        float endF = 0f;
        if (mCurrentTargetTY < 0) {
            endF = -getHeight();
        } else {
            endF = getHeight();
        }
        updateTargetView(mTargetView, mCurrentTargetTY, endF);
    }

    public void openView() {
        float startY = mCurrentTargetTY;
        float endY = 0;
        if (startY != endY) {
            updateTargetView(mTargetView, startY, endY);
        }
    }


    /**
     * 还原
     */
    private void resetTargetView() {
        cancelAnim();
        float startY = mCurrentTargetTY;
        float endY = 0;
        if (startY != endY) {
            updateTargetView(mTargetView, startY, endY);
        }
    }

    /**
     * 更正view
     *
     * @param targetView
     * @param startF
     * @param endF
     */
    private void updateTargetView(final View targetView, float startF, float endF) {
        cancelAnim();
        animator = ValueAnimator.ofFloat(startF, endF);
        animator.setTarget(targetView);
        animator.setDuration(400).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mTargetView.setTranslationY(value);
                mCurrentTargetTY = mTargetView.getTranslationY();
                setBackDrawableAlpha(Math.abs(mCurrentTargetTY));
            }
        });
    }

    private void cancelAnim() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        animator = null;
    }


    public IBrowserCloseView icloseView;

    public void setIBrowserCloseView(IBrowserCloseView icloseView) {
        this.icloseView = icloseView;
    }

    public interface IBrowserCloseView {
        void closeView();
    }


    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    private float getMotionEventX(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getX(ev, index);
    }

    public MotionEvent resetMotionEvent(MotionEvent ev) {
        int action = ev.getAction();
        ev.setAction(MotionEvent.ACTION_CANCEL);
        super.dispatchTouchEvent(ev);
        ev.setAction(MotionEvent.ACTION_DOWN);
        super.dispatchTouchEvent(ev);
        ev.setAction(action);
        return ev;
    }


    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        interceptDrag = b;
    }


}
