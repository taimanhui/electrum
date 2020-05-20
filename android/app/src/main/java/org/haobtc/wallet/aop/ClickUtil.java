package org.haobtc.wallet.aop;

import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.R;
import org.haobtc.wallet.event.ExistEvent;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.ble;
import static org.haobtc.wallet.activities.service.CommunicationModeSelector.nfc;

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
        if (viewId == R.id.img_back || viewId == R.id.img_cancel) {
            EventBus.getDefault().post(new ExistEvent());
            if (ble != null) {
                ble.put("IS_CANCEL", true);
                nfc.put("IS_CANCEL", true);
            }
            return false;
        }
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
