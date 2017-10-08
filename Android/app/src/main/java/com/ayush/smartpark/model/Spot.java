package com.ayush.smartpark.model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ayush.smartpark.R;


/**
 * Created by ayush on 07/10/17.
 */
public class Spot {


    public static final List<SpotItem> ITEMS = new ArrayList<>();


    public static final Map<String, SpotItem> ITEM_MAP = new HashMap<>(5);



    public static void addItem(SpotItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static void fetchItems(SpotItem item) {
        ITEMS.clear();
        ITEM_MAP.clear();
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }


    public static class SpotItem {
        public final String id;
        public final int photoId;
        public final String title;
        public final String lat;
        public final String lon;
        public final String addr;
        public final String cost;
        public final String distance;

        public SpotItem(String id, int photoId, String title, String lat, String lon, String addr, String cost, String distance) {
            this.id = id;
            this.photoId = photoId;
            this.title = title;
            this.lat = lat;
            this.lon = lon;
            this.addr = addr;
            this.cost = cost;
            this.distance = distance;
        }
    }
}

