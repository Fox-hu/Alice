package com.component.share.applike;

import com.component.componentlib.applicationlike.IApplicationLike;
import com.component.componentlib.router.ui.UIRouter;

/**
 * Created by fox.hu on 2018/10/11.
 */

public class ShareAppLike implements IApplicationLike {
    UIRouter uiRouter = UIRouter.get();

    @Override
    public void onCreate() {
        uiRouter.registerUI("share");
    }

    @Override
    public void onStop() {
        uiRouter.unRegisterUI("share");
    }
}
