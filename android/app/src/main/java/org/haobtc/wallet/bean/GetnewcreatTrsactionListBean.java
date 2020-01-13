package org.haobtc.wallet.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetnewcreatTrsactionListBean {


    /**
     * txid : 5e4503a857eb0c89ccf0f0a60f35e0a964ed057efa2de0594eebda1fb160d4ad
     * can_broadcast : false
     * amount : 0 mBTC
     * fee : 0.001 mBTC
     * description : 明天会更好
     * tx_status : Unsigned
     * sign_status : [0,2]
     * output_addr : [{"addr":"bcrt1qnuh3qc9g6lwlqqvmf7hg05pzlujhua9emdqdty4znjstr5886paqhwp25r","amount":"0.0001 mBTC"},{"addr":"bcrt1q2tfva7ak5t0zkp80dpgjdyx8n9fqs0mregtcjuj39twck6eup7vqycfnw8","amount":"100000.1067 mBTC"}]
     * cosigner : ["Vpub5gyCX33B53xAyfEaH1Jfnp5grizbHfxVz6bWLPD92nLcbKMsQzSbM2eyGiK4qiRziuoRhoeVMoPLvEdfbQxGp88PN9cU6zupSSuiPi3RjEg","Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41vMKQbeHveRvuThAmm"]
     * tx : 70736274ff0100fd2302020000000b7f863c1bf2b175be80895db2d857ee4183505f8d2c6705d37ece9d01fc8d320c0100000000fdffffff19f1208bb1a4cd3b9f7780e17b277066dfe0cda9df0acc1cca2ca667a402d3190000000000fdffffffc1d534e912affdb7adf3f1bbc0038cf572e49c4d882d4c1595b9a0000022020392952503489544bca7417275720a5fe98ba94d8bc69fd899b99d3894bc30406c0cbc21edd1010000000000000000
     */

    @SerializedName("txid")
    private String txid;
    @SerializedName("can_broadcast")
    private boolean canBroadcast;
    @SerializedName("amount")
    private String amount;
    @SerializedName("fee")
    private String fee;
    @SerializedName("description")
    private String description;
    @SerializedName("tx_status")
    private String txStatus;
    @SerializedName("tx")
    private String tx;
    @SerializedName("sign_status")
    private List<Integer> signStatus;
    @SerializedName("output_addr")
    private List<OutputAddrBean> outputAddr;
    @SerializedName("cosigner")
    private List<String> cosigner;

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public boolean isCanBroadcast() {
        return canBroadcast;
    }

    public void setCanBroadcast(boolean canBroadcast) {
        this.canBroadcast = canBroadcast;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTxStatus() {
        return txStatus;
    }

    public void setTxStatus(String txStatus) {
        this.txStatus = txStatus;
    }

    public String getTx() {
        return tx;
    }

    public void setTx(String tx) {
        this.tx = tx;
    }

    public List<Integer> getSignStatus() {
        return signStatus;
    }

    public void setSignStatus(List<Integer> signStatus) {
        this.signStatus = signStatus;
    }

    public List<OutputAddrBean> getOutputAddr() {
        return outputAddr;
    }

    public void setOutputAddr(List<OutputAddrBean> outputAddr) {
        this.outputAddr = outputAddr;
    }

    public List<String> getCosigner() {
        return cosigner;
    }

    public void setCosigner(List<String> cosigner) {
        this.cosigner = cosigner;
    }

    public static class OutputAddrBean {
        /**
         * addr : bcrt1qnuh3qc9g6lwlqqvmf7hg05pzlujhua9emdqdty4znjstr5886paqhwp25r
         * amount : 0.0001 mBTC
         */

        @SerializedName("addr")
        private String addr;
        @SerializedName("amount")
        private String amount;

        public String getAddr() {
            return addr;
        }

        public void setAddr(String addr) {
            this.addr = addr;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }
    }
}
