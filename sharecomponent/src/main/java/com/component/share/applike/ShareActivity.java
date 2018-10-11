package com.component.share.applike;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.component.basicres.BaseActivity;
import com.component.componentservice.share.bean.Author;
import com.component.share.R;

/**
 * Created by fox.hu on 2018/10/11.
 */

public class ShareActivity extends BaseActivity {

    String bookName;

    Author author;

    private TextView tvShareTitle;
    private TextView tvShareBook;
    private TextView tvAuthor;
    private TextView tvCounty;

    private final static int RESULT_CODE = 8888;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        AutowiredService.Factory.getInstance().create().autowire(this);
        setContentView(R.layout.share_activity_share);

        tvShareTitle =  findViewById(R.id.share_title);
        tvShareBook =  findViewById(R.id.share_tv_tag);
        tvAuthor =  findViewById(R.id.share_tv_author);
        tvCounty =  findViewById(R.id.share_tv_county);

        tvShareTitle.setText("Book");

        if (bookName != null) {
            tvShareBook.setText(bookName);
        }

        if (author != null) {
            tvAuthor.setText(author.getName());
            tvCounty.setText(author.getCounty());
        }

        Intent intent = new Intent();
        intent.putExtra("result", "Share Success");
        setResult(RESULT_CODE, intent);
    }
}
