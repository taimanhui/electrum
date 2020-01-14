package org.haobtc.wallet.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ScanCheckDetailBean {

    /**
     * type : 2
     * data : {"txid":"090f7f6e09b73580af01b54e71ec4a6ad550cd2cac294c572be8b9fe74bb952b","can_broadcast":false,"amount":"0 mBTC","fee":"0.01 mBTC","description":"","tx_status":"Unsigned","sign_status":[0,2],"output_addr":[{"addr":"bcrt1qnuh3qc9g6lwlqqvmf7hg05pzlujhua9emdqdty4znjstr5886paqhwp25r","amount":"0.01 mBTC"},{"addr":"bcrt1qng4x9xgz4ffxhgp38jh5adp7ssu6jd7jkplq5zh3wdw66mzadr7sfprk96","amount":"9.98 mBTC"}],"cosigner":["Vpub5gyCX33B53xAyfEaH1Jfnp5grizbHfxVz6bWLPD92nLcbKMsQzSbM2eyGiK4qiRziuoRhoeVMoPLvEdfbQxGp88PN9cU6zupSSuiPi3RjEg","Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41vMKQbeHveRvuThAmm"],"tx":"70736274ff010089020000000120f4dd69233e0659b0fb786ad9f1b73c78da5feb89262cf633cc9c2352ad5c770100000000fdffffff02e8030000000000002200209f2f1060a8d7ddf0019b4fae87d022ff257e74b9db40d592a29ca0b1d0e7d07a703a0f00000000002200209a2a629902aa526ba0313caf4eb43e8439a937d2b07e0a0af1735dad6c5d68fd544c19000001012b40420f0000000000220020a9ec03eaa11691158d4214abc47137256639e39d115ca65714f4f4aec303eeee010547522102a4b60ee8a4d8c2eff95b8ddc0ba3f0025d3092e744448b13a34dfd191bbaaf0fb7a78d194e3fdbcfcae49bd7f5ca87d90cbc21edd1010000000200000000"}
     */

    @SerializedName("type")
    private int type;
    @SerializedName("data")
    private DataBean data;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * txid : 090f7f6e09b73580af01b54e71ec4a6ad550cd2cac294c572be8b9fe74bb952b
         * can_broadcast : false
         * amount : 0 mBTC
         * fee : 0.01 mBTC
         * description :
         * tx_status : Unsigned
         * sign_status : [0,2]
         * output_addr : [{"addr":"bcrt1qnuh3qc9g6lwlqqvmf7hg05pzlujhua9emdqdty4znjstr5886paqhwp25r","amount":"0.01 mBTC"},{"addr":"bcrt1qng4x9xgz4ffxhgp38jh5adp7ssu6jd7jkplq5zh3wdw66mzadr7sfprk96","amount":"9.98 mBTC"}]
         * cosigner : ["Vpub5gyCX33B53xAyfEaH1Jfnp5grizbHfxVz6bWLPD92nLcbKMsQzSbM2eyGiK4qiRziuoRhoeVMoPLvEdfbQxGp88PN9cU6zupSSuiPi3RjEg","Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41vMKQbeHveRvuThAmm"]
         * tx : 70736274ff010089020000000120f4dd69233e0659b0fb786ad9f1b73c78da5feb89262cf633cc9c2352ad5c770100000000fdffffff02e8030000000000002200209f2f1060a8d7ddf0019b4fae87d022ff257e74b9db40d592a29ca0b1d0e7d07a703a0f00000000002200209a2a629902aa526ba0313caf4eb43e8439a937d2b07e0a0af1735dad6c5d68fd544c19000001012b40420f0000000000220020a9ec03eaa11691158d4214abc47137256639e39d115ca65714f4f4aec303eeee010547522102a4b60ee8a4d8c2eff95b8ddc0ba3f0025d3092e744448b13a34dfd191bbaaf0fb7a78d194e3fdbcfcae49bd7f5ca87d90cbc21edd1010000000200000000
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
             * amount : 0.01 mBTC
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
}
