package org.haobtc.wallet.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.haobtc.wallet.R;
import org.haobtc.wallet.fragment.mainwheel.WheelViewpagerFragment;

import butterknife.BindView;


public class BluetoothFragment extends Fragment implements OnRefreshListener {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    private BleDeviceRecyclerViewAdapter bleDeviceRecyclerViewAdapter;

    public BluetoothFragment(BleDeviceRecyclerViewAdapter adapter) {
        this.bleDeviceRecyclerViewAdapter = adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bluetooth_device_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.device_list);
        SmartRefreshLayout refreshLayout = view.findViewById(R.id.smart_RefreshLayout);
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setEnableRefresh(true);
        refreshLayout.setOnRefreshListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(bleDeviceRecyclerViewAdapter);

        return view;
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        Toast.makeText(getActivity(), "刷新蓝牙列表", Toast.LENGTH_SHORT).show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.finishRefresh();
            }
        }, 300);

    }
}
