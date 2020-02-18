package org.haobtc.wallet.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.bean.CNYBean;

import java.util.ArrayList;
import java.util.List;

public class CNYAdapter extends RecyclerView.Adapter<CNYAdapter.myViewHolder> {
    private Context context;
    private ArrayList<CNYBean> listCNY;

    public CNYAdapter(Context context, ArrayList<CNYBean> listCNY,int cnypos) {
        this.context = context;
        this.listCNY = listCNY;
        this.listCNY.get(cnypos).setStatus(true);
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        TextView tet_Cny;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            tet_Cny = itemView.findViewById(R.id.tet_Cny);
        }
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.cny_item, null);
        return new myViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        holder.tet_Cny.setText(listCNY.get(position).getName());

        holder.tet_Cny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < listCNY.size(); i++) {
                    listCNY.get(i).setStatus(false);
                }
                listCNY.get(position).setStatus(true);
                Log.i("onClickJXM", "onClick: "+listCNY.get(position).isStatus());
                onLisennorClick.ItemClick(position);
                notifyDataSetChanged();

            }
        });
        if (listCNY.get(position).isStatus()) {
            holder.tet_Cny.setTextColor(context.getColor(R.color.button_bk_disableok));
//            holder.tet_types.setBackgroundColor(Color.parseColor("#ffffff"));
        } else {
            holder.tet_Cny.setTextColor(context.getColor(R.color.text));
        }
    }

    @Override
    public int getItemCount() {
        return listCNY.size();
    }

    public interface onLisennorClick {
        void ItemClick(int pos);
    }

    private onLisennorClick onLisennorClick;

    public void setOnLisennorClick(onLisennorClick onLisennorClick) {
        this.onLisennorClick = onLisennorClick;
    }

}
