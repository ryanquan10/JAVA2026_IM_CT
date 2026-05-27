package org.tio.mg.service.model.main;


import org.tio.mg.service.model.main.base.BaseWalletItemLocal;

public class WalletItemLocal extends BaseWalletItemLocal<WalletItemLocal> {
    public static final WalletItemLocal dao = new WalletItemLocal().dao();
}

