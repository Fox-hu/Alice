package com.component.alice.application;

import android.app.Application;

import com.component.componentlib.router.ui.UIRouter;

/**
 * Created by fox.hu on 2018/10/15.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        UIRouter.get().registerUI("app");
    }
}
