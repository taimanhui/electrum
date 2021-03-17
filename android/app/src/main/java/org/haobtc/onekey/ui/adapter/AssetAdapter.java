package org.haobtc.onekey.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.haobtc.onekey.R;
import org.haobtc.onekey.business.assetsLogo.AssetsLogo;
import org.haobtc.onekey.constant.Vm;
import org.haobtc.onekey.event.GetXpubEvent;

/** @author liyan */
public class AssetAdapter extends RecyclerView.Adapter<AssetAdapter.ViewHolder> {

    public List<Vm.CoinType> mValues;
    private Context context;

    public AssetAdapter(Context context, List<Vm.CoinType> list) {
        this.mValues = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_asset, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mAsset = mValues.get(position);
        holder.mIcon.setBackgroundResource(AssetsLogo.getLogoResources(holder.mAsset));

        holder.mName.setText(holder.mAsset.coinName);
        holder.mView.setOnClickListener(
                v -> {
                    EventBus.getDefault().post(new GetXpubEvent(holder.mAsset));
                });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mName;
        public final ImageView mIcon;
        public final View mView;
        public Vm.CoinType mAsset;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mName = view.findViewById(R.id.item_name);
            mIcon = view.findViewById(R.id.item_icon);
        }
    }
}
