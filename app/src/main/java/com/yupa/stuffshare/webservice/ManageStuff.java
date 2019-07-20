package com.yupa.stuffshare.webservice;

import android.content.res.Resources;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yupa.stuffshare.R;
import com.yupa.stuffshare.entity.Stuff;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ManageStuff implements WSConstants{
    public static final MediaType header = MediaType.parse(jsonHeader);
    public static final String url = hostUrl + "addStuff.php";


    public static String addStuff(Stuff stuff) {
        String jsonString = JSON.toJSONString(stuff);
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(header, jsonString);
        Request request;
        request = new Request.Builder().url(url).post(body).build();
        String errorStr = null;
        try {
            Response response = client.newCall(request).execute();
            if ( response.isSuccessful() ) {
                JSONObject ret = JSON.parseObject(response.body().string());
                Integer code = ret.getInteger("code");
                if ( code == 200 ) {
                } else {
                    errorStr = ret.getString("msg");
                }
            } else {
                errorStr = response.message();
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorStr = e.getMessage();
        }
        return errorStr;
    }

}
