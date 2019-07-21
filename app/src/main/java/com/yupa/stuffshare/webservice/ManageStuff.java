package com.yupa.stuffshare.webservice;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yupa.stuffshare.entity.Stuff;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ManageStuff implements WSConstants {
    static final MediaType jsonHeader = MediaType.parse(jsonType);
    static final String addUrl = hostUrl + "addStuff.php";
    static final String uploadUrl = hostUrl + "uploadImage.php";
    static final MediaType imageHeader = MediaType.parse(imageType);

    /**
     * add stuff information into mysql
     *
     * @param stuff
     * @return
     */
    public static String addStuff(Stuff stuff) {
        String jsonString = JSON.toJSONString(stuff);
        RequestBody body = RequestBody.create(jsonHeader, jsonString);
        Request request;
        request = new Request.Builder().url(addUrl).post(body).build();
        return executeOK3Post(request);
    }

    /**
     * upload image to server
     *
     * @param path
     * @return
     */
    public static String uploadImage(String path) {
        File file = new File(path);
        RequestBody req = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("fileName", file.getName(), RequestBody.create(imageHeader, file)).build();
        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(req)
                .build();
        return executeOK3Post(request);
    }

    private static String executeOK3Post(Request request) {
        OkHttpClient client = new OkHttpClient();
        String res = null;
        try {
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                JSONObject ret = JSON.parseObject(response.body().string());
                res = ret.getString("code");
                Log.i("Successful upload", ret.getString("msg"));
            } else {
                res = response.message();
            }
        } catch (IOException e) {
            e.printStackTrace();
            res = e.getMessage();
        }
        return res;
    }

}
