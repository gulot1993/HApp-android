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

import co.work.fukouka.happ.interfaces.UserSearch;
import co.work.fukouka.happ.model.User;
import co.work.fukouka.happ.utils.JsonObjectRequest;
import co.work.fukouka.happ.utils.SessionManager;
import co.work.fukouka.happ.view.UserSearchView;


public class UserSearchPresenter implements UserSearch {

    private UserSearchView mView;
    private Context mContext;
    private SessionManager mSession;
    private RequestQueue requestQueue;

    public UserSearchPresenter(UserSearchView view, Context context) {
        this.mView = view;
        this.mContext = context;
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

        HashMap<String, String> user = mSession.getUserId();
        userId = user.get(SessionManager.KEY_USER_ID);

        return userId;
    }

    public void searchUser(final String name) {
        Map<String,String> params = new HashMap<String, String>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","user_search");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("name", name);
        params.put("h_id", name);
        params.put("user_id_for_block", getUserId());
        params.put("offset", "");
        params.put("number", "");

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray jsonArray = null;
                        try {
                            jsonArray = response.getJSONArray("result");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                List<User> userList = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    if (object != null) {
                                        int id = object.getInt("user_id");
                                        String happId = object.getString("h_id");
                                        String name = object.getString("name");
                                        String photoUrl = object.getString("icon");
                                        String email = object.getString("email");
                                        String message = object.getString("mess");
                                        String skills = object.getString("skills");

                                        User user = new User(id, happId, name, photoUrl, email, message, skills);
                                        userList.add(user);
                                    }
                                }
                                mView.onUserFound(userList);
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
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsObjRequest);
    }
}
