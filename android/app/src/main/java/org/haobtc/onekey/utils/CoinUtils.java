package org.haobtc.onekey.utils;
import org.haobtc.onekey.R;
import org.haobtc.onekey.bean.CoinBean;

import java.util.ArrayList;
import java.util.List;

public final class CoinUtils {

    private CoinUtils() {
    }

    public static List<CoinBean> getSupportCoins() {
        CoinBean btc = new CoinBean(R.drawable.token_btc, R.string.coin_btc);
        List<CoinBean> list = new ArrayList<>();
        list.add(btc);
        return list;
    }
}
