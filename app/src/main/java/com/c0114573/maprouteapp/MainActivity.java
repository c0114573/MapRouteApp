package com.c0114573.maprouteapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.*;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.CameraUpdateFactory;

import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.ProgressDialog;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, LocationListener {
    private final static int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    private final static String RIGHT_MESSAGE = "0";
    private final static String LEFT_MESSAGE = "1";
    public String message;
    public String address;
    public int port;
    UDPObjectTransfer udp = null;
    private static final int MY_LOCATION_REQUEST_CODE = 1000;
    public static ArrayList<RouteDate> RouteList = new ArrayList<RouteDate>();
    static final String TAG = "MainActivity";

    MapFragment mf;

    GoogleMap gMap;
    private static final int MENU_A = 0;
    private static final int MENU_B = 1;
    private static final int MENU_C = 2;
    private static final int MENU_D = 3;

    public static String posinfo = "";
    public static String info_A = "";
    public static String info_B = "";
    ArrayList<LatLng> markerPoints;
    // Toastを表示させるために使うハンドラ
    private Handler mHandler = new Handler();

    // スレッドを停止するために必要
    private boolean mThreadActive = true;
    public static MarkerOptions options;

    //現在位置情報
    public LocationManager mLocationManager; //GPSロケーションマネージャ
    // 現在位置　更新時間(ミリ秒)
    private static final int LOCATION_UPDATE_MIN_TIME = 10000;
    // 現在位置　更新距離(メートル)
    private static final int LOCATION_UPDATE_MIN_DISTANCE = 1;
    //LocationProvider provider = mLocationManager.getProvider(LocationManager.GPS_PROVIDER);

    private Marker mMarker;
    double MyLatitude;
    double MyLongitude;

    double durationLatitude;
    double durationLongitude;

    public ProgressDialog progressDialog;

    public String travelMode = "walking";//default

    final String[] items = {"walking", "driving"};

    int route_point = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //プログレス
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("検索中だす......");
        progressDialog.hide();

        //初期化
        markerPoints = new ArrayList<LatLng>();

        SupportMapFragment mapfragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapfragment.getMapAsync(this);
//        locationStart();
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;


///////////////     ここから
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
///////////////        ここまで自動で追加したもの。要確認
               gMap.setMyLocationEnabled(true);

        // 初期位置
        LatLng farstlocation = new LatLng(35.6263035, 139.3371608);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(farstlocation, 17));

        if (gMap != null) {
            if (farstlocation != null) {
                if (PermissionChecker.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || PermissionChecker.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    gMap.setMyLocationEnabled(true);
                }
            }

            // クリックリスナー
            gMap.setOnMapClickListener(new OnMapClickListener() {
                @Override
                public void onMapClick(LatLng point) {

                    // 3度目クリックでスタート地点を再設定
                    if (markerPoints.size() > 1) {
                        markerPoints.clear();
                        gMap.clear();
                    }

                    markerPoints.add(point);

                    options = new MarkerOptions();
                    options.position(point);

                    if (markerPoints.size() == 1) {
                        // options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.green));
                        options.title("A");

                    } else if (markerPoints.size() == 2) {
                        // options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.red));
                        options.title("B");

                    }

                    gMap.addMarker(options);

                    gMap.setOnMarkerClickListener(new OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            // TODO Auto-generated method stub

                            String title = marker.getTitle();
                            if (title.equals("A")) {
                                marker.setSnippet(info_A);

                            } else if (title.equals("B")) {
                                marker.setSnippet(info_B);
                            }
                            return false;
                        }
                    });

                    if (markerPoints.size() >= 2) {// ルート検索
                        routeSearch();
                    }
                }
            });
        }

    }


    private void routeSearch() {
        progressDialog.show();

        LatLng origin = markerPoints.get(0);
        LatLng dest = markerPoints.get(1);

        String url = getDirectionsUrl(origin, dest);

        DownloadTask downloadTask = new DownloadTask();

        downloadTask.execute(url);

    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        String sensor = "sensor=false";

        // パラメータ
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&language=ja" + "&mode=" + travelMode;

        // JSON指定
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        // 非同期で取得
        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            parserTask.execute(result);
        }
    }

    /*parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                parseJsonpOfDirectionAPI parser = new parseJsonpOfDirectionAPI();


                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // ルート検索で得た座標を使って経路表示
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            if (result.size() != 0) {

                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList<LatLng>();
                    lineOptions = new PolylineOptions();

                    List<HashMap<String, String>> path = result.get(i);

                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }
                    // ポリライン
                    lineOptions.addAll(points);
                    lineOptions.width(10);
                    lineOptions.color(0x550000ff);
                }
                // 描画
                gMap.addPolyline(lineOptions);
            } else {
                gMap.clear();
                Toast.makeText(MainActivity.this, "ルート情報を取得できませんでした", Toast.LENGTH_LONG).show();
            }
            progressDialog.hide();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.main, menu);
        menu.add(0, MENU_A, 0, "Info");
        // menu.add(0, MENU_B, 0, "Legal Notices");
        menu.add(0, MENU_C, 0, "Mode");
        menu.add(0, MENU_D, 0, "経路案内");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_A:
                show_mapInfo();
                return true;
            //何故か免責事項を表示させると固まるのでとりあえず避難
//            case MENU_B:
//                // Legal Notices(免責事項)
//                String LicenseInfo = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getApplicationContext());
//                AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(MainActivity.this);
//                LicenseDialog.setTitle("Legal Notices");
//
//                LicenseDialog.setMessage(LicenseInfo);
//                LicenseDialog.show();
//                return true;

            case MENU_C:
                show_settings();
                return true;

            case MENU_D:
                // スレッド処理開始
                if(RouteList.size()!=0) {
                    Toast.makeText(this, "経路案内を開始します", Toast.LENGTH_SHORT).show();
                    route_point = 0;
                    this.mThread = new Thread(null, mTask, "show_mapInfo");
                    this.mThread.start();
                }else {
                    Toast.makeText(this, "経路を設定してください", Toast.LENGTH_LONG).show();
                }
                return true;
        }
        return false;
    }

    // スレッド処理
    // 3秒ごとに route_invite();を繰り返し処理を行う
    private Runnable mTask = new Runnable() {
        @Override
        public void run() {
            // アクティブな間だけ処理をする
            while (mThreadActive) {
                try {
                    Thread.sleep(3000); //3秒待つ
                } catch (InterruptedException e) {
                    // TODO 自動生成された catch ブロック
                    e.printStackTrace();
                }

                // ハンドラーをはさまないとToastでエラーでる
                // UIスレッド内で処理をしないといけない
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        route_invite();
                    }
                });
            }
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    showText("スレッド終了");
                }
            });
        }
    };
    private Thread mThread;

    // スレッド時用トースト表示
    private void showText(final String text) {
        Toast.makeText(this, "" + text, Toast.LENGTH_SHORT).show();
    }


    // リ･ルート検索
    private void re_routeSearch() {
        progressDialog.show();

        LatLng origin = markerPoints.get(0);
        LatLng dest = markerPoints.get(1);

        //
        gMap.clear();

        // マーカー
        // A
        options = new MarkerOptions();
        options.position(origin);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.green));
        options.title("A");
        options.draggable(true);
        gMap.addMarker(options);
        // B
        options = new MarkerOptions();
        options.position(dest);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.red));
        options.title("B");
        options.draggable(true);
        gMap.addMarker(options);

        String url = getDirectionsUrl(origin, dest);

        DownloadTask downloadTask = new DownloadTask();

        downloadTask.execute(url);
    }

    // ルート表示
    private void show_mapInfo() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("経路情報");
        String str = "";
        for (RouteDate rd : RouteList) {
            str += rd.getRoutrInfo();
        }
        alert.setMessage(str);
        alert.show();
    }



    // 経路案内処理
    private void route_invite() {
        String str = "";
//        myLat = myLatitude[route_point];
//        myLng = myLongitude[route_point];
        // 表示
        setTitle(RouteList.get(route_point).instructions);
        TextView tv2 = (TextView) findViewById(R.id.textView2);
        str = "現在地:" + RouteList.get(route_point).instructions + "\n" +
                "距離:" + RouteList.get(route_point).duration_value + "\n" +
                "時間:" + RouteList.get(route_point).duration_txt + "\n" +
                "位置:" + RouteList.get(route_point).lat + "," + RouteList.get(route_point).lng + "\n" +
                "経路番号:" + route_point + "経路総数:" + RouteList.size();
        tv2.setText(str);
        durationLatitude = RouteList.get(route_point).lat;
        durationLongitude = RouteList.get(route_point).lng;

        float results = getDistanceBetween(MyLatitude, MyLongitude, durationLatitude, durationLongitude);

        // 指定範囲内 10以内
        if (results < 10) {

            str = RouteList.get(route_point + 1).duration_value;

            if ((str.contains("右折")) || (str.contains("右方向")) || (str.contains("大きく右")) || (str.contains("斜め右"))) {
                // UDP通信でESPに送る為の場所
                message = "RightOn";
                address = "192.168.4.1"; // 受信側端末の実際のアドレスに書き換える
                port = 8888;       // 受信側と揃える
                udp.trans(message, address, port);

            } else if (str.contains("左折") || (str.contains("左方向")) || (str.contains("大きく左")) || (str.contains("斜め左"))) {
                // ヴイィィィーン処理
                message = "LeftOn";
                address = "192.168.4.1"; // 受信側端末の実際のアドレスに書き換える
                port = 8888;// 受信側と揃える
                udp.trans(message, address, port);

            } else {


            }

            route_point++;
        }

        // 終了処理
        if (route_point > RouteList.size()) {
            Toast.makeText(this, "到着", Toast.LENGTH_LONG).show();
            // 終了を知らせる何か処理
            // スレッド停止
            this.mThread.interrupt();
            this.mThreadActive = false;
            return;
        }
    }


    // 距離判定
    public float getDistanceBetween(
            double latitude1, double longitude1,
            double latitude2, double longitude2) {

        float[] results = new float[3];
        // 2点間の距離を取得（第5引数にセットされることに注意！）
        Location.distanceBetween(latitude1, longitude1, latitude2, longitude2, results);

        if (results.length < 1) {
            // 取得に失敗した場合のコード
        }

        if (results.length < 3) {
            // 方位角の値を取得した場合のコード
        }

        // ここでは距離（メートル）のみを返却
        return results[0];
    }

    int point = 0;

    // モード切替
    private void show_settings() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("travelMode")
                .setSingleChoiceItems(items, point, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        Log.i("travelMode", items[item]);
                        travelMode = items[item];
                        point = item;
                    }
                })
                .setNegativeButton("close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            // よくサンプルコードでは以下のように引数でパーミッションチェックしています。
            //if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
            //        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 一方、パーミッションの許可ダイアログで「許可」された場合、このコールバックメソッド以降で現在位置の取得処理を
            // 行う必要があります。
            // 現在位置の取得はrequestLocationUpdatesを実行する必要がありますが、パーミッションチェックをやれとエラーが出ます。
            // そこで、このメソッドに到達した時点ではすでにパーミッションが許可/拒否されていますので、引数でなくとも
            // heckSelfPermissionを実行すればエラーも解消されますし良いかなと思って、以下のようにしています。
            if (PermissionChecker.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                gMap.setMyLocationEnabled(true);
                // mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                //  mLocationManager.requestLocationUpdates(getProvider(), 100, 100, (android.location.LocationListener) mLocationManager);
                mLocationManager.requestLocationUpdates(getProvider(), 10000, 10, this);
            } else {
                Toast.makeText(this, "権限を取得できませんでした。", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "LocationChanged実行", Toast.LENGTH_SHORT).show();
        setLocation(location);
        try {
            mLocationManager.removeUpdates(this);
        } catch (SecurityException e) {
        }
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    //
    @Override
    public void onProviderDisabled(String s) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("MainActivity","Called_onDestroy");
        try {
            mLocationManager.removeUpdates((android.location.LocationListener) mLocationManager);

            this.mThread.interrupt();
            this.mThreadActive = false;
        } catch (SecurityException e) {
            // removeUpdatesを使用する場合もパーミッションチェックをするか、このようにSecurityExceptionをキャッチする対応が必要です。
            // onRequestPermissionsResultでパーミッションチェックを例にしたのでこちらはSecurityExceptionで対応します。
            // 何もしてませんが、本当は例外に応じた後続処理を書く必要があります。
        } catch (ClassCastException e) {

        } catch (NullPointerException e) {
            // スレッドが起動していないのに終了しようとするとエラーが起きるためここで回避
        }

    }

    private String getProvider() {
        Criteria criteria = new Criteria();
        return mLocationManager.getBestProvider(criteria, true);
    }

    private void confirmPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this).setTitle("パーミッション説明")
                    .setMessage("このアプリを実行するには位置情報の権限を与えてやる必要です。よろしくお願い致します。")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // trueもfalseも結局同じrequestPermissionsを実行しているので一つにまとめるべきかも
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_LOCATION_REQUEST_CODE);
                        }
                    })
                    .create()
                    .show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
        }
    }

    private void setDefaultLocation() {
        LatLng tokyo = new LatLng(35.681298, 139.766247);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tokyo, 18));
    }

    private void setLocation(Location location) {
        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        gMap.addMarker(new MarkerOptions().position(myLocation).title("now Location"));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));
    }

}


//    //現在位置マーカー更新
//    public void onCurrentMarkerChanged(double lat, double lng) {
//        LatLng myLocation = new LatLng(lat, lng);
//        mMarker.remove();
//        mMarker=gMap.addMarker(new MarkerOptions().position(myLocation).title("現在位置").
//                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//    }


//    @Override
//    public void onPause() {
//        if (mLocationManager != null) {
//            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
//                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
//                    (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//            mLocationManager.removeUpdates(this);
//        }
//        super.onPause();
//
//    }


