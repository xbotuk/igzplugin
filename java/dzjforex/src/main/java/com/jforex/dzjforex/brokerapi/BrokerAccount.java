package com.jforex.dzjforex.brokerapi;

import com.jforex.dzjforex.config.ReturnCodes;
import com.jforex.dzjforex.handler.AccountInfo;

public class BrokerAccount {

    private final AccountInfo accountInfo;

    public BrokerAccount(final AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public int handle(final double accountInfoParams[]) {
        if (!isAccountAvailable())
            return ReturnCodes.ACCOUNT_UNAVAILABLE;

        fillAccountParams(accountInfoParams);
        return ReturnCodes.ACCOUNT_AVAILABLE;
    }

    private boolean isAccountAvailable() {
        return accountInfo.isConnected();
    }

    private void fillAccountParams(final double accountInfoParams[]) {
        accountInfoParams[0] = accountInfo.equity();
        accountInfoParams[1] = accountInfo.tradeValue();
        accountInfoParams[2] = accountInfo.usedMargin();
    }
}