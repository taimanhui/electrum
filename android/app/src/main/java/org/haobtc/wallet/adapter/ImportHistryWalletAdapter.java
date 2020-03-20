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

import java.util.ArrayList;
import java.util.List;

public class ImportHistryWalletAdapter extends RecyclerView.Adapter<ImportHistryWalletAdapter.myViewHolder> {
    private Context context;
    private ArrayList<AddBixinKeyEvent> xpubList;
    private List<Boolean> isClicks;

    public ImportHistryWalletAdapter(Context context, ArrayList<AddBixinKeyEvent> xpubList) {
        this.context = context;
        this.xpubList = xpubList;
        isClicks = new ArrayList<>();
        for (int i = 0; i < xpubList.size(); i++) {
            isClicks.add(false);
        }
//        isClicks.set(0,true);
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        TextView tet_walletname,tet_AddBixinkey;
        ImageView img_wallet;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            tet_walletname = itemView.findViewById(R.id.tet_walletname);
            tet_AddBixinkey = itemView.findViewById(R.id.tet_AddBixinkey);
            img_wallet = itemView.findViewById(R.id.img_wallet);
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
        holder.tet_walletname.setText(xpubList.get(position).getKeyname());
        holder.tet_AddBixinkey.setText(xpubList.get(position).getKeyaddress());
        if(onItemClickListener!=null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition(); // 1
                    for(int i = 0; i <isClicks.size();i++){
                        isClicks.set(i,false);
                    }
                    isClicks.set(position,true);
                    notifyDataSetChanged();
                    onItemClickListener.onItemClick(position); // 2
                }
            });
        }
        //5、记录要更改属性的控件
        holder.itemView.setTag(holder.img_wallet);
        //6、判断改变属性
        if(isClicks.get(position)){
            holder.img_wallet.setBackground(context.getDrawable(R.drawable.checkbox));
        }else{
            holder.img_wallet.setBackground(context.getDrawable(R.drawable.nocheckbox));
        }

    }

    @Override
    public int getItemCount() {
        if (xpubList != null) {
            return xpubList.size();
        } else {
            return 0;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
