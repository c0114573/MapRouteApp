package com.c0114573.maprouteapp;

/**
 * Created by member on 2017/12/22.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;

public class parseJsonpOfDirectionAPI {

    MainActivity ma;
    ArrayList<RouteDate> RouteList = new ArrayList<RouteDate>();
    //    ArrayList<String> route = new ArrayList<>();
    RouteDate rl;

    public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
        String temp = "";

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
        JSONArray jsonRoutes = null;
        JSONArray jsonLegs = null;
        JSONArray jsonSteps = null;

        try {

            jsonRoutes = jObject.getJSONArray("routes");

            for (int i = 0; i < jsonRoutes.length(); i++) {
                jsonLegs = ((JSONObject) jsonRoutes.get(i)).getJSONArray("legs");

                // スタート地点・住所
                String s_address = (String) ((JSONObject) (JSONObject) jsonLegs.get(i)).getString("start_address");
                ma.info_A = s_address;

                // 到着地点・住所
                String e_address = (String) ((JSONObject) (JSONObject) jsonLegs.get(i)).getString("end_address");
                ma.info_B = e_address;

                String distance_txt = (String) ((JSONObject) ((JSONObject) jsonLegs.get(i)).get("distance")).getString("text");
                temp += "移動距離" + distance_txt;
//                temp += "<br>";

                String distance_val = (String) ((JSONObject) ((JSONObject) jsonLegs.get(i)).get("duration")).getString("text");
                temp += "移動時間" + distance_val;
//                temp += "<br><br>";

                List path = new ArrayList<HashMap<String, String>>();
//                RouteDate rl = new RouteDate();

                for (int j = 0; j < jsonLegs.length(); j++) {
                    jsonSteps = ((JSONObject) jsonLegs.get(j)).getJSONArray("steps");

                    for (int k = 0; k < jsonSteps.length(); k++) {
//						String polyline = "";
//						polyline = (String) ((JSONObject) ((JSONObject) jsonSteps.get(k)).get("polyline")).get("points");
//						// Log.d("po",polyline);

                        String instructions = (String) ((JSONObject) (JSONObject) jsonSteps.get(k)).getString("html_instructions");
                        instructions = instructions.replaceAll("<.+?>", "");
                        Log.i("AAAAAAAAAAAAAAAA", instructions);


//                        route.add(instructions);
                        String duration_value = (String) ((JSONObject) ((JSONObject) jsonSteps.get(k)).get("duration")).getString("value");
                        String duration_txt = (String) ((JSONObject) ((JSONObject) jsonSteps.get(k)).get("duration")).getString("text");


                        HashMap<String, String> hm = new HashMap<String, String>();
//						Log.d("html", instructions);
                        String slat = (String) ((JSONObject) ((JSONObject) jsonSteps.get(k)).get("start_location")).getString("lat");
                        String slng = (String) ((JSONObject) ((JSONObject) jsonSteps.get(k)).get("start_location")).getString("lng");
                        String elat = (String) ((JSONObject) ((JSONObject) jsonSteps.get(k)).get("end_location")).getString("lat");
                        String elng = (String) ((JSONObject) ((JSONObject) jsonSteps.get(k)).get("end_location")).getString("lng");
                        hm.put("lat", slat);
                        hm.put("lng", slng);
                        hm.put("lat", elat);
                        hm.put("lng", elng);
//						Log.d("slat", slat);
//						Log.d("slng", slng);
//						Log.d("elat", elat);
//						Log.d("elng", elng);

//                        temp += instructions + "/" + duration_value + " m /"
//                                + duration_txt + "<br>経度:" + slat + "<br>緯度:" + slng + "<br><br>";


                        temp += instructions + "/" + duration_value + " m /"
                                + duration_txt + "経度:" + slat + "緯度:" + slng + "\n";

                        path.add(hm);

                        rl = new RouteDate();

                        rl.instructions = instructions;
                        rl.duration_value = duration_value;
                        rl.duration_txt = duration_txt;

                        rl.lat = Double.parseDouble(slat);
                        rl.lng = Double.parseDouble(slng);

                        RouteList.add(rl);

                        Log.i("AAA", rl.instructions);
                        Log.i("BBB", rl.duration_value);
                        Log.i("CCC", rl.duration_txt);
                        // Log.d("Nakata", RouteList.get(k).instructions);

                    }
//                    System.out.println(route);
                    // ルート座標
                    routes.add(path);
                }
                // ルート情報
                ma.posinfo = temp;
                ma.RouteList = RouteList;

            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return routes;
    }
}