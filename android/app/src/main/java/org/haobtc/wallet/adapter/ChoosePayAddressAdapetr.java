package org.haobtc.wallet.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.haobtc.wallet.R;
import org.haobtc.wallet.bean.AddressEvent;

import java.util.ArrayList;
import java.util.List;

public class ChoosePayAddressAdapetr extends RecyclerView.Adapter<ChoosePayAddressAdapetr.myViewHolder> {

    private Context context;
    private List<Boolean> isClicks;
    private List<AddressEvent> data;

    public ChoosePayAddressAdapetr(Context context, List<AddressEvent> data) {
        this.context = context;
        this.data = data;

        isClicks = new ArrayList<>();
        for(int i = 0;i<data.size();i++){
            isClicks.add(false);
        }
    }

    public class myViewHolder extends RecyclerView.ViewHolder{
        TextView tet_WalletName;
        TextView tet_WalletType;
        RelativeLayout rel_background;
        View view_line;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            tet_WalletName = itemView.findViewById(R.id.tet_WalletName);
            tet_WalletType = itemView.findViewById(R.id.tet_WalletType);
            rel_background = itemView.findViewById(R.id.rel_background);
            view_line = itemView.findViewById(R.id.view_line);
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
        String streplace = data.get(position).getType().replaceAll("of", "/");
        holder.tet_WalletName.setText(data.get(position).getName());
        holder.tet_WalletType.setText(streplace);

        if(mOnItemClickListener!=null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition(); // 1
                    for(int i = 0; i <isClicks.size();i++){
                        isClicks.set(i,false);
                    }
                    isClicks.set(position,true);
                    notifyDataSetChanged();
                    mOnItemClickListener.onItemClick(position); // 2
                }
            });
        }
        holder.itemView.setTag(holder.tet_WalletName);
        holder.itemView.setTag(holder.tet_WalletType);

        if(isClicks.get(position)){
            holder.tet_WalletName.setTextColor(Color.parseColor("#ffffff"));
            holder.tet_WalletType.setTextColor(Color.parseColor("#ffffff"));
            holder.rel_background.setBackgroundColor(Color.parseColor("#6182F5"));
            holder.view_line.setBackgroundColor(Color.parseColor("#6182F5"));
        }else{
            holder.tet_WalletName.setTextColor(Color.parseColor("#494949"));
            holder.tet_WalletType.setTextColor(Color.parseColor("#6182F5"));
            holder.rel_background.setBackgroundColor(Color.parseColor("#ffffff"));
            holder.view_line.setBackgroundColor(Color.parseColor("#f1f1f1"));
        }

    }

    @Override
    public int getItemCount() {
        if (data!=null){
            return data.size();
        }else{
            return 0;
        }

    }

    //7、定义点击事件回调接口
    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    //2、定义监听并设set方法
    private OnItemClickListener mOnItemClickListener;

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

}