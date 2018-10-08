package com.component.basicres;

import android.app.Application;

/**
 * Created by fox.hu on 2018/10/8.
 */

public class BaseApplication extends Application {
    private static BaseApplication mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = this;
    }

    public static Application getAppContext() {
        return mAppContext;
    }
}
