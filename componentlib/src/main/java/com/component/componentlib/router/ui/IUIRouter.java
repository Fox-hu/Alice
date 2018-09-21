package com.component.componentlib.router.ui;

/**
 * Created by fox.hu on 2018/9/21.
 */

public interface IUIRouter extends IComponentRouter {

    int PRIORITY_NORMAL = 0;
    int PRIORITY_LOW = -1000;
    int PRIORITY_HEIGHT = 1000;

    void registerUI(IComponentRouter router, int priority);

    void registerUI(IComponentRouter router);

    void registerUI(String host);

    void registerUI(String host, int priority);

    void unRegisterUI(IComponentRouter router);

    void unRegisterUI(String host);
}
