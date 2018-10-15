package com.component.readercomponent.runalone.application;

import com.component.basicres.BaseApplication;
import com.component.componentlib.router.Route;

public class ReaderApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Route.registerComponent("com.component.share.applike.ShareAppLike");
    }
}