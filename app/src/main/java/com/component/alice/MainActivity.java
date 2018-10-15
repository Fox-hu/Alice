package com.component.alice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.component.basiclib.ToastManager;
import com.component.basicres.BaseApplication;
import com.component.componentlib.router.Router;
import com.component.componentservice.readerbook.ReadBookService;
import com.component.router.annotation.RouteNode;

@RouteNode(path = "/main", desc = "首页")
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Fragment fragment;
    FragmentTransaction ft;
    Button installRead;
    Button uninstallRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        installRead = findViewById(R.id.install_share);
        uninstallRead = findViewById(R.id.uninstall_share);
        installRead.setOnClickListener(this);
        uninstallRead.setOnClickListener(this);
        showFragment();
    }

    private void showFragment() {
        if (fragment != null) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.remove(fragment).commit();
            fragment = null;
        }
        Router router = Router.get();
        if (router.getService(ReadBookService.class.getSimpleName()) != null) {
            ReadBookService service = (ReadBookService) router.getService(
                    ReadBookService.class.getSimpleName());
            fragment = service.getReadBookFragment();
            ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.tab_content, fragment).commitAllowingStateLoss();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.install_share:
                Router.registerComponent("com.component.share.applike.ShareAppLike");
                break;
            case R.id.uninstall_share:
                Router.unregisterComponent("com.component.share.applike.ShareAppLike");
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            ToastManager.show(BaseApplication.getAppContext(), data.getStringExtra("result"));
        }
    }
}
