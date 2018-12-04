package com.ont.player.sample.network;

import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by betali on 2018/3/19.
 */

public class NetworkClient {

    public static void doRequest(final OntPlayerRequest ontPlayerRequest) {

        AsyncTask<String, Integer, ResultObject> task = new AsyncTask<String, Integer, ResultObject>() {

            @Override
            protected ResultObject doInBackground(String... strings) {

                URL url = null;
                HttpURLConnection conn = null;
                byte[] responseBody = null;
                ResultObject responseErr = null;

                try {
                    url = new URL(ontPlayerRequest.getRequestUrl());
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod(ontPlayerRequest.getRequestType());
                    conn.setDoInput(true);
                    conn.setUseCaches(false);

                    //set header
                    conn.setRequestProperty("api-key", ontPlayerRequest.getApiKey());
                    conn.setRequestProperty("Content-Type", "application/json");

                    String body = ontPlayerRequest.getRequestBody();
                    if(!TextUtils.isEmpty(body)) {

                        conn.getOutputStream().write(body.getBytes());
                    }

                    conn.connect();
                    InputStream is = conn.getInputStream();
                    responseBody = getBytesByInputStream(is);
                } catch (Exception e) {

                    responseErr = new ResultObject();
                    responseErr.success = false;
                    responseErr.response = e.toString();
                } finally {

                    if (conn != null) {

                        conn.disconnect();
                    }
                }

                if (responseErr != null) {

                    return responseErr;
                }

                responseErr = new ResultObject();
                responseErr.success = true;
                responseErr.response = getStringByBytes(responseBody);
                return responseErr;
            }

            @Override
            protected void onPostExecute(ResultObject s) {

                super.onPostExecute(s);
                if (ontPlayerRequest.getApiListener() != null) {

                    ontPlayerRequest.getApiListener().onComplete(s.success, s.response);
                }
            }
        }.execute();
    }

    public static void doUpload(final OntPlayerRequest ontPlayerRequest) {

        AsyncTask<String, Integer, ResultObject> task = new AsyncTask<String, Integer, ResultObject>() {
            @Override
            protected ResultObject doInBackground(String... strings) {

                URL url = null;
                HttpURLConnection conn = null;
                FileInputStream inputStream = null;
                byte[] responseBody = null;
                ResultObject responseErr = null;

                try {

                    // 打开连接
                    url = new URL(ontPlayerRequest.getRequestUrl());
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod(ontPlayerRequest.getRequestType());
                    conn.setDoInput(true);
                    conn.setUseCaches(false);

                    //set header
                    conn.setRequestProperty("api-key", ontPlayerRequest.getApiKey());
                    conn.setRequestProperty("Content-Type", "application/octet-stream");

                    // 写文件
                    String filePath = ontPlayerRequest.getRequestBody();
                    File file = new File(filePath);
                    inputStream = new FileInputStream(file);

                    byte[] buffer = new byte[1024];
                    int len = 0;
                    OutputStream os = conn.getOutputStream();
                    while((len = inputStream.read(buffer, 0, 1024)) != -1){

                        os.write(buffer, 0, len);
                    }
                    os.flush();
                    inputStream.close();

                    // 取返回值
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    responseBody = getBytesByInputStream(is);
                } catch (Exception e) {

                    responseErr = new ResultObject();
                    responseErr.success = false;
                    responseErr.response = e.toString();
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }

                if (responseErr != null) {

                    return responseErr;
                }

                responseErr = new ResultObject();
                responseErr.success = true;
                responseErr.response = getStringByBytes(responseBody);
                return responseErr;
            }

            @Override
            protected void onPostExecute(ResultObject s) {

                super.onPostExecute(s);
                if (ontPlayerRequest.getApiListener() != null) {

                    ontPlayerRequest.getApiListener().onComplete(s.success, s.response);
                }
            }
        }.execute();
    }

    private static String getStringByBytes(byte[] bytes) {
        if(null == bytes)
            return null;
        String str = "";
        try {
            str = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

    private static byte[] getBytesByInputStream(InputStream is) {
        byte[] bytes = null;
        BufferedInputStream bis = new BufferedInputStream(is);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos);
        byte[] buffer = new byte[1024 * 8];
        int length = 0;
        try {
            while ((length = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, length);
            }
            bos.flush();
            bytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bytes;
    }

    private static class ResultObject {

        boolean success;
        String response;
    }
}
