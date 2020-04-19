package org.haobtc.wallet.fragment.mainwheel;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.personalwallet.hidewallet.HideWalletActivity;
import org.haobtc.wallet.aop.SingleClick;

/**
 * .
 */
public class CheckHideWalletFragment extends WheelViewpagerFragment {

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
                Intent intent = new Intent(getActivity(), HideWalletActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    public void setValue(String msgVote) {
    }
}
