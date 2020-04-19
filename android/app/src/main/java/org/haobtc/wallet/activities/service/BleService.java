package org.haobtc.wallet.activities.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.R;
import org.haobtc.wallet.event.ConnectingEvent;
import org.haobtc.wallet.event.HandlerEvent;
import org.haobtc.wallet.fragment.BleDeviceRecyclerViewAdapter;
import org.haobtc.wallet.utils.CommonUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.callback.BleConnectCallback;
import cn.com.heaton.blelibrary.ble.callback.BleNotiftCallback;
import cn.com.heaton.blelibrary.ble.callback.BleWriteCallback;
import cn.com.heaton.blelibrary.ble.model.BleDevice;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.ble;
import static org.haobtc.wallet.activities.service.CommunicationModeSelector.bleHandler;
import static org.haobtc.wallet.activities.service.CommunicationModeSelector.bleTransport;
import static org.haobtc.wallet.activities.service.CommunicationModeSelector.isDfu;
import static org.haobtc.wallet.activities.service.CommunicationModeSelector.isNFC;
import static org.haobtc.wallet.activities.service.CommunicationModeSelector.nfcTransport;
import static org.haobtc.wallet.activities.service.CommunicationModeSelector.usb;

public class BleService extends Service {
    private Ble<BleDevice> mBle;
    public static final String TAG = BleService.class.getSimpleName();
    private BluetoothDevice bluetoothDevice;
    private static boolean isErrorOccurred;
    private final IntentFilter bondFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
    private boolean isBonded;
    private BleDevice mBleDevice;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mBle = Ble.getInstance();
        registerReceiver(broadcastReceiver, bondFilter);

    }

    private final BleWriteCallback<BleDevice> writeCallBack = new BleWriteCallback<BleDevice>() {

        @Override
        public void onWriteSuccess(BleDevice device, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "send successful:" + CommonUtils.bytes2hex(characteristic.getValue()));
            ble.put("WRITE_SUCCESS", true);
        }
    };



    private final BleConnectCallback<BleDevice> connectCallback = new BleConnectCallback<BleDevice>() {
        @Override
        public void onConnectionChanged(BleDevice device) {
            if (device.isConnectting()) {
                Log.i(TAG, "connecting---" + device.getBleName());
                EventBus.getDefault().post(new ConnectingEvent());
            }
            if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                return;
            }
            if (device.isConnected()) {
                Log.i(TAG, "connected---" + device.getBleName());
            }
        }

        @Override
        public void onServicesDiscovered(BleDevice device, List<BluetoothGattService> gattServices) {
            super.onServicesDiscovered(device, gattServices);
            setNotify(device);
        }

        @Override
        public void onReady(BleDevice device) {
            super.onReady(device);
            EventBus.getDefault().post(new ConnectingEvent());
            isNFC = false;
            bleTransport.put("ENABLED", true);
            nfcTransport.put("ENABLED", false);
            usb.put("ENABLED", false);
            bleHandler.put("BLE", mBle);
            bleHandler.put("BLE_DEVICE", device);
            bleHandler.put("CALL_BACK", writeCallBack);
            for (;;) {
                BluetoothGattCharacteristic characteristic = mBle.getBleRequest().getReadCharacteristic(bluetoothDevice.getAddress());
                BluetoothGattDescriptor notify = null;
                if (characteristic != null) {
                    notify = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                }
                boolean isNotify = false;
                if (notify != null) {
                    isNotify = Arrays.equals(notify.getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                }
                if (isNotify) {
                    new Handler().postDelayed(() -> EventBus.getDefault().post(new HandlerEvent()), isBonded ? 3000 : 1600);
                    break;
                } else {
                    setNotify(mBleDevice);
                }
                if (isErrorOccurred) {
                    isErrorOccurred = false;
                    break;
                }
            }
        }

        @Override
        public void onConnectException(BleDevice device, int errorCode) {
            super.onConnectException(device, errorCode);
            Log.e(TAG, String.format("连接异常，异常状态码: %d", errorCode));
            isErrorOccurred = true;
            if (isDfu) {
                return;
            }
            switch (errorCode) {
                case 2523:
                    Toast.makeText(BleService.this, getString(R.string.bluetooth_abnormal), Toast.LENGTH_LONG).show();
/*
                    mBle.connect(device, connectCallback);
*/
                    break;
                case 133:
                case 8:
                case 2521:
                case 59:
                    Toast.makeText(BleService.this, getString(R.string.bluetooth_fail), Toast.LENGTH_LONG).show();
                    stopSelf();
                    break;
                default:
            }
        }

        @Override
        public void onConnectCancel(BleDevice device) {
            super.onConnectCancel(device);
            Log.i(TAG, "正在取消连接");

        }

        @Override
        public void onConnectTimeOut(BleDevice device) {
            super.onConnectTimeOut(device);
            Log.e(TAG, String.format("连接设备==%s超时", device.getBleName()));
            stopSelf();
        }
    };
    private void setNotify(BleDevice device) {
        /*Set up notifications when the connection is successful*/
        mBle.enableNotify(device, true, new BleNotiftCallback<BleDevice>() {
            @Override
            public void onChanged(BleDevice device, BluetoothGattCharacteristic characteristic) {
                Log.e(TAG, "receive from hardware: " + CommonUtils.bytes2hex(characteristic.getValue()));
                bleHandler.put("RESPONSE", characteristic.getValue());
            }

            @Override
            public void onNotifySuccess(BleDevice device) {
                super.onNotifySuccess(device);
                Log.d(TAG, "onNotifySuccess: " + device.getBleName());
            }

            @Override
            public void onNotifyCanceled(BleDevice device) {
                super.onNotifyCanceled(device);
            }
        });
    }

        @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            if (mBle.isScanning()) {
                mBle.stopScan();
            }
            mBleDevice = BleDeviceRecyclerViewAdapter.mBleDevice;
            bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mBleDevice.getBleAddress());
            switch (bluetoothDevice.getBondState()) {
                case BluetoothDevice.BOND_BONDED:
                    if (mBleDevice.isConnected()) {
                        connectCallback.onReady(mBleDevice);
                    } else if (mBleDevice.isConnectting()) {
                        mBle.cancelConnectting(mBleDevice);
                        mBle.connect(mBleDevice, connectCallback);
                    } else if (mBleDevice.isDisconnected()) {
                        mBle.connect(mBleDevice, connectCallback);
                    }
                    break;
                case BluetoothDevice.BOND_NONE:
                    boolean bond =  bluetoothDevice.createBond();
                    if (!bond) {
                        Log.e("BLE", "无法绑定设备");
                        Toast.makeText(this, "无法绑定设备，请重启设备重试", Toast.LENGTH_SHORT).show();
                    }
            }
        return Service.START_NOT_STICKY;
    }
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice  device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.i(TAG, "receive broadcast===" + action);
            if (bluetoothDevice == null || device == null || !device.getAddress().equals(bluetoothDevice.getAddress())) {
                return;
            }
            if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                EventBus.getDefault().post(new ConnectingEvent());
            }
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                if (bluetoothDevice != null) {
                    if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                        if (mBleDevice.isConnected()) {
                            connectCallback.onReady(mBleDevice);
                        } else {
                            mBle.connect(mBleDevice, connectCallback);
                        }
                        isBonded = true;
                    }
                }
            }
        }
    };
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
