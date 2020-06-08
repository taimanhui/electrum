package org.haobtc.wallet.event;

public class FixWalletNameEvent {
    private String newName;

    public FixWalletNameEvent(String newName) {
        this.newName = newName;
    }

    public String getNewName() {
        return newName;
    }
}
