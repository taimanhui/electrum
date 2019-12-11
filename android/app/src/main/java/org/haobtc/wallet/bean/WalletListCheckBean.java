package org.haobtc.wallet.bean;

public class WalletListCheckBean {

    /**
     * wallet_type : 2of2
     * balance : 0. mBTC
     * name : xiaomi
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
