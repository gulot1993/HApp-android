package co.work.fukouka.happ.presenter;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.model.Post;
import co.work.fukouka.happ.utils.JsonObjectRequest;
import co.work.fukouka.happ.utils.SessionManager;
import co.work.fukouka.happ.view.NotifDetailView;

public class NotifDetailsPresenter {

    private Context mContext;
    private NotifDetailView mView;
    private SessionManager mSession;
    private RequestQueue requestQueue;
    private HappHelper mHelper;

    public NotifDetailsPresenter(Context context) {
        this.mContext = context;
        mSession = new SessionManager(context);
        requestQueue = Volley.newRequestQueue(context);
        mHelper = new HappHelper(context);
    }

    public NotifDetailsPresenter(Context context, NotifDetailView mView) {
        this.mContext = context;
        this.mView = mView;
        mSession = new SessionManager(context);
        requestQueue = Volley.newRequestQueue(context);
        mHelper = new HappHelper(context);
    }

    public String getUserId() {
        String userId;

        HashMap<String, String> user = mSession.getUserId();
        userId = user.get(SessionManager.KEY_USER_ID);

        return userId;
    }

    public String getFbUid() {
        String user = null;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            user = currentUser.getUid();
        }

        return user;
    }

    public String getLanguage() {
        String language;

        HashMap<String, String> lang = mSession.getLanguage();
        language = lang.get(SessionManager.LANGUAGE);

        return language;
    }


    public void getNotifDetails(int id, String skills) {
        Map<String, String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_timeline");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", getUserId());
        params.put("post_id", String.valueOf(id));
        params.put("count", "1");
        params.put("skills", skills);

        final JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("result");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i= 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    int postId = object.getInt("ID");
                                    String dateModified = object.getString("post_modified");
                                    JSONObject fields = object.getJSONObject("fields");
                                    String skills = fields.getString("skills");
                                    String body = fields.getString("body");
                                    String fromUserId = fields.getString("from_user_id");
                                    List<String> images = new ArrayList<>();

                                    // Convert to japanese date
                                    if (dateModified != null && getLanguage().equals("jp")) {
                                        String[] dateArray = dateModified.split("\\s+");
                                        String date = dateArray[0];
                                        String time = dateArray[1];

                                        date = mHelper.getJapaneseDate(date);

                                        dateModified = date + " " + time;
                                    }

                                    try {
                                        JSONArray imageArray = fields.getJSONArray("images");
                                        if (imageArray != null && imageArray.length() > 0) {
                                            for (int j = 0; j < imageArray.length(); j++) {
                                                JSONObject imageObject = imageArray.getJSONObject(j);
                                                JSONObject jsonObject = imageObject.getJSONObject("image");
                                                JSONObject obj = jsonObject.getJSONObject("sizes");

                                                String image = jsonObject.getString("url");
                                                images.add(image);
                                                //System.out.println("Post "+body+ " Thumbnail " +thumbnail);
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    Post post = new Post(postId, dateModified, skills, body,
                                            fromUserId, images);

                                    //Check if blocked
                                    isBlocked(fromUserId, post);
                                }
                            } else {
                                String message = new GetSystemValue(mContext).getValue("already_deleted_post_mess");
                                mView.onFailed(message);
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
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        requestQueue.add(jsObjRequest);
    }

    private void isBlocked(String id, final Post post) {
        Map<String, String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_block_list");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", id);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean isBlocked = false;
                    JSONArray array = response.getJSONArray("result");
                    if (array != null && array.length() > 0) {
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i).getJSONObject("fields");
                            String blockedId = jsonObject.getString("block_user_id");
                            if (blockedId.equals(getUserId())) {
                                isBlocked = true;

                                break;
                            }
                        }
                    }

                    if (!isBlocked) {
                        mView.onLoadPostNotif(post);
                    } else {
                        mView.onFailed(mHelper.notAllowedToView());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        requestQueue.add(jsObjRequest);

    }
}
