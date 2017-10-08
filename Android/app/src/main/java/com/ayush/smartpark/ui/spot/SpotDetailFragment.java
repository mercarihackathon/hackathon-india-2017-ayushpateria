package com.ayush.smartpark.ui.spot;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ayush.smartpark.util.Userdata;
import com.bumptech.glide.Glide;

import butterknife.Bind;
import butterknife.OnClick;

import com.ayush.smartpark.R;
import com.ayush.smartpark.model.Spot;
import com.ayush.smartpark.ui.base.BaseActivity;
import com.ayush.smartpark.ui.base.BaseFragment;


/**
 * Created by ayush on 07/10/17.
 */
public class SpotDetailFragment extends BaseFragment {

    public static final String ARG_ITEM_ID = "item_id";


    private Spot.SpotItem mSpot;

    @Bind(R.id.details_address)
    TextView address;

    @Bind(R.id.details_cost)
    TextView cost;

    @Bind(R.id.backdrop)
    ImageView backdropImg;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // load dummy item by using the passed item ID.
            mSpot = Spot.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflateAndBind(inflater, container, R.layout.fragment_spot_detail);

        if (!((BaseActivity) getActivity()).providesActivityToolbar()) {
            // No Toolbar present. Set include_toolbar:
            ((BaseActivity) getActivity()).setToolbar((Toolbar) rootView.findViewById(R.id.toolbar));
        }

        if (mSpot != null) {
            loadBackdrop();
            collapsingToolbar.setTitle(mSpot.title);
            cost.setText("Rs. " + mSpot.cost);
            address.setText(mSpot.addr);
        }

        return rootView;
    }

    private void loadBackdrop() {
        Glide.with(this).load(mSpot.photoId).centerCrop().into(backdropImg);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sample_actions, menu);
        menu.findItem(R.id.action_gps).setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static SpotDetailFragment newInstance(String itemID) {
        SpotDetailFragment fragment = new SpotDetailFragment();
        Bundle args = new Bundle();
        args.putString(SpotDetailFragment.ARG_ITEM_ID, itemID);
        fragment.setArguments(args);
        return fragment;
    }

    public SpotDetailFragment() {}

    @OnClick(R.id.details_directions)
    public void openDirections(View view) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr="+ Userdata.getLongLat(this.getActivity().getApplicationContext(), "")+"&daddr="+mSpot.lat+","+mSpot.lon));
        startActivity(intent);
    }
}
