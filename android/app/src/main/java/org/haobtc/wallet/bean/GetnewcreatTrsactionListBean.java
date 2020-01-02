package org.haobtc.wallet.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetnewcreatTrsactionListBean {


    /**
     * txid : eef1c62c12ebe4470fa052b7746019146725f491fef20f38b82b190f92b9d218
     * can_broadcast : true
     * amount : 0. mBTC
     * fee : unknown mBTC
     * description :
     * tx_status : Signed
     * sign_status : [0,0]
     * output_addr : [{"addr":"bcrt1qnuh3qc9g6lwlqqvmf7hg05pzlujhua9emdqdty4znjstr5886paqhwp25r","amount":"0. mBTC"},{"addr":"","amount":"0. mBTC"}]
     * input_addr : ["",""]
     * cosigner : ["Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41vMKQbeHveRvuThAmm","Vpub5gyCX33B53xAyfEaH1Jfnp5grizbHfxVz6bWLPD92nLcbKMsQzSbM2eyGiK4qiRziuoRhoeVMoPLvEdfbQxGp88PN9cU6zupSSuiPi3RjEg"]
     * tx : 02000000010000000000000000000000000000000000000000000000000000000000000000ffffffff00ffffffff0200000000000000002200209f2f1060a8d7ddf0019b4fae87d022ff257e74b9db40d592a29ca0b1d0e7d07a0000000000000000266a24aa21a9ede2f61c3f71d1defd3fa999dfa36953755c690689799962b48bebd836974e8cf900000000
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
    @SerializedName("input_addr")
    private List<String> inputAddr;
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

    public List<String> getInputAddr() {
        return inputAddr;
    }

    public void setInputAddr(List<String> inputAddr) {
        this.inputAddr = inputAddr;
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
