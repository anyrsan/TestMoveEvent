package com.any.testmoveevent;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

/**
 * @author any
 * @date 2017/12/6
 * 此view是用来处理循环的，完成分页显示  基于3个view循环
 */
public class MyImageViewGoup extends ViewGroup {
    private SparseArray<View> mapView = null;
    private static final int INVALID_POINTER = -1;
    private int mTouchSlop;
    private float mInitialDownX;
    private boolean mIsBeingDragged;
    private int mActivePointerId = INVALID_POINTER;
    private boolean interceptDrag = false;
    private Scroller mScroller;
    private int mCurScreen = 0;
    private int mTotal = 1;


    private final static int INTERLACES = 15;
    private final static int SNAP_VELOCITY = 100;
    private final static int DEFAULT_DURATION = 300;

    private VelocityTracker velocityTracker;
    private ISubView iSubView;

    //外层更改此值就可以配置成动态了
    private int interlaces = INTERLACES;
    private int duration = DEFAULT_DURATION;

    public MyImageViewGoup(Context context) {
        super(context);
    }

    public MyImageViewGoup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyImageViewGoup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initBase();
    }

    private void initBase() {
        setWillNotDraw(false);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mScroller = new Scroller(getContext(),new LinearInterpolator());
    }


    /**
     * 处理view
     *
     * @param isubview
     */
    public void setViewGoup(ISubView isubview) {
        mapView = isubview.getView(getContext());
        iSubView = isubview;
        mTotal = isubview.getSize();
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);


        addView(getView(0), params);
        addView(getView(1), params);
        addView(getView(2), params);

        //定位view
        requestLayout();
        startLoadData();
    }

    public void startLoadData() {
        openPage(mCurScreen);
        loadData(mCurScreen);
        int tpage = mCurScreen - 1;
        if (tpage > -1) {
            loadData(tpage);
        }
        tpage = mCurScreen + 1;
        if (tpage < mTotal) {
            loadData(tpage);
        }
    }


    private void startAnimation(int mPage) {
        stopScroller();
        final int delta = getTargetWidth(mPage) - getScrollX();
        mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
        postInvalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }


    /***
     * get view by map
     *
     * @param page
     * @return
     */
    private View getView(int page) {
        if (mapView == null) return null;
        page = page % mapView.size();
        return mapView.get(page);
    }

    /***
     * view layout local
     *
     * @param mCurScreen
     */
    private void layoutView(int mCurScreen) {
        View view = getView(mCurScreen);
        if (view == null) return;
        if (mCurScreen >= 0 && mCurScreen < mTotal) {
            view.layout(getTargetWidth(mCurScreen), 0,
                    getTargetWidth(mCurScreen) + getWidth(), getHeight());
        } else {
            view.layout(0, 0, 0, 0);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        initLayout();
    }

    //初始化view位置
    private void initLayout() {
        layoutView(mCurScreen - 1);
        layoutView(mCurScreen);
        layoutView(mCurScreen + 1);
        scrollTo(getTargetWidth(mCurScreen), 0);
    }

    private void stopScroller() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
    }

    //interlaces*2 超出屏幕
    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(Math.max(0, Math.min(x, getTargetWidth(mTotal - 1))), y);
    }

    public int getTargetWidth(int mCurScreen) {
        return (getWidth() + interlaces) * (mCurScreen);
    }

    public int getLocalX() {
        return getTargetWidth(mCurScreen);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        super.onInterceptTouchEvent(ev);
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                stopScroller();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                float initialDownX = getMotionEventX(ev, mActivePointerId);
                if (initialDownX == -1) {
                    return false;
                }
                mInitialDownX = initialDownX;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                final float x = getMotionEventX(ev, mActivePointerId);
                if (x == -1) {
                    return false;
                }
                final float xDiff = mInitialDownX - x;
                //从这里开始处理

                if (Math.abs(xDiff) < mTouchSlop || interceptDrag) {
                    return false;
                }

                if (!mIsBeingDragged) {
                    mInitialDownX = x;
                    mIsBeingDragged = true;
                    setRequestDisallowInterceptTouchEvent(true); //请求不要再拦截了
                }
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                moveSelf();
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }
        return mIsBeingDragged;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);
        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = true;
                stopScroller();
                break;
            case MotionEvent.ACTION_MOVE:
                float initialDownX = getMotionEventX(ev, mActivePointerId);
                if (initialDownX == -1) {
                    return false;
                }
                //降低滑动速率
                final float offset = (initialDownX - mInitialDownX) * 0.9f;
                mInitialDownX = initialDownX;

                // 不能处理时,应该返回false
                if (mIsBeingDragged) {
                    boolean isTrue = drag(-offset);
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
                initialDownX = getMotionEventX(ev, mActivePointerId);
                if (initialDownX == -1) {
                    return false;
                }
                mInitialDownX = initialDownX;
                mIsBeingDragged = true;
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.computeCurrentVelocity(1000);
                int velocityX = (int) velocityTracker.getXVelocity();
                int offsetX = getScrollX() - getLocalX();
                int mPage = mCurScreen;
                if (mPage > 0 && velocityX > SNAP_VELOCITY && -offsetX > getWidth() / 10) {
                    movePrePage();
                } else if (mPage < mTotal - 1 && velocityX < -SNAP_VELOCITY && offsetX > getWidth() / 10) {
                    moveNextPage();
                } else {
                    moveSelf();
                }
                velocityTracker.recycle();
                velocityTracker = null;
                pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                return false;
            case MotionEvent.ACTION_CANCEL:
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                moveSelf();
                break;
        }
        return true;
    }

    private void moveSelf() {
        startAnimation(mCurScreen);
    }

    public void moveNextPage() {
        mCurScreen++;
        handlerNext(mCurScreen);
        startAnimation(mCurScreen);
    }

    public void movePrePage() {
        mCurScreen--;
        handlerPrevious(mCurScreen);
        startAnimation(mCurScreen);
    }


    private void handlerPrevious(int mPage) {
        destroyData(mPage + 2);
        layoutView(mPage - 1);
        loadData(mPage - 1);
        openPage(mPage);
    }

    private void handlerNext(int mPage) {
        destroyData(mPage - 2);
        layoutView(mPage + 1);
        loadData(mPage + 1);
        openPage(mPage);
    }

    private void destroyData(int page) {
        if (page > mTotal - 1 || page < 0) return;
        if (iSubView != null) {
            iSubView.destroyData(page, getView(page));
        }
    }

    private void loadData(int page) {
        if (page > mTotal - 1 || page < 0) return;
        if (iSubView != null) {
            iSubView.loadData(page, getView(page));
        }
    }

    private void openPage(int page) {
        if (iSubView != null) {
            iSubView.openPage(page);
        }
    }


    /**
     * 需要
     *
     * @param offset
     * @return
     */
    private boolean drag(float offset) {
        scrollBy((int) offset, 0);
        return true;
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
        //继续传递
        setRequestDisallowInterceptTouchEvent(b);
    }


    private void setRequestDisallowInterceptTouchEvent(boolean intercept) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(intercept);
        }
    }

}
