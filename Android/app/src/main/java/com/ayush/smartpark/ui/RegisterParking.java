package com.ayush.smartpark.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.ayush.smartpark.R;
import com.ayush.smartpark.json.JSONParser;
import com.ayush.smartpark.model.Spot;
import com.ayush.smartpark.ui.base.BaseActivity;
import com.ayush.smartpark.util.Constants;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by ayush on 07/10/17.
 */
public class RegisterParking extends BaseActivity {
    private static final int PLACE_PICKER_REQUEST = 1;

    @Bind(R.id.reg_text_username)
    EditText username;

    @Bind(R.id.reg_text_title)
    EditText title;

    @Bind(R.id.reg_text_cost)
    EditText cost;

    @Bind(R.id.reg_text_addr)
    TextView addr;

    @Bind(R.id.reg_progressbar)
    ProgressBar progressbar;


    boolean loc_picked = false;
    private double userLat, userLon;
    private String userLoc, st;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_parking);
        ButterKnife.bind(this);
        setupToolbar();
    }

    @OnClick(R.id.reg_pick_place)
    public void pickPlace(View view) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(RegisterParking.this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.fab)
    public void onFabClicked(View view) {
        if(loc_picked) {
            Spot.SpotItem mySpot = new Spot.SpotItem("0", R.drawable.p1, title.getText().toString(), String.valueOf(userLat), String.valueOf(userLon), addr.getText().toString(), cost.getText().toString(), "");
            JSONObject jsonSpot = new JSONObject();
            try {
                jsonSpot.put("title", mySpot.title);
                jsonSpot.put("cost", mySpot.cost);
                jsonSpot.put("user", username.getText().toString());
                jsonSpot.put("coord", ""+mySpot.lat+","+mySpot.lon);
                jsonSpot.put("address", mySpot.addr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new LoadJson().execute(jsonSpot);
        }
        else
        {
            Snackbar.make(view, "Please select a location!", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        }
    }

    private void setupToolbar() {
        final ActionBar ab = getActionBarToolbar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        menu.findItem(R.id.action_gps).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_reg;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place selectedPlace = PlacePicker.getPlace(data, this);
                userLat = selectedPlace.getLatLng().latitude;
                userLon = selectedPlace.getLatLng().longitude;
                userLoc = "" + userLat + "," + userLon;
                st = selectedPlace.getAddress().toString();
                addr.setText(st);
                addr.setVisibility(View.VISIBLE);
                loc_picked = true;
            }
        }
    }


    public class LoadJson extends AsyncTask<JSONObject, String, String> {
        JSONParser jsonParser = new JSONParser();
        String jsonSpots;
        @Override
        protected void onPreExecute() {
            progressbar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(JSONObject... params) {
            Log.d("test", params[0].toString());
            try {
                Log.d("userLoc", userLoc);

                String url_spot 		= Constants.addURLspot();
                JSONArray json_spots 	= jsonParser.makeHttpRequest(url_spot,"POST", params[0]);

                jsonSpots 	= json_spots.toString();


            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            progressbar.setVisibility(View.GONE);
            super.onPostExecute(result);
            Snackbar.make(findViewById(R.id.reg_fab), "Parking spot added!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
//            finish();
        }

    }
}
