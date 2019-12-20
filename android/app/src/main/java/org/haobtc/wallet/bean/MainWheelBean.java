package org.haobtc.wallet.bean;

import java.util.List;

public class MainWheelBean {

    private List<WalletsBean> wallets;

    public List<WalletsBean> getWallets() {
        return wallets;
    }

    public void setWallets(List<WalletsBean> wallets) {
        this.wallets = wallets;
    }

    public static class WalletsBean {
        /**
         * wallet_type : 2of2
         * balance : 110. mBTC
         * name : ghhh
         */

        private String wallet_type;
        private String balance;
        private String name;

        public String getWallet_type() {
            return wallet_type;
        }

        public void setWallet_type(String wallet_type) {
            this.wallet_type = wallet_type;
        }

        public String getBalance() {
            return balance;
        }

        public void setBalance(String balance) {
            this.balance = balance;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
