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
    static final String searchUrl = hostUrl + "searchStuff.php";
    static final String updateUrl = hostUrl + "updateStuff.php";
    static final String deleteUrl = hostUrl + "deleteStuff.php";
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
    public static int addStuff(Stuff stuff, String key) {
        setPicName(stuff);
        String jsonString = JSON.toJSONString(stuff);
        RequestBody req = RequestBody.create(jsonHeader, jsonString);
        Request request = new Request.Builder().url(addUrl).post(req).build();
        int id = 0;
        OkHttpClient client = new OkHttpClient();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                JSONObject ret = JSON.parseObject(response.body().string());
                id = ret.getInteger(key);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return id;
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
        dbController = new DBController(context);
        JSONObject queryAll = new JSONObject();
        queryAll.put("queryData", "all");
        RequestBody req = RequestBody.create(jsonHeader, queryAll.toJSONString());
        Request request = new Request.Builder().url(queryAllUrl).post(req).build();
        String dataStr = executeOK3Post(request, "data");
        if (dataStr == null){
            return "";
        }
        ArrayList<Stuff> stuffList = (ArrayList<Stuff>) JSON.parseArray(dataStr, Stuff.class);
        //replace local db's data and non-exists pic path to a new List
        for (Stuff stuff : stuffList) {
            stuff.set_picture(bPath + "/" + stuff.get_picture());
        }
        dbController.replaceAllStuff(stuffList);
        for (Stuff stuff : stuffList) {
            final File stuffPic = new File(stuff.get_picture());
            if (!stuffPic.exists()) {
                downloadImage(stuffPic);
            }
        }

        return "200";
    }

    /**
     * download the image
     * @param stuffPic
     */
    private static void downloadImage(File stuffPic) {
        JSONObject imageName = new JSONObject();
        imageName.put("name", stuffPic.getName());
        RequestBody reqSync = RequestBody.create(jsonHeader, imageName.toJSONString());
        Request downloadRequest = new Request.Builder().url(downloadUrl).post(reqSync).build();
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

    /**
     * search data from remote server
     *
     * @param bPath
     * @param stuffName
     * @return
     */
    public static String searchStuffByName(Context context,String bPath, String stuffName) {

        JSONObject query = new JSONObject();
        query.put("stuffName", stuffName);
        RequestBody req = RequestBody.create(jsonHeader, query.toJSONString());
        Request request = new Request.Builder().url(searchUrl).post(req).build();
        String dataStr = executeOK3Post(request, "data");
        if (dataStr == null){
            return "";
        }
        ArrayList<Stuff> stuffList = (ArrayList<Stuff>) JSON.parseArray(dataStr, Stuff.class);
        for (Stuff stuff : stuffList) {
            stuff.set_picture(bPath + "/" + stuff.get_picture());
        }
        dbController = new DBController(context);
        dbController.replaceAllStuff(stuffList);
        for (Stuff stuff : stuffList) {
            final File stuffPic = new File(stuff.get_picture());
            if (!stuffPic.exists()) {
                downloadImage(stuffPic);
            }
        }
        return stuffName;
    }


    /**
     * delete stuff from server
     *
     * @param stuff
     * @return
     */
    public static String deleteStuff(Stuff stuff) {
        JSONObject deleteStuff = new JSONObject();
        deleteStuff.put("id", stuff.get_id());
        setPicName(stuff);
        deleteStuff.put("pic", stuff.get_picture());
        RequestBody req = RequestBody.create(jsonHeader, deleteStuff.toJSONString());
        Request request = new Request.Builder().url(deleteUrl).post(req).build();
        return executeOK3Post(request, "code");
    }

    /**
     * update stuff in mysql
     *
     * @param stuff
     * @return
     */
    public static String updateStuff(Stuff stuff) {
        String localPath = stuff.get_picture();
        setPicName(stuff);
        String jsonString = JSON.toJSONString(stuff);
        stuff.set_picture(localPath);
        RequestBody req = RequestBody.create(jsonHeader, jsonString);
        Request request = new Request.Builder().url(updateUrl).post(req).build();
        return executeOK3Post(request, "code");
    }

    private static void setPicName(Stuff stuff) {
        String fileName = stuff.get_picture();
        fileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());
        stuff.set_picture(fileName);
    }


    private static String executeOK3Post(Request request, String key) {
        OkHttpClient client = new OkHttpClient();
        String res;
        try {
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String resJSONString = response.body().string();
                JSONObject ret = JSON.parseObject(resJSONString);
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
