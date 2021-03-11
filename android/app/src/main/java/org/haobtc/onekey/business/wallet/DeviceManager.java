package org.haobtc.onekey.business.wallet;

import android.content.Context;
import androidx.annotation.Nullable;
import com.google.common.base.Strings;
import org.haobtc.onekey.activities.base.MyApplication;
import org.haobtc.onekey.bean.HardwareFeatures;
import org.haobtc.onekey.manager.PreferencesManager;

/**
 * 硬件设备管理类
 *
 * @author Onekey@QuincySx
 * @create 2021-03-01 2:06 PM
 */
public class DeviceManager {

    private final Context mContext;

    public DeviceManager() {
        mContext = MyApplication.getInstance();
    }

    /**
     * 根据设备 ID 获取设备信息
     *
     * @param deviceId 设备 ID
     * @return 设备信息
     */
    @Nullable
    public HardwareFeatures getDeviceInfo(String deviceId) {
        String deviceInfo =
                PreferencesManager.get(
                                mContext, org.haobtc.onekey.constant.Constant.DEVICES, deviceId, "")
                        .toString();
        if (!Strings.isNullOrEmpty(deviceInfo)) {
            return HardwareFeatures.objectFromData(deviceInfo);
        } else {
            return null;
        }
    }

    /**
     * 根据设备名称获取设备蓝牙 MacAddress
     *
     * @param bleName 设备名称
     * @return 设备蓝牙的 MacAddress
     */
    public String getDeviceBleMacAddress(String bleName) {
        return PreferencesManager.get(
                        mContext, org.haobtc.onekey.constant.Constant.BLE_INFO, bleName, "")
                .toString();
    }
}
