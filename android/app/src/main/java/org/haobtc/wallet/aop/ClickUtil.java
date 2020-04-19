package org.haobtc.wallet.aop;

import android.view.View;

public class ClickUtil {
    /**
     * the last click time
     */
    private static long mLastClickTime;
    /**
     * the view of id last clicked
     */
    private static int mLastClickViewId;

    /**
     * is fast click
     *
     * @param v  the view clicked
     * @param intervalMillis  interval（ms）
     * @return true  for fast click
     */
    public static boolean isFastDoubleClick(View v, long intervalMillis) {
        int viewId = v.getId();
        long time = System.currentTimeMillis();
        long timeInterval = Math.abs(time - mLastClickTime);
        if (timeInterval < intervalMillis && viewId == mLastClickViewId) {
            return true;
        } else {
            mLastClickTime = time;
            mLastClickViewId = viewId;
            return false;
        }
    }
}
