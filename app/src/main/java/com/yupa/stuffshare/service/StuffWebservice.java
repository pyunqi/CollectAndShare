package com.yupa.stuffshare.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yupa.stuffshare.db.DBController;
import com.yupa.stuffshare.entity.Stuff;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StuffWebservice implements WSConstants {
    private static final String TAG = "StuffWebservice";

    private static DBController dbController;

    static final MediaType jsonHeader = MediaType.parse(jsonType);
    static final String addUrl = hostUrl + "addStuff.php";
    static final String uploadUrl = hostUrl + "uploadImage.php";
    static final String downloadUrl = hostUrl + "downloadImage.php";
    static final String queryAllUrl = hostUrl + "queryAll.php";
    static final MediaType imageHeader = MediaType.parse(imageType);

    /**
     * add stuff information into mysql
     *
     * @param stuff
     * @return
     */
    public static String addStuff(Stuff stuff) {
        String fileName = stuff.get_picture();
        fileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());
        stuff.set_picture(fileName);
        String jsonString = JSON.toJSONString(stuff);
        RequestBody req = RequestBody.create(jsonHeader, jsonString);
        Request request = new Request.Builder().url(addUrl).post(req).build();
        return executeOK3Post(request, "code");
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
        return executeOK3Post(request, "code");
    }

    /**
     * make local db and file consistent with server
     *
     * @param context
     * @param bPath
     * @return
     */
    public static String syncServer(Context context, String bPath) {
        // load all data , did not do split page since in this case rows limit to 50.
        JSONObject queryAll = new JSONObject();
        dbController = new DBController(context);
        queryAll.put("queryData", "all");
        RequestBody req = RequestBody.create(jsonHeader, queryAll.toJSONString());
        Request request = new Request.Builder().url(queryAllUrl).post(req).build();
        String dataStr = executeOK3Post(request, "data");
        ArrayList<Stuff> stuffList = (ArrayList<Stuff>) JSON.parseArray(dataStr, Stuff.class);
        //replace local db's data and non-exists pic path to a new List
        for (Stuff stuff : stuffList) {
            stuff.set_picture(bPath + "/" + stuff.get_picture());
        }
        dbController.replaceAllStuff(stuffList);
        for (Stuff stuff : stuffList) {
            final File stuffPic = new File(stuff.get_picture());
            if (!stuffPic.exists()) {
                Request downloadRequest = new Request.Builder().url(downloadUrl).get().build();
                OkHttpClient client = new OkHttpClient();
                try {
                    Response response = client.newCall(downloadRequest).execute();
                    final Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                    OutputStream os;
                    os = new FileOutputStream(stuffPic);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    os.flush();
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return "200";
    }


    private static String executeOK3Post(Request request, String key) {
        OkHttpClient client = new OkHttpClient();
        String res = null;
        try {
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                JSONObject ret = JSON.parseObject(response.body().string());
                res = ret.getString(key);
                Log.i("Success", ret.getString("msg"));
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
