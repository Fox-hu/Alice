package com.component.readercomponent.applike;

import com.component.componentlib.applicationlike.IApplicationLike;
import com.component.componentlib.router.Router;
import com.component.componentlib.router.ui.UIRouter;
import com.component.componentservice.readerbook.ReadBookService;
import com.component.readercomponent.serviceimpl.ReadBookServiceImpl;

/**
 * Created by fox.hu on 2018/10/8.
 */

public class ReadAppLike implements IApplicationLike {
    Router router = Router.get();
    UIRouter uiRouter = UIRouter.get();

    @Override
    public void onCreate() {
        uiRouter.registerUI("reader");
        router.addService(ReadBookService.class.getSimpleName(), new ReadBookServiceImpl());
    }

    @Override
    public void onStop() {
        uiRouter.unRegisterUI("reader");
        router.removeService(ReadBookService.class.getSimpleName());
    }
}
