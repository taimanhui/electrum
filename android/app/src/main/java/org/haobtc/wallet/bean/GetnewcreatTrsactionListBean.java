package org.haobtc.wallet.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetnewcreatTrsactionListBean {


    /**
     * txid : d8c325244f1c68b7983b55e45c8c1b4d61b82c3749deb35883eb71687f1f478b
     * can_broadcast : false
     * amount : 0. mBTC
     * fee : unknown mBTC
     * description :
     * tx_status : 21 confirmations
     * output_addr : [{"addr":"bcrt1qnuh3qc9g6lwlqqvmf7hg05pzlujhua9emdqdty4znjstr5886paqhwp25r","amount":"0. mBTC"},{"addr":null,"amount":"0. mBTC"}]
     * cosigner : ["Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41vMKQbeHveRvuThAmm","Vpub5gyCX33B53xAyfEaH1Jfnp5grizbHfxVz6bWLPD92nLcbKMsQzSbM2eyGiK4qiRziuoRhoeVMoPLvEdfbQxGp88PN9cU6zupSSuiPi3RjEg"]
     * tx : 020000000001010000000000000000000000000000000000000000000000000000000000000000ffffffff05023d310101ffffffff0200000000000000002200209f2f1060a8d7ddf0019b4fae87d022ff257e74b9db40d592a29ca0b1d0e7d07a0000000000000000266a24aa21a9ede2f61c3f71d1defd3fa999dfa36953755c690689799962b48bebd836974e8cf90120000000000000000000000000000000000000000000000000000000000000000000000000
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
         * amount : 0. mBTC
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
