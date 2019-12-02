package org.haobtc.wallet.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.haobtc.wallet.MainActivity;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.CreateWalletActivity;
import org.haobtc.wallet.activities.ReceivedPageActivity;
import org.haobtc.wallet.activities.SendOne2OneMainPageActivity;
import org.haobtc.wallet.activities.SignaturePageActivity;
import org.haobtc.wallet.adapter.locateimplements.LocateCenterHorizontalView;

import java.util.ArrayList;

public class MainWheelPlantAdapetr extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements LocateCenterHorizontalView.IAutoLocateHorizontalView {
    private final int CHECK_TYPE = 1;
    private final int CHECK_NOTYPE = 2;
    private Context context;
    private ArrayList<String> dataList;
    private ArrayList<String> dataListName = new ArrayList<>();
    private ArrayList<String> dataListBlance = new ArrayList<>();
    private View inflateAll;

    public MainWheelPlantAdapetr(Context context, ArrayList<String> dataList, ArrayList<String> dataListName, ArrayList<String> dataListBlance) {
        this.context = context;
        this.dataList = dataList;
        this.dataListName = dataListName;
        this.dataListBlance = dataListBlance;
    }

    @Override
    public View getItemView() {
        return inflateAll;
    }

    @Override
    public void onViewSelected(boolean isSelected, int pos, RecyclerView.ViewHolder holder, int itemWidth) {
        if (isSelected){
            Toast.makeText(context, "pos===  "+pos, Toast.LENGTH_SHORT).show();
        }
    }

    public class myViewHolderMore extends RecyclerView.ViewHolder {
        Button button_send, button_receive, button_signature;
        TextView tetMoneyname,tetPersonce,tetBlance;
        CardView wallet_card;

        public myViewHolderMore(@NonNull View view) {
            super(view);
            button_send = view.findViewById(R.id.wallet_card_bn1);
            button_receive = view.findViewById(R.id.wallet_card_bn2);
            button_signature = view.findViewById(R.id.wallet_card_bn3);
            tetMoneyname = view.findViewById(R.id.wallet_card_tv1);
            tetPersonce = view.findViewById(R.id.wallet_card_tv2);
            tetBlance = view.findViewById(R.id.wallet_card_tv4);

            button_send.setOnClickListener(v -> {
                Intent intent = new Intent(context, SendOne2OneMainPageActivity.class);
                context.startActivity(intent);
            });
            button_receive.setOnClickListener(v -> {
                Intent intent = new Intent(context, ReceivedPageActivity.class);
                context.startActivity(intent);
            });
            button_signature.setOnClickListener(v -> {
                Intent intent = new Intent(context, SignaturePageActivity.class);
                context.startActivity(intent);
            });

        }

    }

    public class myViewHolderAdd extends RecyclerView.ViewHolder {
        ImageView imgAdd;

        public myViewHolderAdd(@NonNull View itemView) {
            super(itemView);
            imgAdd = itemView.findViewById(R.id.img_Addmoney);

        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case CHECK_TYPE:
                inflateAll = LayoutInflater.from(context).inflate(R.layout.wallet_card, null);
                return new myViewHolderMore(inflateAll);

            case CHECK_NOTYPE:
                return new myViewHolderAdd(LayoutInflater.from(context).inflate(R.layout.wallet_card_add, null));

        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof myViewHolderMore) {
            ((myViewHolderMore) holder).tetMoneyname.setText(dataListName.get(position));
            String tetperxon = dataList.get(position);
            String streplace = tetperxon.replaceAll("of", "/");
            ((myViewHolderMore) holder).tetPersonce.setText(streplace);
            ((myViewHolderMore) holder).tetBlance.setText(dataListBlance.get(position));



        } else if (holder instanceof myViewHolderAdd) {
            ((myViewHolderAdd) holder).imgAdd.setOnClickListener(view -> {
                Intent intent6 = new Intent(context, CreateWalletActivity.class);
                context.startActivity(intent6);

            });
        }
    }

    //Realize multi layout mode
    @Override
    public int getItemViewType(int position) {
        if (position != dataList.size() - 1) {
            return CHECK_TYPE;
        } else {
            return CHECK_NOTYPE;

        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public interface onItemonClicklisoner{
        void onOnCLick(int pos);
    }

    public void setOnItemonClicklisoner(MainWheelPlantAdapetr.onItemonClicklisoner onItemonClicklisoner) {
        this.onItemonClicklisoner = onItemonClicklisoner;
    }

    private onItemonClicklisoner onItemonClicklisoner;



}
