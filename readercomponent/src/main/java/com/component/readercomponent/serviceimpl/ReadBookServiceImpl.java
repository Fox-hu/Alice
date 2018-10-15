package com.component.readercomponent.serviceimpl;

import android.support.v4.app.Fragment;

import com.component.componentservice.readerbook.ReadBookService;
import com.component.readercomponent.ReaderFragment;

/**
 * Created by fox.hu on 2018/10/15.
 */

public class ReadBookServiceImpl implements ReadBookService {
    @Override
    public Fragment getReadBookFragment() {
        return new ReaderFragment();
    }
}
