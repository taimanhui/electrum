package org.haobtc.wallet.fragment.mainwheel;


import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.CreateWalletActivity;
import org.haobtc.wallet.activities.CreateWalletPageActivity;
import org.haobtc.wallet.activities.manywallet.ManyWalletTogetherActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddViewFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_view, container, false);
        CardView cardAdd = view.findViewById(R.id.wallet_card_add);
        cardAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreateWalletActivity.class);
                getActivity().startActivity(intent);
            }
        });

        return view;
    }

}
