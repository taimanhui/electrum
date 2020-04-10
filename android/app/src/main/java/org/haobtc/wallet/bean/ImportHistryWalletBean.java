package org.haobtc.wallet.bean;

import com.google.gson.annotations.SerializedName;

public class ImportHistryWalletBean {


    /**
     * xpubId : Vpub5kKD1ggSEaNt9MHbaoorY2r4xdBA57GvAfujHYxuqhpkDabmd6DRPWz45Zt1FFBdYzSz8y2aAZJLFpFsUvf7G7BHhQD27CgUvTy1U1P8jmD
     * walletId : 0847a3e0cb364f581fc73a3eaf95a0141cc6a6b3b85d110afd983f0dc9f531d2
     * xpubs : ["Vpub5kKD1ggSEaNt9MHbaoorY2r4xdBA57GvAfujHYxuqhpkDabmd6DRPWz45Zt1FFBdYzSz8y2aAZJLFpFsUvf7G7BHhQD27CgUvTy1U1P8jmD", "Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41vMKQbeHveRvuThAmm"]
     * walletType : 2-2
     */

    @SerializedName("xpubId")
    private String xpubId;
    @SerializedName("walletId")
    private String walletId;
    @SerializedName("xpubs")
    private String xpubs;
    @SerializedName("walletType")
    private String walletType;

    public String getXpubId() {
        return xpubId;
    }

    public void setXpubId(String xpubId) {
        this.xpubId = xpubId;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getXpubs() {
        return xpubs;
    }

    public void setXpubs(String xpubs) {
        this.xpubs = xpubs;
    }

    public String getWalletType() {
        return walletType;
    }

    public void setWalletType(String walletType) {
        this.walletType = walletType;
    }
}
