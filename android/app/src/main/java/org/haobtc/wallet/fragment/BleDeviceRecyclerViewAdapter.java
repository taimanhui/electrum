package org.haobtc.wallet.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import org.haobtc.wallet.R;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.callback.BleConnectCallback;
import cn.com.heaton.blelibrary.ble.model.BleDevice;


public class BleDeviceRecyclerViewAdapter extends RecyclerView.Adapter<BleDeviceRecyclerViewAdapter.ViewHolder> {

    public static List<BleDevice> mValues = new ArrayList<>();
    private Context context;
    private BleConnectCallback<BleDevice> connectCallback;
    private Ble<BleDevice> mBle;
    public static BluetoothDevice device;
    public static BleDevice mBleDevice;

    public BleDeviceRecyclerViewAdapter(Context context) {
        this.context = context;
    }

    public void setConnectCallback(BleConnectCallback<BleDevice> connectCallback) {
        this.connectCallback = connectCallback;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void add(BleDevice device) {
            mValues.add(device);
            // 由于设备被连接时，会停止广播导致该设备无法被搜索到,所以要添加本APP以连接的设备到列表中
            mValues.addAll(Ble.getInstance().getConnetedDevices());
            mValues = mValues.stream().distinct().
                    filter(bleDevice -> bleDevice.getBleName() != null && bleDevice.getBleName().startsWith("BixinKEY")).collect(Collectors.toList());
            notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.bluetooth_device_list_item, parent, false);
        mBle = Ble.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.device = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getBleName() == null ?
                mValues.get(position).getBleAddress() : mValues.get(position).getBleName());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBle.stopScan();
                device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(holder.device.getBleAddress());
                mBleDevice = holder.device;
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDED:
                        if (holder.device.isConnected()) {
                            connectCallback.onReady(holder.device);
                        } else if (holder.device.isConnectting()) {
                            mBle.cancelConnectting(holder.device);
                            mBle.connect(holder.device, connectCallback);
                        } else if (holder.device.isDisconnected()) {
                            mBle.connect(holder.device, connectCallback);
                        }
                        break;
                    case BluetoothDevice.BOND_NONE:
                       boolean bond =  device.createBond();
                       if (!bond) {
                           Log.e("BLE", "无法绑定设备");
                           Toast.makeText(context, "无法绑定设备，请重启设备重试", Toast.LENGTH_SHORT).show();
                       }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public BleDevice device;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.device_alias);
        }

    }
}
