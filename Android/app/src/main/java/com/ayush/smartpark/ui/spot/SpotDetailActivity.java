package com.ayush.smartpark.ui.spot;

import android.os.Bundle;

import com.ayush.smartpark.R;
import com.ayush.smartpark.ui.base.BaseActivity;


/**
 * Created by ayush on 07/10/17.
 */
public class SpotDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Show the Up button in the action bar.
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SpotDetailFragment fragment =  SpotDetailFragment.newInstance(getIntent().getStringExtra(SpotDetailFragment.ARG_ITEM_ID));
        getFragmentManager().beginTransaction().replace(R.id.article_detail_container, fragment).commit();
    }

    @Override
    public boolean providesActivityToolbar() {
        return false;
    }
}
