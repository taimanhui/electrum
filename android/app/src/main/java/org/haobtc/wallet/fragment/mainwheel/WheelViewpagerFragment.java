package org.haobtc.wallet.fragment.mainwheel;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.chaquo.python.PyObject;
import com.google.gson.Gson;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.ReceivedPageActivity;
import org.haobtc.wallet.activities.SendOne2OneMainPageActivity;
import org.haobtc.wallet.activities.onlywallet.CheckWalletDetailActivity;
import org.haobtc.wallet.bean.MainNewWalletBean;
import org.haobtc.wallet.utils.Daemon;

/**
 * A simple {@link Fragment} subclass.
 */
public class WheelViewpagerFragment extends Fragment {

    private TextView wallet_card_name;
    private TextView walletpersonce;
    private TextView walletBlance;

    private String name;
    private String personce;
    //    private String balance;
    private Button btnLeft;
    private Button btncenetr;
    private PyObject select_wallet;
    private TextView tetCny;
    private PyObject pyObject;
    private CardView card_view;
    private Button btn_appWallet;
    //    private Button btnright;

    private boolean isFirst = false;
    private String strSeed;
    private SharedPreferences.Editor edit;
    private SharedPreferences preferences;


    public WheelViewpagerFragment(String name, String personce) {
        this.name = name;
        this.personce = personce;
//        this.balance = balance;
    }

    public WheelViewpagerFragment(String name, String personce, boolean isFirst) {
        this.isFirst = isFirst;
        this.name = name;
        this.personce = personce;
//        this.balance = balance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wheel_viewpager, container, false);
        preferences = getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        edit = preferences.edit();
        strSeed = preferences.getString("strSeed", "");
        String strScrollName = preferences.getString(name, "");
        wallet_card_name = view.findViewById(R.id.wallet_card_name);
        walletpersonce = view.findViewById(R.id.wallet_card_tv2);
        walletBlance = view.findViewById(R.id.wallet_card_tv4);

        btnLeft = view.findViewById(R.id.wallet_card_bn1);
        btncenetr = view.findViewById(R.id.wallet_card_bn2);
        btn_appWallet = view.findViewById(R.id.app_wallet);
        tetCny = view.findViewById(R.id.tet_Cny);
        card_view = view.findViewById(R.id.wallet_card);
        init();
        initdata();

        if (isFirst) refreshList();

        return view;
    }

    private void init() {
        wallet_card_name.setText(name);
        if (!TextUtils.isEmpty(personce)) {
            if (personce.equals("standard")) {
                btn_appWallet.setVisibility(View.VISIBLE);
                walletpersonce.setVisibility(View.GONE);
            } else {
                String of = personce.replaceAll("of", "/");
                walletpersonce.setText(of);
            }
        }

    }

    private void initdata() {
        btnLeft.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SendOne2OneMainPageActivity.class);
            intent.putExtra("wallet_name", name);
            intent.putExtra("wallet_type", personce);
            startActivity(intent);
        });
        btncenetr.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ReceivedPageActivity.class);
            startActivity(intent);
        });
        card_view.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CheckWalletDetailActivity.class);
            intent.putExtra("wallet_name", name);
            startActivity(intent);
        });

    }

    public void refreshList() {
        if (personce.equals("standard")){
            String strScrollPass = preferences.getString(name, "");
            Log.i("refreshList", "---: "+strScrollPass);
            if (TextUtils.isEmpty(strScrollPass)){
                View view1 = LayoutInflater.from(getActivity()).inflate(R.layout.input_wallet_pass, null, false);
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setView(view1).create();
                EditText str_pass = view1.findViewById(R.id.edit_password);
                view1.findViewById(R.id.btn_enter_wallet).setOnClickListener(v -> {
                    String strPassword = str_pass.getText().toString();
                    if (TextUtils.isEmpty(strPassword)){
                        Toast.makeText(getActivity(), getResources().getString(R.string.please_input_pass), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        Daemon.commands.callAttr("load_wallet", name, strPassword);
                        getWalletMsg();
                        edit.putString(name,strPassword);
                        edit.apply();
                    } catch (Exception e) {
                        Log.i("lllllllll", "json++: "+e.getMessage());
                        if (e.getMessage().toString().contains("Incorrect password")){
                            Toast.makeText(getActivity(),getResources().getString(R.string.wrong_pass), Toast.LENGTH_SHORT).show();
                        }
                        e.printStackTrace();
                    }
                    alertDialog.dismiss();


                });
                view1.findViewById(R.id.cancel_select_wallet).setOnClickListener(v -> {
                    Toast.makeText(getActivity(), getResources().getString(R.string.msg_get_wrong), Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                });
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }else{
                try {
                    Daemon.commands.callAttr("load_wallet", name,strScrollPass);
                    getWalletMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }else{
            try {
                Daemon.commands.callAttr("load_wallet", name);
                getWalletMsg();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //get wallet message
    public void getWalletMsg(){
        try {
            select_wallet = Daemon.commands.callAttr("select_wallet", name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (select_wallet != null) {
            String toString = select_wallet.toString();
            Log.i("select_wallet", "select_wallet+++: " + toString);
            Gson gson = new Gson();
            MainNewWalletBean mainWheelBean = gson.fromJson(toString, MainNewWalletBean.class);
            String walletType = mainWheelBean.getWalletType();
            String balanceC = mainWheelBean.getBalance();
            if (!TextUtils.isEmpty(walletType)) {
                String streplaceC = walletType.replaceAll("of", "/");
                walletpersonce.setText(streplaceC);
            }

            wallet_card_name.setText(name);

            if (!TextUtils.isEmpty(balanceC)) {
                if (balanceC.contains("(")) {
                    String substring = balanceC.substring(0, balanceC.indexOf("("));
                    walletBlance.setText(substring);
                } else {
                    walletBlance.setText(balanceC);
                }
            }
        }
    }

}
