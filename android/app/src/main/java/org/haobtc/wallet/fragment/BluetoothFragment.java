package org.haobtc.wallet.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.haobtc.wallet.R;


public class BluetoothFragment extends Fragment {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    private BleDeviceRecyclerViewAdapter  bleDeviceRecyclerViewAdapter;
    public BluetoothFragment(BleDeviceRecyclerViewAdapter adapter) {
        this.bleDeviceRecyclerViewAdapter = adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bluetooth_device_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.device_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(bleDeviceRecyclerViewAdapter);
        return view;
    }

}
