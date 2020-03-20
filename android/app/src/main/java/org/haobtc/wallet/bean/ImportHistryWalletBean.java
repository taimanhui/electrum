package org.haobtc.wallet.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ImportHistryWalletBean {


    @SerializedName("Walltes")
    private List<WalltesBean> Walltes;

    public List<WalltesBean> getWalltes() {
        return Walltes;
    }

    public void setWalltes(List<WalltesBean> Walltes) {
        this.Walltes = Walltes;
    }

    public static class WalltesBean {
        /**
         * id : 5
         * xpubId : Vpub5jfMA3ceJzUMo2kz6vS7ywiSs5VbgGJDT8eHchN5cc6sGDkJwSkJmqvXn4fRijAESoWiZE2Q78ErXjHucpdVFUUawU1tRgvBGjncALBNpXC
         * WalletId : 41e7b5257ed79f40dcc82867db2cf8099bbdccb53268822c754c4a6bc9884384
         * Xpubs : ["Vpub5jfMA3ceJzUMo2kz6vS7ywiSs5VbgGJDT8eHchN5cc6sGDkJwSkJmqvXn4fRijAESoWiZE2Q78ErXjHucpdVFUUawU1tRgvBGjncALBNpXC", "Vpub5gyCX33B53xAyfEaH1Jfnp5grizbHfxVz6bWLPD92nLcbKMsQzSbM2eyGiK4qiRziuoRhoeVMoPLvEdfbQxGp88PN9cU6zupSSuiPi3RjEg"]
         * WalletType : 2-2
         */

        @SerializedName("id")
        private int id;
        @SerializedName("xpubId")
        private String xpubId;
        @SerializedName("WalletId")
        private String WalletId;
        @SerializedName("Xpubs")
        private String Xpubs;
        @SerializedName("WalletType")
        private String WalletType;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getXpubId() {
            return xpubId;
        }

        public void setXpubId(String xpubId) {
            this.xpubId = xpubId;
        }

        public String getWalletId() {
            return WalletId;
        }

        public void setWalletId(String WalletId) {
            this.WalletId = WalletId;
        }

        public String getXpubs() {
            return Xpubs;
        }

        public void setXpubs(String Xpubs) {
            this.Xpubs = Xpubs;
        }

        public String getWalletType() {
            return WalletType;
        }

        public void setWalletType(String WalletType) {
            this.WalletType = WalletType;
        }
    }
}
