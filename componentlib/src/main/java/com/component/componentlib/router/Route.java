package com.component.componentlib.router;

import android.text.TextUtils;

import com.component.componentlib.applicationlike.IApplicationLike;

import java.util.HashMap;

/**
 * Created by fox.hu on 2018/9/25.
 */

public class Route {
    private static final String TAG = Route.class.getSimpleName();

    private HashMap<String, Object> services = new HashMap<>();
    private static HashMap<String, IApplicationLike> components = new HashMap<>();

    private Route() {}

    public static Route get() {
        return Holder.INSTANCE;
    }

    public synchronized void addService(String serviceName, Object serviceImpl) {
        if (TextUtils.isEmpty(serviceName) || serviceImpl == null) {
            return;
        }
        services.put(serviceName, serviceImpl);
    }

    public synchronized Object getService(String serviceName) {
        if (TextUtils.isEmpty(serviceName)) {
            return null;
        }
        return services.get(serviceName);
    }

    public synchronized void removeService(String serviceName) {
        if (TextUtils.isEmpty(serviceName)) {
            return;
        }
        services.remove(serviceName);
    }

    public static void registerComponent(String clzName) {
        if (TextUtils.isEmpty(clzName)) {
            return;
        }

        if (components.keySet().contains(clzName)) {
            return;
        }

        try {
            Class<?> clazz = Class.forName(clzName);
            IApplicationLike iApplicationLike = (IApplicationLike) clazz.newInstance();
            iApplicationLike.onCreate();
            components.put(clzName, iApplicationLike);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unregisterComponent(String clzName) {
        if (TextUtils.isEmpty(clzName)) {
            return;
        }

        if (components.keySet().contains(clzName)) {
            components.get(clzName).onStop();
            components.remove(clzName);
            return;
        }
        try {
            Class<?> clazz = Class.forName(clzName);
            IApplicationLike iApplicationLike = (IApplicationLike) clazz.newInstance();
            iApplicationLike.onStop();
            components.remove(clzName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final class Holder {
        private static final Route INSTANCE = new Route();
    }
}
