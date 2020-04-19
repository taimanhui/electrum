package org.haobtc.wallet.event;

public class InitEvent {
    private String name;

    public InitEvent(String name) {
        this.name = name;
    }

    public void setName(String task) {
        this.name = task;
    }

    public String getName() {
        return name;
    }
}
