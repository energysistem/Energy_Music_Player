
package com.energysistem.energyMusic.service;

import android.content.ContextWrapper;

public class ServiceToken {
    public ContextWrapper mWrappedContext;

    public ServiceToken(ContextWrapper context) {
        mWrappedContext = context;
    }
}
