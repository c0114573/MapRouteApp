package com.c0114573.maprouteapp;

/**
 * Created by member on 2017/12/25.
 */

public class RouteDate {
    String instructions;    // 案内情報
    String duration_value;  // 距離
    String duration_txt;    // 時間

    double lat;
    double lng;

    //コンストラクタ
    public RouteDate() {
    }

    public String getRoutrInfo() {
        String str = "";
        str = "進行方向 : " + this.instructions + "\n移動距離 : " + this.duration_value + "m,\n移動時間 : " + this.duration_txt
                + "\n目標地点 : (" + lat + "," + lng + ")\n\n";
        return str;
    }


}
