package com.component.componentlib.router.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by fox.hu on 2018/9/21.
 */

public interface IComponentRouter {

    boolean openUri(Context context, String url, Bundle bundle);

    boolean openUri(Context context, Uri uri, Bundle bundle);

    boolean openUri(Context context, String url, Bundle bundle, Integer requestCode);

    boolean openUri(Context context, Uri uri, Bundle bundle, Integer requestCode);

    boolean verifyUri(Uri uri);
}
