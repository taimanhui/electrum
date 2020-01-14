package org.haobtc.wallet.bean;

import com.google.gson.annotations.SerializedName;

public class MainSweepcodeBean {

    /**
     * type : 1
     * data : {"amount":1000000000,"message":"test","address":"bcrt1q9a4kk79hacd2s838xhdvxmhxrs6tskfp744t7v9pj7f9flayjy0s4d3ttm","memo":"test"}
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
         * amount : 1000000000
         * message : test
         * address : bcrt1q9a4kk79hacd2s838xhdvxmhxrs6tskfp744t7v9pj7f9flayjy0s4d3ttm
         * memo : test
         */

        @SerializedName("amount")
        private int amount;
        @SerializedName("message")
        private String message;
        @SerializedName("address")
        private String address;
        @SerializedName("memo")
        private String memo;

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getMemo() {
            return memo;
        }

        public void setMemo(String memo) {
            this.memo = memo;
        }
    }
}
