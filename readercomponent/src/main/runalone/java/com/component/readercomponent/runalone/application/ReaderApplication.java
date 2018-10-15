package com.component.readercomponent.runalone.application;

import com.component.basicres.BaseApplication;
import com.component.componentlib.router.Router;

public class ReaderApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Router.registerComponent("com.component.share.applike.ShareAppLike");
    }
}