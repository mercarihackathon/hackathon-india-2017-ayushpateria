package com.ayush.smartpark.ui.spot;
import com.ayush.smartpark.json.JSONParser;
import com.ayush.smartpark.model.Spot;
import com.ayush.smartpark.ui.SpotsMapView;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ayush.smartpark.util.*;
import com.ayush.smartpark.R;
import com.ayush.smartpark.ui.base.BaseActivity;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by ayush on 07/10/17.
 */

public class ListActivity extends BaseActivity implements SpotListFragment.Callback {

    String defaultLatLng = "0.0,0.0";
    GPSTracker gps;
    ProgressBar progressbar;
    TextView customTitle, noSpots;
    MenuItem gpsIcon;
    double userLon, userLat;
    FloatingActionButton fab;
    private static final int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        fab = (FloatingActionButton) findViewById(R.id.list_mapview);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        noSpots = (TextView) findViewById(R.id.nospots);
        setupToolbar();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapIntent = new Intent(ListActivity.this, SpotsMapView.class);
                startActivity(mapIntent);
            }
        });
    }


    /**
     * Called when an item has been selected
     *
     * @param id the selected spot ID
     */
    @Override
    public void onItemSelected(String id) {
        // Start the detail activity in single pane mode.
        Intent detailIntent = new Intent(this, SpotDetailActivity.class);
        detailIntent.putExtra(SpotDetailFragment.ARG_ITEM_ID, id);
        startActivity(detailIntent);

    }

    private void setupToolbar() {
        final ActionBar ab = getActionBarToolbar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayShowCustomEnabled(true);
        View customView = getLayoutInflater().inflate(R.layout.actionbar_layout, null);
        // Get the textview of the title
        customTitle = (TextView) customView.findViewById(R.id.actionbarTitle);

        String street = Userdata.getStreet(getApplicationContext(), "SmartPark");
        customTitle.setText(street);

        // Change the font family (optional)
        customTitle.setTypeface(Typeface.MONOSPACE);

        // Apply the custom view
        ab.setCustomView(customView);

        // Set the on click listener for the title
        customView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("MainActivity", "ActionBar's title clicked.");
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(ListActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place selectedPlace = PlacePicker.getPlace(data, this);
                userLat = selectedPlace.getLatLng().latitude;
                userLon = selectedPlace.getLatLng().longitude;
                String userLoc = "" + userLat + "," + userLon;
                Userdata.setLongLat(getApplicationContext(), userLoc);
                String st = selectedPlace.getAddress().toString();
                st = st.subSequence(0, Math.min(st.length(), 15)).toString() + "..";
                customTitle.setText(st);
                Userdata.setStreet(getApplicationContext(), st);
                Log.d("St", st);
                fetchData();
            }
        }
    }

    public void refreshSpots() {
        String userLoc = Userdata.getLongLat(this, defaultLatLng);


                if (checkLocationPermission()) {
                gps = new GPSTracker(ListActivity.this);
                // check for GPS location
                if (gps.canGetLocation()) {
                    userLat = gps.getLatitude();
                    userLon = gps.getLongitude();
                    userLoc = "" + userLat + "," + userLon;
                    Userdata.setLongLat(getApplicationContext(), userLoc);
                    // Get the place name and set it in title and pref.

                    String st = getStreet(userLat, userLon).toString();
                    st = st.subSequence(0, Math.min(st.length(), 15)).toString() + "..";
                    customTitle.setText(st);
                    Userdata.setStreet(getApplicationContext(), st);

                }
            }
                else {
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    // Toast.makeText(ListActivity.this, "You need to allow locations permission", Toast.LENGTH_LONG).show();
                }
        fetchData();
    }

    public void fetchData() {
        String userLoc = "" + userLat + "," + userLon;
        if (userLoc != defaultLatLng)
            new LoadJson().execute("");
         else {
            Toast.makeText(this, "Please select a location.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getStreet(double userLat, double userLon) {
        Geocoder geocoder;
        List<Address> addresses;
        String address = "SmartPark";
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(userLat, userLon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses.get(0).getAddressLine(0);
            Userdata.setStreet(getApplicationContext(), address);
            Log.d("Street", address);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    refreshSpots();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }

    }

    public boolean checkLocationPermission()
    {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        gpsIcon = menu.findItem(R.id.action_gps);
        refreshSpots();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer();
                return true;

            case R.id.action_gps:
                refreshSpots();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_find;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }


public class LoadJson extends AsyncTask<String, String, String> {
    JSONParser jsonParser = new JSONParser();
    String jsonSpots;
    @Override
    protected void onPreExecute() {
        progressbar.setVisibility(View.VISIBLE);
        gpsIcon.setEnabled(false);
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        String userLoc = "" + userLat + "," + userLon;
        try {
                Log.d("userLoc", userLoc);
                JSONObject param = null;

                String url_spot 		= Constants.getURLspot(userLoc);
                JSONArray json_spots 	= jsonParser.makeHttpRequest(url_spot,"GET", param);
                Spot.ITEMS.clear();
                Spot.ITEM_MAP.clear();
                for(int i = 0; i < json_spots.length(); i++){
                    JSONObject spot = json_spots.optJSONObject(i);
                    if (spot != null) {
                        String[] coord = spot.getString("coord").split(",");
                        Spot.addItem(new Spot.SpotItem(String.valueOf(i), getMockPhoto(i), spot.getString("title"), coord[0], coord[1], spot.getString("address"), spot.getString("cost"), spot.getString("distance")));
                    }
                }

                jsonSpots 	= json_spots.toString();


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }


    public int getMockPhoto(int i) {
        int item = new Random().nextInt(5)+1;
        switch (i%5 + 1) {
            case 1:
                return R.drawable.p1;
            case 2:
                return R.drawable.p2;
            case 3:
                return R.drawable.p3;
            case 4:
                return R.drawable.p4;
            case 5:
                return R.drawable.p5;
            default:
                return R.drawable.p1;
        }
    }
    @Override
    protected void onPostExecute(String result) {


        progressbar.setVisibility(View.GONE);
        gpsIcon.setEnabled(true);

        SpotListFragment listFragment = (SpotListFragment) getFragmentManager().findFragmentById(R.id.article_list);

        // Check if the tab fragment is available
        if (listFragment != null) {
            // Call your method in the TabFragment
            listFragment.refreshAdapter();
        }
        Log.d("Resp", jsonSpots);
        super.onPostExecute(result);
    }

}


}
