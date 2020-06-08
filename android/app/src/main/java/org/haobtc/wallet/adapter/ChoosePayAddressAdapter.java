package org.haobtc.wallet.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.bean.AddressEvent;

import java.util.ArrayList;
import java.util.List;

public class ChoosePayAddressAdapter extends RecyclerView.Adapter<ChoosePayAddressAdapter.myViewHolder> {

    private Context context;
    private List<Boolean> isClicks;
    private List<AddressEvent> data;

    public ChoosePayAddressAdapter(Context context, List<AddressEvent> data) {
        this.context = context;
        this.data = data;

        isClicks = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            isClicks.add(false);
        }
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        TextView tet_WalletName;
        TextView tet_WalletType;
        RelativeLayout rel_background;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            tet_WalletName = itemView.findViewById(R.id.tet_WalletName);
            tet_WalletType = itemView.findViewById(R.id.tet_WalletType);
            rel_background = itemView.findViewById(R.id.rel_background);
        }
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.chooseaddress, null);
        return new myViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        holder.tet_WalletName.setText(data.get(position).getName());
        if ("standard".equals(data.get(position).getType())) {
            holder.tet_WalletType.setText(R.string.software_wallet);
        } else {
            holder.tet_WalletType.setText(data.get(position).getType());
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getLayoutPosition(); // 1
                for (int i = 0; i < isClicks.size(); i++) {
                    isClicks.set(i, false);
                }
                isClicks.set(position, true);
                notifyDataSetChanged();
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position); // 2
                }
            }
        });

        holder.itemView.setTag(holder.tet_WalletName);
        holder.itemView.setTag(holder.tet_WalletType);

        if (isClicks.get(position)) {
            holder.rel_background.setBackgroundColor(Color.parseColor("#F6F7F8"));
        } else {
            holder.rel_background.setBackgroundColor(Color.parseColor("#ffffff"));
        }

    }

    @Override
    public int getItemCount() {
        if (data != null) {
            return data.size();
        } else {
            return 0;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

}