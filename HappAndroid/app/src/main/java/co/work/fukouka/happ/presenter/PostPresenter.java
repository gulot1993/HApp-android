package co.work.fukouka.happ.presenter;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.model.Notification;
import co.work.fukouka.happ.model.Post;
import co.work.fukouka.happ.model.User;
import co.work.fukouka.happ.utils.HappPreference;
import co.work.fukouka.happ.utils.JsonObjectRequest;
import co.work.fukouka.happ.utils.SessionManager;
import co.work.fukouka.happ.utils.SystemValuesEn;
import co.work.fukouka.happ.utils.SystemValuesJp;
import co.work.fukouka.happ.view.PostView;

public class PostPresenter {
    private static final String TAG = "PostPresenter";
    public static int notifCount = 0;

    private Context mContext;
    private PostView mView;
    private SessionManager mSession;
    private List<Post> postList;
    private List<Post> newPostList;
    private List<Post> posts;
    private List<Post> newPosts;
    private RequestQueue requestQueue;
    private HappPreference pref;
    private DatabaseReference mDatabase;
    private HappHelper mHelper;

    public static boolean hasLoadedAllItems = false;
    private int postSize = 0;
    private int counter2 = 0;
    private int counter1 = 0;
    private int listSize = 0;
    private int newListSize = 0;

    private String fUids = "";
    private List<String> userIds;
    private List<String> blockIds;

    public PostPresenter(Context context, PostView view) {
        this.mContext = context;
        this.mView = view;
        mSession = new SessionManager(context);
        requestQueue = Volley.newRequestQueue(context);
        pref = new HappPreference(context);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mHelper = new HappHelper(context);
    }

    public PostPresenter(Context context) {
        this.mContext = context;
        mSession = new SessionManager(context);
        requestQueue = Volley.newRequestQueue(context);
        pref = new HappPreference(context);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mHelper = new HappHelper(context);
    }

    private interface VolleyCallback {
        void onSuccess(User user);
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

    public void getPosts(final String userId, final String page, String skills, final String action) {
        posts = new ArrayList<>();

        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_timeline");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", userId);
        params.put("from_id", userId);
        params.put("page", page);
        params.put("count", "5");
        params.put("skills", skills);
        params.put("origin", "timeline");

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("result");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                //instantiate Post object
                                postList = new ArrayList<>();
                                for (int i= 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    //get timeline data
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
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    if (i == 0 && page.equals("1")) {
                                        HappPreference pref = new HappPreference(mContext);
                                        pref.storeFirstPostId(postId);
                                    }

                                    Post post = new Post(postId, dateModified, skills,  body,  fromUserId, images);
                                    //getPostItem(post);
                                    //post.setImageUrl(imageUrl);
                                    postList.add(post);
                                }

                                //get each post details
                                listSize = postList.size();
                                getPostItem(postList.get(0).getFromUserId(), action);

                                if (jsonArray.length() < 5) {
                                    mView.onFailed("");
                                    hasLoadedAllItems = true;
                                }
                            } else {
                                mView.onFailed("");
                                hasLoadedAllItems = true;
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
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsObjRequest);
    }

    public void getNewPost(final String caller, final String skill) {
        final int currentPostId = new HappPreference(mContext).getFirstPostId();
        newPosts = new ArrayList<>();

        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_timeline");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", getUserId());
        params.put("from_id", getUserId());
        params.put("page", "1");
        params.put("count", "10");
        params.put("skills", skill);
        params.put("origin", "timeline");

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("result");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                //instantiate Post object
                                newPostList = new ArrayList<>();
                                for (int i= 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    //get timeline data
                                    int postId = object.getInt("ID");


                                    if (currentPostId != postId) {
                                        String dateModified = object.getString("post_modified");
                                        JSONObject fields = object.getJSONObject("fields");
                                        String skills = fields.getString("skills");
                                        String body = fields.getString("body");
                                        String fromUserId = fields.getString("from_user_id");
                                        //String imageUrl = null;
                                        List<String> images = new ArrayList<>();

                                        try {
                                            JSONArray imageArray = fields.getJSONArray("images");
                                            if (imageArray != null && imageArray.length() > 0) {
                                                for (int j = 0; j < imageArray.length(); j++) {
                                                    JSONObject imageObject = imageArray.getJSONObject(j);
                                                    JSONObject jsonObject = imageObject.getJSONObject("image");
                                                    JSONObject obj = jsonObject.getJSONObject("sizes");

                                                    String image = jsonObject.getString("url");
                                                    images.add(image);
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        Post post = new Post(postId, dateModified, skills, body,
                                                fromUserId, images);

                                        newPostList.add(post);

                                        if (i == 0) {
                                            pref.storeFirstPostId(postId);
                                        }
                                    } else {
                                        if (!newPostList.isEmpty() && newPostList.size() > 0) {
                                            newListSize = newPostList.size();
                                            getPostItems(newPostList.get(0).getFromUserId(), caller);
                                        } else {
                                            //no new posts
                                            mView.onNoNewPost();
                                        }
                                        return;
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
                mView.onFailed(String.valueOf(error));
            }
        });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsObjRequest);
    }

    public void getLatestPost() {
        Map<String,String> params = new HashMap<String, String>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_timeline");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", getUserId());
        params.put("page", "1");
        params.put("count", "1");
        params.put("skills", "");

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("result");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i= 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    int postId = object.getInt("ID");

                                    new HappPreference(mContext).storeFirstPostId(postId);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mView.onFailed(String.valueOf(error));
            }
        });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        requestQueue.add(jsObjRequest);
    }

    private void getPostItem(final String fromUserId, final String action) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_userinfo");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", fromUserId);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject object;
                        try {
                            object = response.getJSONObject("result");
                            String name = object.getString("name");
                            String photoUrl = object.getString("icon");
                            int postId = postList.get(counter1).getPostId();
                            String dateMod = postList.get(counter1).getDateModified();
                            String body = postList.get(counter1).getBody();
                            List<String> images = postList.get(counter1).getImages();
                            String skills = postList.get(counter1).getSkills();
                            //String fromUserId = postList.get(counter1).getFromUserId();

                            Post post = new Post(postId, name,  photoUrl, dateMod,
                                    skills,  body,  fromUserId, images);
                            posts.add(post);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        counter1++;
                        if (counter1 < listSize) {
                            String fromUserId = getPostAuthorId(counter1);
                            getPostItem(fromUserId, action);
                        } else {
                            counter1 = 0;
                            mView.onLoadPosts(posts, action);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsObjRequest);
    }

    private void getPostItems(final String fromUserId, final String caller) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_userinfo");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", fromUserId);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject object;
                        try {
                            object = response.getJSONObject("result");
                            String name = object.getString("name");
                            String photoUrl = object.getString("icon");
                            int postId = newPostList.get(counter2).getPostId();
                            String dateMod = newPostList.get(counter2).getDateModified();
                            String body = newPostList.get(counter2).getBody();
                            List<String> images = newPostList.get(counter2).getImages();
                            String skills = newPostList.get(counter2).getSkills();

                            Post post = new Post(postId, name,  photoUrl, dateMod,
                                    skills,  body,  fromUserId, images);
                            newPosts.add(post);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        counter2 += 1;
                        if (counter2 < newListSize) {
                            String fromUserId = getNewPostId(counter2);
                            getPostItems(fromUserId, caller);
                        } else {
                            counter2 = 0;
                            Collections.reverse(newPosts);
                            if (caller.equals("author")) {
                                mView.appendAuthorPost(newPosts);
                            } else {
                                mView.appendNewPost(newPosts);
                            }
                            //mView.onLoadPosts(posts);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsObjRequest);
    }

    public void getUserInfo(String userId, final VolleyCallback callback) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_userinfo");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", userId);

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject object = response.getJSONObject("result");
                            String name = object.getString("name");
                            String photoUrl = object.getString("icon");

                            User user = new User(name, photoUrl);
                            callback.onSuccess(user);

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

        objectRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(objectRequest);
    }

    private String getPostAuthorId(int counter) {
        return postList.get(counter).getFromUserId();
    }

    private String getNewPostId(int counter) {
        return newPostList.get(counter).getFromUserId();
    }

    private void writeAppNotification(final int id, final String type, final String firIds,
                                   final List<String> blockIds) {
        final Map<String, String> timestamp = ServerValue.TIMESTAMP;

        mDatabase.child("users").child(getFbUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        String name = null;
                        String photoUrl = null;

                        if (user != null) {
                            name = user.getName();
                            photoUrl = user.getPhotoUrl();
                        }

                        Notification notif = new Notification(id, getFbUid(), name,
                                photoUrl, type, timestamp);

                        DatabaseReference appRef = mDatabase.child("notifications").child("app-notification");

                        final String pushedKey = mDatabase.child("notifications").push().getKey();

                        appRef.child("notification-all").child(pushedKey).setValue(notif);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                        ref.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                    final String key = snapshot.getKey();
                                    if (!key.equals(getFbUid())) {
                                        if (!blockIds.contains(key)) {
                                            mDatabase.child("notifications").child("app-notification")
                                                    .child("notification-user")
                                                    .child(key).child("notif-list")
                                                    .child(pushedKey).child("read")
                                                    .setValue(false);

                                            final DatabaseReference ref = mDatabase.child("notifications")
                                                    .child("app-notification")
                                                    .child("notification-user")
                                                    .child(key).child("unread")
                                                    .child("count");

                                            final DatabaseReference mRef = mDatabase
                                                    .child("user-badge").child("freetime");

                                            mRef.child(key)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.exists()) {
                                                                Long count = (Long) dataSnapshot.getValue();
                                                                if (count != null && !key.equals(getFbUid())) {
                                                                    mRef.child(key).setValue(count + 1);
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        Long count = (Long) dataSnapshot.getValue();
                                                        if (count != null) {
                                                            ref.setValue(count + 1);
                                                        }
                                                    } else {
                                                        ref.setValue(1);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void writePushNotification(final int id, final String type, final String firIds) {
        final Map<String, String> timestamp = ServerValue.TIMESTAMP;

        mDatabase.child("users").child(getFbUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        String name = null;
                        String photoUrl = null;

                        if (user != null) {
                            name = user.getName();
                            photoUrl = user.getPhotoUrl();
                        }

                        SystemValuesEn mValuesEn = new SystemValuesEn(mContext);
                        SystemValuesJp mValuesJp = new SystemValuesJp(mContext);
                        String freetimeMessEn = mValuesEn.getSystemValue("notif_freetime_mess");
                        String freetimeMessJp = mValuesJp.getSystemValue("notif_freetime_mess");

                        Notification freeTime = new Notification(id, firIds, getFbUid(), name,
                                photoUrl, type, freetimeMessEn, freetimeMessJp, timestamp);

                        DatabaseReference pushRef = mDatabase.child("notifications").child("push-notification");
                        pushRef.child("free-time").setValue(freeTime);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void deletePost(final int postId) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","delete_timeline");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("pid", String.valueOf(postId));

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                JSONObject object = response.getJSONObject("result");
                                String message = null;
                                if (object != null) {
                                    message = object.getString("mess");
                                }
                                mView.onSuccess(message);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            boolean error = response.getBoolean("error");
                            if (error) {
                                String message = response.getString("message");
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

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsObjRequest);

    }

    public void freetimeStatusOn(final String id) {
        userIds = new ArrayList<>();
        blockIds = new ArrayList<>();

        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","update_freetime_status");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", id);
        params.put("office_id", "32");
        params.put("status_key", "freetime");

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            mView.onSuccess(null);
                            if (success) {
                                final JSONObject jsonObj = response.getJSONObject("result");
                                final JSONArray jsonBlock = jsonObj.getJSONArray("blockid");
                                final JSONArray jsonArray = jsonObj.getJSONArray("freetime");

                                if (jsonBlock != null && jsonBlock.length() > 0) {
                                    for (int i = 0; i < jsonBlock.length(); i++) {
                                        blockIds.add(jsonBlock.getString(i));
                                    }
                                }

                                if (jsonArray != null && jsonArray.length() > 0) {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject object = jsonArray.getJSONObject(i);
                                        JSONObject fields = object.getJSONObject("fields");
                                        String userId = fields.getString("user_id");

                                        userIds.add(userId);
                                    }

                                    if (userIds != null && userIds.size() > 0) {
                                        if (userIds.contains(id)) {
                                            for (int i = 0; i < userIds.size(); i++) {
                                                String id = userIds.get(i);
                                                final int finalI = i;
                                                mDatabase.child("users").orderByChild("id").equalTo(Integer.parseInt(id))
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                                                    String fUid = snapshot.getKey();

                                                                    if (!fUid.equals(getFbUid())) {
                                                                        fUids += fUid + ",";
                                                                    }
                                                                    if (finalI == userIds.size() - 1) {
                                                                        if (!fUids.equals("")) {
                                                                            fUids = fUids.substring(0, fUids.length() - 1);
                                                                            writePushNotification(0, "free-time", fUids);
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                            }
                                            writeAppNotification(0, "free-time", fUids, blockIds);
                                        } else {
                                            mView.onFreetimeFailed();
                                        }
                                    }
                                } else {
                                    mView.onFreetimeFailed();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            boolean error = response.getBoolean("error");
                            if (error) {
                                String message = response.getString("message");
                                mView.onFreetimeFailed();
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

    public void getUserSessionStatus() {
        userIds = new ArrayList<>();

        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_freetime_status_for_me");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", getUserId());
        params.put("office_id", "32");
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
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject object = jsonArray.getJSONObject(i);
                                        JSONObject fields = object.getJSONObject("fields");
                                        String userId = fields.getString("user_id");

                                        userIds.add(userId);
                                    }

                                    if (userIds != null && userIds.size() > 0) {
                                        if (userIds.contains(getUserId())) {
                                            mView.isFree(true);
                                        } else {
                                            mView.isFree(false);
                                            //scFreetimeStatus.setChecked(false);
                                        }
                                    }
                                } else {
                                    mView.isFree(false);
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

    public void freetimeStatusOff(String userId) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","freetime_off");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", userId);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
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

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsObjRequest);
    }

}
