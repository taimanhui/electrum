package org.haobtc.wallet.fragment.mainwheel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.chaquo.python.Kwarg;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.personalwallet.hidewallet.CheckHideWalletActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.utils.Daemon;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.xpub;

public class CheckHideWalletFragment extends WheelViewpagerFragment {
    public static final String TAG = CheckHideWalletFragment.class.getSimpleName();

    @SuppressLint("CommitPrefEdits")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_check_hide_wallet, container, false);
        initview(view);
        return view;
    }

    private void initview(View view) {
        CardView cardCheckHidewallet = view.findViewById(R.id.wallet_card_add);
        cardCheckHidewallet.setOnClickListener(new View.OnClickListener() {
            @SingleClick
            @Override
            public void onClick(View v) {
                CommunicationModeSelector.runnables.clear();
                CommunicationModeSelector.runnables.add(null);
                CommunicationModeSelector.runnables.add(null);
                Intent intent = new Intent(getActivity(), CommunicationModeSelector.class);
                intent.putExtra("tag", TAG);
                startActivity(intent);
            }
        });
    }

    @Override
    public void setValue(String msgVote) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
