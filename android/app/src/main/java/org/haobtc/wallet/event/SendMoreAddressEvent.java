package org.haobtc.wallet.event;

public class SendMoreAddressEvent {
    private String inputAddress;
    private String inputAmount;

    public String getInputAddress() {
        return inputAddress;
    }

    public void setInputAddress(String inputAddress) {
        this.inputAddress = inputAddress;
    }

    public String getInputAmount() {
        return inputAmount;
    }

    public void setInputAmount(String inputAmount) {
        this.inputAmount = inputAmount;
    }
}
