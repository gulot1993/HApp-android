package co.work.fukouka.happ.presenter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.model.MessageContent;
import co.work.fukouka.happ.model.Post;
import co.work.fukouka.happ.model.User;
import co.work.fukouka.happ.utils.JsonObjectRequest;
import co.work.fukouka.happ.utils.SessionManager;
import co.work.fukouka.happ.view.ManagePostView;
import co.work.fukouka.happ.view.ProfileView;


public class ProfilePresenter {

    private static final String TAG = "ProfilePresenter";

    private ProfileView mView;
    private ManagePostView mPview;
    private Context mContext;
    private SessionManager mSession;
    private DatabaseReference mDatabase;
    private  RequestQueue requestQueue;
    private HappHelper mHelper;

    private String chatroomId = null;
    public static String chatmateId;
    private int postCount = 0;
    private List<Post> postList;
    private List<Post> posts;
    private int listSize = 0;
    private int counter = 0;

    public static String chatroomIdNew;
    public static boolean hasLoadedAllItems;

    public ProfilePresenter(ProfileView mView, Context context) {
        this.mView = mView;
        this.mContext = context;
        mSession = new SessionManager(context);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        requestQueue = Volley.newRequestQueue(mContext);
        mHelper = new HappHelper(context);
    }

    public ProfilePresenter(Context context, ManagePostView mPview) {
        this.mPview = mPview;
        this.mContext = context;
        mSession = new SessionManager(context);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        requestQueue = Volley.newRequestQueue(mContext);
        mHelper = new HappHelper(context);
    }

    public ProfilePresenter(Context context) {
        this.mContext = context;
        mSession = new SessionManager(context);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        requestQueue = Volley.newRequestQueue(mContext);
        mHelper = new HappHelper(context);
    }

    public String getUserId() {
        String userId;

        HashMap<String, String> user = mSession.getUserId();
        userId = user.get(SessionManager.KEY_USER_ID);

        return userId;
    }

    public String getfbUid() {
        String userId = null;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }
        return userId;
    }

    public String getLanguage() {
        String lang;

        HashMap<String, String> hashmap = mSession.getLanguage();
        lang = hashmap.get(SessionManager.LANGUAGE);

        return lang;
    }

    public void getUserInfo(String userId) {
        Map<String,String> params = new HashMap<String, String>();
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
                            String happId = object.getString("h_id");
                            String name = object.getString("name");
                            String photoUrl = object.getString("icon");
                            String email = object.getString("email");
                            String message = object.getString("mess");
                            String skills = object.getString("skills");


                            User user = new User(id, happId, name, photoUrl, email, message, skills);
                            mView.onLoadUserInfo(user);
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

    public void getUserPost(final String userId, String skills, String page) {
        posts = new ArrayList<>();
        postList = new ArrayList<>();

        Map<String,String> params = new HashMap<String, String>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_timeline");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", userId);
        params.put("from_id", userId);
        params.put("count", "10");
        params.put("page", page);
        params.put("skills", "");

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("result");
                            if (jsonArray != null && jsonArray.length() > 0) {
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

                                    Post post = new Post(postId, "", "", dateModified, skills, body,
                                            fromUserId, images);
                                    postList.add(post);
                                   // postList.add(post);
                                    //getPostItem(post);
                                }

                                listSize = postList.size();
                                getPostItem(postList.get(0).getFromUserId());

                                if (jsonArray.length() < 10) {
                                    hasLoadedAllItems = true;
                                }

                               // mView.onLoadPost(postList);
                            } else {
                                hasLoadedAllItems = true;
                                mView.onGetPostFailed();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mView.onGetPostFailed();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                mView.onGetPostFailed();
            }
        });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsObjRequest);
    }

    private void getPostItem(final String fromUserId) {
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
                            int postId = postList.get(counter).getPostId();
                            String dateMod = postList.get(counter).getDateModified();
                            String body = postList.get(counter).getBody();
                            List<String> images = postList.get(counter).getImages();
                            String skills = postList.get(counter).getSkills();

                            Post post = new Post(postId, name,  photoUrl, dateMod,
                                    skills,  body,  fromUserId, images);
                            posts.add(post);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        counter++;
                        if (counter < listSize) {
                            String fromUserId = getPostAuthorId(counter);
                            getPostItem(fromUserId);
                        } else {
                            counter = 0;
                            mView.onLoadPost(posts);
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

    private String getPostAuthorId(int counter) {
        return postList.get(counter).getFromUserId();
    }

    public void blockUser(String idToblock) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","add_block");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", getUserId());
        params.put("block_user_id", idToblock);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //get user info
                            boolean success = response.getBoolean("success");
                            if (success) {
                                JSONObject object = response.getJSONObject("result");
                                String mess = object.getString("mess");

                                mView.userBlocked();
                                blockUserInFb(getChatroomId());
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            mView.onUpdateFailed("");
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

    public void reportUser(String fromUserId, String postId) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","add_report");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("from_id", fromUserId);
        params.put("report_post_id", postId);
        params.put("user_id", getUserId());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //get user info
                            boolean success = response.getBoolean("success");
                            if (success) {
                                JSONObject object = response.getJSONObject("result");
                                String mess = object.getString("mess");

                                mPview.onUserReported(mess);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            mView.onUpdateFailed("");
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

    public void blockUserFromTimeline(final String idToblock) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","add_block");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", getUserId());
        params.put("block_user_id", idToblock);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //get user info
                            boolean success = response.getBoolean("success");
                            if (success) {
                                JSONObject object = response.getJSONObject("result");
                                String mess = object.getString("mess");

                                mPview.onUserBlocked(mess);
                                blockUserInFbFromTimeline(getChatroomId(), idToblock);
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

    public void unBlockUser(String id) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","unlock_block");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", getUserId());
        params.put("block_user_id", id);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //get user info
                            JSONObject object = response.getJSONObject("result");
                            String mess = object.getString("mess");

                            mView.userUnblocked();
                            unBlockUserInFb(getChatroomId());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mView.onUpdateFailed("");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        requestQueue.add(jsObjRequest);
    }

    public void checkIfblocked(final String id) {
        Map<String,String> params = new HashMap<String, String>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_block_list");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", getUserId());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //get user info
                            JSONArray jsonArray = response.getJSONArray("result");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i)
                                            .getJSONObject("fields");
                                    String blockedUser = object.getString("block_user_id");
                                    if (blockedUser.equals(id)) {
                                        mView.userBlocked();
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

    public void updateUserInfoInFirebase() {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_userinfo");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", getUserId());

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //get user info
                            JSONObject object = response.getJSONObject("result");
                            int id = object.getInt("user_id");
                            String name = object.getString("name");
                            final String photoUrl = object.getString("icon");
                            String email = object.getString("email");
                            String skills = object.getString("skills");
                            String lang = object.getString("lang");

                            if (!skills.equals("")) {
                                skills = ","+skills+",";
                            }

                            final User user = new User(id, name, email, photoUrl, skills, lang);

                            //save updates to firebase
                            mDatabase.child("users").child(getfbUid()).setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                getUserChatroomId(user);
                                            }
                                        }
                                    });

                            //update user notification data
                            updateUserData(getfbUid(), name, photoUrl);

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

    private void updateUserData(String userId, final String name, final String photoUrl) {
        mDatabase.child("notifications").child("app-notification").child("notification-all")
                .orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            String key = snapshot.getKey();

                            DatabaseReference ref = mDatabase.child("notifications")
                                    .child("app-notification").child("notification-all")
                                    .child(key);

                            //Update data
                            ref.child("name").setValue(name);
                            ref.child("photoUrl").setValue(photoUrl);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        final DatabaseReference ref = mDatabase.child("chat").child("last-message");

        ref.child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            String key = snapshot.getKey();
                            MessageContent message = snapshot.getValue(MessageContent.class);
                            String chatmateId;
                            if (message != null) {
                                chatmateId = message.getChatmateId();
                                ref.child(chatmateId).child(key)
                                        .child("photoUrl").setValue(photoUrl);
                                ref.child(chatmateId).child(key)
                                        .child("name").setValue(name);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    //used to update user's info in messages ei. photoUrl, name
    private void getUserChatroomId(final User user) {
        mDatabase.child("chat").child("members").orderByChild(getfbUid()).equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                String chatroomId = snapshot.getKey();
                                updateUserChatData(chatroomId, user);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void updateUserChatData(final String chatroomId, User user) {
        Query messQuery = mDatabase.child("chat").child("messages").child(chatroomId)
                .orderByChild("userId").equalTo(getfbUid());

        final String name = user.getName();
        final String photoUrl = user.getPhotoUrl();

        messQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();

                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/chat/messages/" +chatroomId + "/" +key +"/photoUrl", photoUrl);
                    childUpdates.put("/chat/messages/" +chatroomId + "/" +key +"/name", name);

                    mDatabase.updateChildren(childUpdates);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getMessageThread(int happId) {
        mDatabase.child("users").orderByChild("id").equalTo(happId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            chatmateId = snapshot.getKey();
                            searchMessageThread(chatmateId);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public String getChatroomId() {
        return chatroomId;
    }

    /**
     * Search if there is existing message thread
     */
    //TODO: This logic wont work if the user message itself
    private void searchMessageThread(final String chatmateId) {
        mDatabase.child("chat").child("members").orderByChild(getfbUid()).equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            if (chatmateId.compareTo(getfbUid()) == 0) {
                                chatroomId = null;
                            } else {
                                if (snapshot.hasChild(chatmateId)) {
                                    chatroomId = snapshot.getKey();

                                    // Check if blocked
                                    if (mView != null) {
                                        //checkIfBlocked(chatroomId);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void blockUserInFb(String chatroomId) {
        if (chatroomId != null) {
            mDatabase.child("chat").child("members").child(chatroomId)
                    .child("blocked").setValue(true);
        } else {
            Map<String, Boolean> members = new HashMap<>();
            members.put(getfbUid(), true);
            members.put(chatmateId, true);
            members.put("blocked", true);

            String chatroom = mDatabase.child("chat").push().getKey();
            chatroomIdNew = chatroom;
            mDatabase.child("chat").child("members").child(chatroom).setValue(members)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String id = ((Activity) mContext).getIntent().getStringExtra("user_id");
                            if (id != null) {
                                getMessageThread(Integer.parseInt(id));
                            }
                        }
                    });
        }
    }

    private void blockUserInFbFromTimeline(String chatroomId, final String userId) {
        if (chatroomId != null) {
            mDatabase.child("chat").child("members").child(chatroomId)
                    .child("blocked").setValue(true);
        } else {
            Map<String, Boolean> members = new HashMap<>();
            members.put(getfbUid(), true);
            members.put(chatmateId, true);
            members.put("blocked", true);

            String chatroom = mDatabase.child("chat").push().getKey();
            chatroomIdNew = chatroom;
            mDatabase.child("chat").child("members").child(chatroom).setValue(members)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (userId != null) {
                                getMessageThread(Integer.parseInt(userId));
                            }
                        }
                    });
        }
    }

    private void unBlockUserInFb(String chatroomId) {
        if (chatroomId != null) {
            mDatabase.child("chat").child("members").child(chatroomId)
                    .child("blocked").setValue(false);
        }
    }

    private void checkIfBlocked(String chatroomId) {
        mDatabase.child("chat").child("members").child(chatroomId).child("blocked")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Boolean blocked = (Boolean) dataSnapshot.getValue();
                            if (blocked) {
                                mView.userBlocked();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
