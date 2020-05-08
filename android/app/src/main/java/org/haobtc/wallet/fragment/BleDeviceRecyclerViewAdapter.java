package org.haobtc.wallet.fragment;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.service.BleService;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.event.ConnectingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.model.BleDevice;


public class BleDeviceRecyclerViewAdapter extends RecyclerView.Adapter<BleDeviceRecyclerViewAdapter.ViewHolder> {

    public static List<BleDevice> mValues = new ArrayList<>();
    public static BleDevice mBleDevice;
    private Context context;

    public BleDeviceRecyclerViewAdapter(Context context) {
        this.context = context;
    }


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
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.device = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getBleName() == null ?
                mValues.get(position).getBleAddress() : mValues.get(position).getBleName());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @SingleClick(value = 2000)
            @Override
            public void onClick(View v) {
                mBleDevice = holder.device;
                EventBus.getDefault().post(new ConnectingEvent());
                context.startService(new Intent(context, BleService.class));
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
