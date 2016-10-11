package edu.asu.artag.Helper;


import android.app.Activity;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class VolleyHelper {

    private Context mContext;
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;

    // 利用Volley实现Post请求
    public void volley_post(Activity activity) {
        String url = "http://aplesson.com/wap/api/user.php?action=login";
        mContext = activity;
        mRequestQueue = Volley.newRequestQueue(mContext);
        mStringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("请求结果:" + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("请求错误:" + error.toString());
            }
        }) {
            // 携带参数
            @Override
            protected HashMap<String, String> getParams()
                    throws AuthFailureError {
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("un", "852041173");
                hashMap.put("pw", "852041173abc");
                return hashMap;
            }

            // Volley请求类提供了一个 getHeaders（）的方法，重载这个方法可以自定义HTTP 的头信息。（也可不实现）
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }

        };

        mRequestQueue.add(mStringRequest);

    }



    // 利用Volley实现Get请求
    public void volley_get(Activity activity) {
        mContext = activity;
        String url = "http://www.aplesson.com/";
        // 1 创建RequestQueue对象
        mRequestQueue = Volley.newRequestQueue(mContext);
        // 2 创建StringRequest对象
        mStringRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("请求结果:" + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("请求错误:" + error.toString());
            }
        });
        // 3 将StringRequest添加到RequestQueue
        mRequestQueue.add(mStringRequest);
    }


}
