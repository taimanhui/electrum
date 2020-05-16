package org.haobtc.wallet.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.event.AddBixinKeyEvent;
import org.haobtc.wallet.event.InputHistoryWalletEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportHistryWalletAdapter extends RecyclerView.Adapter<ImportHistryWalletAdapter.myViewHolder> {
    private Context context;
    private ArrayList<InputHistoryWalletEvent> xpubList;
    private Map<Integer, Boolean> checkStatus;

    public ImportHistryWalletAdapter(Context context, ArrayList<InputHistoryWalletEvent> xpubList) {
        this.context = context;
        this.xpubList = xpubList;
        initData();

    }

    private void initData() {
        checkStatus = new HashMap<>();
        for (int i = 0; i < xpubList.size(); i++) {
            checkStatus.put(i, false);//
        }
    }

    //Get the final map storage data
    public Map<Integer, Boolean> getMap() {
        return checkStatus;
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        TextView tet_walletname, tet_AddBixinkey;
        CheckBox checkbox;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            tet_walletname = itemView.findViewById(R.id.tet_walletname);
            tet_AddBixinkey = itemView.findViewById(R.id.tet_AddBixinkey);
            checkbox = itemView.findViewById(R.id.img_wallet);
        }
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.histrywallet_item, null);
        return new myViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        holder.tet_walletname.setText(xpubList.get(position).getName());
        holder.tet_AddBixinkey.setText(xpubList.get(position).getXpubs());
        holder.checkbox.setOnCheckedChangeListener(null);
        holder.checkbox.setChecked(checkStatus.get(position));
        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkStatus.put(position, isChecked);
            }
        });
        if (checkStatus.get(position) == null) {
            checkStatus.put(position, false);
        }
    }

    @Override
    public int getItemCount() {
        return xpubList == null ? 0 : xpubList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
