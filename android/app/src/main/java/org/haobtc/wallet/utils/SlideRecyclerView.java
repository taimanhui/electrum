package org.haobtc.wallet.utils;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Support for sideslip removal RecyclerView
 * <p>
 * Created by DavidChen on 2018/5/29.
 */

public class SlideRecyclerView extends RecyclerView {

    private static final String TAG = "SlideRecyclerView";
    private static final int INVALID_POSITION = -1;
    private static final int INVALID_CHILD_WIDTH = -1;
    private static final int SNAP_VELOCITY = 600;   // Minimum sliding speed

    private VelocityTracker mVelocityTracker;   // Speed Tracker
    private int mTouchSlop; // Considered as the minimum distance of sliding (generally provided by the system)
    private Rect mTouchFrame;   // Rectangle range of child view
    private Scroller mScroller;
    private float mLastX;   // Record the last touch point x during sliding
    private float mFirstX, mFirstY; // First touch range
    private boolean mIsSlide;   // Slide subview or not
    private ViewGroup mFlingView;   // Touch child view
    private int mPosition;  // Location of the view touched
    private int mMenuViewWidth;    // Menu button width

    public SlideRecyclerView(Context context) {
        this(context, null);
    }

    public SlideRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mScroller = new Scroller(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        obtainVelocity(e);
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {  // If the animation has not stopped, terminate the animation immediately
                    mScroller.abortAnimation();
                }
                mFirstX = mLastX = x;
                mFirstY = y;
                mPosition = pointToPosition(x, y);
                if (mPosition != INVALID_POSITION) {
                    View view = mFlingView;
                    // Get the view of the touchpoint
                    mFlingView = (ViewGroup) getChildAt(mPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition());
                    // Here we judge that if the previously touched view has been opened and the currently encountered view is not that view, then immediately close the previous view. There is no need to assume that the animation has not completed the conflict, because the previous view has been abortanimation
                    if (view != null && mFlingView != view && view.getScrollX() != 0) {
                        view.scrollTo(0, 0);
                    }
                    // There is a mandatory requirement here. The child ViewGroup of recyclerview must have two child views, so that the menu button has a value,
                    // Note: if you do not customize the child view of recyclerview, the child view must have a fixed width.
                    // For example, if LinearLayout is used as the root layout, and the width of the content part is already match [parent], then if wrap [content] is used in the menu view, the width of the menu will be 0.
                    if (mFlingView.getChildCount() == 2) {
                        mMenuViewWidth = mFlingView.getChildAt(1).getWidth();
                    } else {
                        mMenuViewWidth = INVALID_CHILD_WIDTH;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.computeCurrentVelocity(1000);
                // There are two judgments here. One is sideslip:
                // 1. If the speed in X direction is greater than that in Y direction and greater than the minimum speed limit;
                // 2. If the side slip distance in X direction is greater than that in Y direction, and the X direction reaches the minimum slip distance;
                float xVelocity = mVelocityTracker.getXVelocity();
                float yVelocity = mVelocityTracker.getYVelocity();
                if (Math.abs(xVelocity) > SNAP_VELOCITY && Math.abs(xVelocity) > Math.abs(yVelocity)
                        || Math.abs(x - mFirstX) >= mTouchSlop
                        && Math.abs(x - mFirstX) > Math.abs(y - mFirstY)) {
                    mIsSlide = true;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                releaseVelocity();
                break;
        }
        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mIsSlide && mPosition != INVALID_POSITION) {
            float x = e.getX();
            obtainVelocity(e);
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    // Slide with fingers
                    if (mMenuViewWidth != INVALID_CHILD_WIDTH) {
                        float dx = mLastX - x;
                        if (mFlingView.getScrollX() + dx <= mMenuViewWidth
                                && mFlingView.getScrollX() + dx > 0) {
                            mFlingView.scrollBy((int) dx, 0);
                        }
                        mLastX = x;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mMenuViewWidth != INVALID_CHILD_WIDTH) {
                        int scrollX = mFlingView.getScrollX();
                        mVelocityTracker.computeCurrentVelocity(1000);
                        // There are two reasons to decide whether to open the menu:
                        // 1. The pull out width of the menu is more than half of the menu width;
                        // 2. The lateral sliding speed is greater than the minimum sliding speed;
                        // Note: the reason why it is less than negative value is that the speed is negative when sliding to the left
                        if (mVelocityTracker.getXVelocity() < -SNAP_VELOCITY) {    // 向左侧滑达到侧滑最低速度，则打开
                            mScroller.startScroll(scrollX, 0, mMenuViewWidth - scrollX, 0, Math.abs(mMenuViewWidth - scrollX));
                        } else if (mVelocityTracker.getXVelocity() >= SNAP_VELOCITY) {  // 向右侧滑达到侧滑最低速度，则关闭
                            mScroller.startScroll(scrollX, 0, -scrollX, 0, Math.abs(scrollX));
                        } else if (scrollX >= mMenuViewWidth / 2) { // 如果超过删除按钮一半，则打开
                            mScroller.startScroll(scrollX, 0, mMenuViewWidth - scrollX, 0, Math.abs(mMenuViewWidth - scrollX));
                        } else {    // Closed in other cases
                            mScroller.startScroll(scrollX, 0, -scrollX, 0, Math.abs(scrollX));
                        }
                        invalidate();
                    }
                    mMenuViewWidth = INVALID_CHILD_WIDTH;
                    mIsSlide = false;
                    mPosition = INVALID_POSITION;
                    releaseVelocity();  // 这里之所以会调用，是因为如果前面拦截了，就不会执行ACTION_UP,需要在这里释放追踪
                    break;
            }
            return true;
        } else {
            // When recyclerview is prevented from sliding normally here, there are still menus not closed
            closeMenu();
            // Velocity，The release here prevents recyclerview from blocking normally, but it is not released in ontouchevent;
            // There are three situations: 1. Oninterceptouchevent is not intercepted. In the oninterceptouchevent method, down and up get and release one-on-one;
            // 2. Oninterceptouchevent intercepts and gets the event from down, but the event is not handled by sideslip and needs to be released here;
            // 3. If the event is intercepted by oninterceptouchevent and acquired by down, it will be released in the up of ontouchevent if it is handled by sideslip.
            releaseVelocity();
        }
        return super.onTouchEvent(e);
    }

    private void releaseVelocity() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void obtainVelocity(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    public int pointToPosition(int x, int y) {
        if (null == getLayoutManager()) return INVALID_POSITION;
        int firstPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        Rect frame = mTouchFrame;
        if (frame == null) {
            mTouchFrame = new Rect();
            frame = mTouchFrame;
        }

        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    return firstPosition + i;
                }
            }
        }
        return INVALID_POSITION;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mFlingView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    /**
     * Close the subview that displays the submenu
     * It's implemented by itself, but it's not good for the listener to click the event because the item is not customized, so the caller needs to close it manually
     */
    public void closeMenu() {
        if (mFlingView != null && mFlingView.getScrollX() != 0) {
            mFlingView.scrollTo(0, 0);
        }
    }
}