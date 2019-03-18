package co.work.fukouka.happ.presenter;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.work.fukouka.happ.interfaces.Situation;
import co.work.fukouka.happ.model.Congestion;
import co.work.fukouka.happ.model.User;
import co.work.fukouka.happ.utils.JsonObjectRequest;
import co.work.fukouka.happ.utils.SessionManager;
import co.work.fukouka.happ.view.SituationView;


public class SituationPresenter implements Situation {

    private Context mContext;
    private SituationView mView;
    private SessionManager mSession;
    private RequestQueue requestQueue;

    private List<String> idList = new ArrayList<>();

    private int userFreeCount;

    public SituationPresenter(Context context, SituationView view) {
        this.mContext = context;
        this.mView = view;
        mSession = new SessionManager(context);
        requestQueue = Volley.newRequestQueue(mContext);
    }

    public String getUserId() {
        String userId;
        HashMap<String, String> user = mSession.getUserId();
        userId = user.get(SessionManager.KEY_USER_ID);

        return userId;
    }

    public String getLanguage() {
        String userId;

        HashMap<String, String> user = mSession.getLanguage();
        userId = user.get(SessionManager.LANGUAGE);

        return userId;
    }

    public void getCongestion(String officeId) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_congestion");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("office_id", officeId);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                JSONArray jsonArray = response.getJSONArray("result");
                                if (jsonArray != null && jsonArray.length() > 0) {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject object = jsonArray.getJSONObject(i);
                                        int id = object.getInt("ID");
                                        String dateMod = object.getString("post_modified");
                                        JSONObject fields = object.getJSONObject("fields");
                                        String percentage = fields.getString("persentage");

                                        Congestion congestion = new Congestion(id, dateMod, percentage);
                                        mView.loadCongestion(congestion);
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        requestQueue.add(jsObjRequest);
    }

    /**
     * Get freetime status
     */
    public void getAvailableUser(String officeId) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_freetime_status_for_me");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", getUserId());
        params.put("office_id", officeId);
        params.put("status_key", "freetime");

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                JSONArray jsonArray = response.getJSONArray("result");
                                if (jsonArray != null && jsonArray.length() > 0) {
                                    userFreeCount = jsonArray.length();

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject object = jsonArray.getJSONObject(i);
                                        JSONObject fields = object.getJSONObject("fields");
                                        String userId = fields.getString("user_id");

                                        if (!idList.contains(userId)) {
                                            idList.add(userId);
                                            getUserInfo(userId);
                                        }
                                    }
                                } else {
                                    mView.isLoaded();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                mView.isLoaded();
            }
        });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsObjRequest);
    }

    public void refreshAvailableUser(String officeId, final boolean dataChanged) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_freetime_status_for_me");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", getUserId());
        params.put("office_id", officeId);
        params.put("status_key", "freetime");

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                JSONArray jsonArray = response.getJSONArray("result");
                                if (jsonArray != null && jsonArray.length() > 0) {
                                    int length = jsonArray.length();
                                    if (length > userFreeCount) {
                                        idList = new ArrayList<>();
                                        userFreeCount = length;
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject object = jsonArray.getJSONObject(i);
                                            JSONObject fields = object.getJSONObject("fields");
                                            String userId = fields.getString("user_id");

                                            if (!idList.contains(userId)) {
                                                idList.add(userId);
                                                getUserInfo(userId);
                                            }
                                        }
                                    }

                                    if (length < userFreeCount || dataChanged) {
                                        userFreeCount = length;
                                        idList = new ArrayList<>();
                                        mView.clearList();
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject object = jsonArray.getJSONObject(i);
                                            JSONObject fields = object.getJSONObject("fields");
                                            String userId = fields.getString("user_id");

                                            if (!idList.contains(userId)) {
                                                idList.add(userId);
                                                getUserInfo(userId);
                                            }
                                        }
                                    }
                                } else {
                                    userFreeCount = 0;
                                    mView.clearList();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsObjRequest);
    }

    private void getUserInfo(final String userId) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_userinfo");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", userId);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //get user info
                            JSONObject object = response.getJSONObject("result");
                            int id = object.getInt("user_id");
                            String name = object.getString("name");
                            String photoUrl = object.getString("icon");

                            User user = new User(id, name, photoUrl);
                            mView.loadAvailableUser(user);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsObjRequest);

    }
}
