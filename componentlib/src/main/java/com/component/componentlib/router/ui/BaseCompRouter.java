package com.component.componentlib.router.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.component.componentlib.router.utils.UriUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fox.hu on 2018/9/21.
 */

public abstract class BaseCompRouter implements IComponentRouter {

    protected Map<String, Class> routeMapper = new HashMap<String, Class>();
    protected Map<Class, Map<String, Integer>> paramsMapper = new HashMap<>();

    protected abstract String getHost();

    @Override
    public boolean openUri(Context context, String url, Bundle bundle) {
        if (TextUtils.isEmpty(url) || context == null) {
            return true;
        }
        return openUri(context, Uri.parse(url), bundle, 0);
    }

    @Override
    public boolean openUri(Context context, Uri uri, Bundle bundle) {
        return openUri(context, uri, bundle, 0);
    }

    @Override
    public boolean openUri(Context context, String url, Bundle bundle, Integer requestCode) {
        if (TextUtils.isEmpty(url) || context == null) {
            return true;
        }
        return openUri(context, Uri.parse(url), bundle, requestCode);
    }

    @Override
    public boolean openUri(Context context, Uri uri, Bundle bundle, Integer requestCode) {
        if (uri == null || context == null) {
            return true;
        }
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (!getHost().equals(host)) {
            return false;
        }
        List<String> pathSegments = uri.getPathSegments();
        //在数组中每个元素之间使用 / 来连接

        String path = "/" + TextUtils.join("/", pathSegments);
        if (routeMapper.containsKey(path)) {
            Class target = routeMapper.get(path);
            if (bundle == null) {
                bundle = new Bundle();
            }
            HashMap<String, String> params = UriUtils.parseParams(uri);
            Map<String, Integer> paramsType = paramsMapper.get(target);
            UriUtils.setBundleValue(bundle, params, paramsType);
            Intent intent = new Intent(context, target);
            intent.putExtras(bundle);
            if (requestCode > 0 && context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, requestCode);
                return true;
            }
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public boolean verifyUri(Uri uri) {
        String host = uri.getHost();
        if (!getHost().equals(host)) {
            return false;
        }
        List<String> pathSegments = uri.getPathSegments();
        String path = "/" + TextUtils.join("/", pathSegments);
        return routeMapper.containsKey(path);
    }
}
