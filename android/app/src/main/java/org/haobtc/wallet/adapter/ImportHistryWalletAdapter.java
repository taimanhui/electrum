package org.haobtc.wallet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.haobtc.wallet.R;
import java.util.ArrayList;

public class ImportHistryWalletAdapter extends RecyclerView.Adapter<ImportHistryWalletAdapter.myViewHolder> {
    private Context context;
    private ArrayList<String> strings;
    private int mSelectedPos = -1;

    public ImportHistryWalletAdapter(Context context, ArrayList<String> strings) {
        this.context = context;
        this.strings = strings;
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        TextView tet_walletname;
        CheckBox chkbox_wallet;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            tet_walletname = itemView.findViewById(R.id.tet_walletname);
            chkbox_wallet = itemView.findViewById(R.id.chkbox_wallet);
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
        holder.tet_walletname.setText(strings.get(position));
        holder.chkbox_wallet.setChecked(mSelectedPos == position);
        holder.chkbox_wallet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(context, position+"", Toast.LENGTH_SHORT).show();
                if (mSelectedPos != position) {
                    holder.chkbox_wallet.setChecked(true);
                    if (mSelectedPos != -1) {
                        notifyItemChanged(mSelectedPos, 0);
                    }
                    mSelectedPos = position;
                }
            }
        });

    }
    public int getSelectedPos(){
        return mSelectedPos;
    }

    @Override
    public int getItemCount() {
        if (strings != null) {
            return strings.size();
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
