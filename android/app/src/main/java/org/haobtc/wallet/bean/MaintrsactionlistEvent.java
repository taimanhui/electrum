package org.haobtc.wallet.bean;

public class MaintrsactionlistEvent {
    private String tx_hash;
    private String date;
    private String amount;
    private boolean is_mine;
    private String confirmations;
    private String type;
    private String tx_status;
    private String invoice_id;

    public String getInvoice_id() {
        return invoice_id;
    }

    public void setInvoice_id(String invoice_id) {
        this.invoice_id = invoice_id;
    }

    public String getTx_status() {
        return tx_status;
    }

    public void setTx_status(String tx_status) {
        this.tx_status = tx_status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(String confirmations) {
        this.confirmations = confirmations;
    }

    public String getTx_hash() {
        return tx_hash;
    }

    public void setTx_hash(String tx_hash) {
        this.tx_hash = tx_hash;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public boolean isIs_mine() {
        return is_mine;
    }

    public void setIs_mine(boolean is_mine) {
        this.is_mine = is_mine;
    }
}
