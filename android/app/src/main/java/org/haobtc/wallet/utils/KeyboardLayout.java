package org.haobtc.wallet.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

public class KeyboardLayout extends RelativeLayout {
    //软键盘展开
    public static final byte KEYBOARD_STATE_SHOW = -3;
    //软键盘隐藏
    public static final byte KEYBOARD_STATE_HIDE = -2;
    //初始化软键盘
    public static final byte KEYBOARD_STATE_INIT = -1;
    private boolean mHasInit;
    private boolean mHasKeybord;
    private int mHeight;
    private onKeyboaddsChangeListener mListener;
 
    private int mScreenHeight;
 
    public KeyboardLayout(Context context) {
        super(context);
        init();
    }
 
    public KeyboardLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
 
    public KeyboardLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
 
    private void init() {
        //屏幕尺寸信息
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        if (null != wm) {
            DisplayMetrics outMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(outMetrics);
            mScreenHeight = outMetrics.heightPixels;
        }
    }
 
    public void setOnkbdStateListener(onKeyboaddsChangeListener listener) {
        mListener = listener;
    }
 
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (b >= mScreenHeight) {
            //bottom的尺寸必须小于屏幕尺寸，一般来说都会小于，但测试发现，魅族手机会出现b==mScreenHeight，导致监听失败
            return;
        }
        if (!mHasInit) {
            mHasInit = true;
            mHeight = b;
            if (mListener != null) {
                mListener.onKeyBoardStateChange(KEYBOARD_STATE_INIT);
            }
        } else {
            mHeight = mHeight < b ? b : mHeight;
        }
        if (mHasInit && mHeight > b) {
            mHasKeybord = true;
            if (mListener != null) {
                mListener.onKeyBoardStateChange(KEYBOARD_STATE_SHOW);
            }
        }
        if (mHasInit && mHasKeybord && mHeight == b) {
            mHasKeybord = false;
            if (mListener != null) {
                mListener.onKeyBoardStateChange(KEYBOARD_STATE_HIDE);
            }
        }
    }
 
    public interface onKeyboaddsChangeListener {
        void onKeyBoardStateChange(int state);
    }
}