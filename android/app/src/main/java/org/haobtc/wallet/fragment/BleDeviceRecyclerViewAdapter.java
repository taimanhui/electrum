package org.haobtc.wallet.fragment;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.common.StringUtils;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.MyApplication;
import org.haobtc.wallet.utils.ClsUtils;

import java.sql.Time;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.callback.BleConnectCallback;
import cn.com.heaton.blelibrary.ble.callback.BleScanCallback;
import cn.com.heaton.blelibrary.ble.model.BleDevice;


public class BleDeviceRecyclerViewAdapter extends RecyclerView.Adapter<BleDeviceRecyclerViewAdapter.ViewHolder> {

    public static List<BleDevice> mValues = new ArrayList<>();
    private Context context;
   private BleConnectCallback<BleDevice> connectCallback;
   private Ble<BleDevice> mBle;
   public BleDeviceRecyclerViewAdapter(Context context) {
        this.context = context;
    }

    public void setConnectCallback(BleConnectCallback<BleDevice> connectCallback) {
        this.connectCallback = connectCallback;
    }

    public void add(BleDevice device) {
        if (!mValues.contains(device) && device.getBleName()!= null) {
            if (device.getBleName().startsWith("BixinKEY")) {
                mValues.add(device);
                notifyDataSetChanged();
            }
        }

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
                if (holder.device.isConnected()) {
                    mBle.disconnect(holder.device);
                    mBle.connect(holder.device, connectCallback);
                } else if (holder.device.isConnectting()) {
                    mBle.cancelConnectting(holder.device);
                    mBle.connect(holder.device, connectCallback);
                } else if (holder.device.isDisconnected()) {
                    mBle.connect(holder.device, connectCallback);
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
