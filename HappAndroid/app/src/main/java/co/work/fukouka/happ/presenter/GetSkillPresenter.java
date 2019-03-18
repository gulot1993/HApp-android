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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.work.fukouka.happ.model.Notification;
import co.work.fukouka.happ.model.Skill;
import co.work.fukouka.happ.model.User;
import co.work.fukouka.happ.utils.JsonObjectRequest;
import co.work.fukouka.happ.utils.SessionManager;
import co.work.fukouka.happ.utils.SystemValuesEn;
import co.work.fukouka.happ.utils.SystemValuesJp;
import co.work.fukouka.happ.view.GetSkillView;

public class GetSkillPresenter {

    private Context mContext;
    private SessionManager mSession;
    private RequestQueue requestQueue;
    private GetSkillView mView;
    private DatabaseReference mDatabase;

    private List<Skill> skills = new ArrayList<>();
    private String name;
    private String photoUrl;
    private String firUids = "";

    public GetSkillPresenter(Context context, GetSkillView view) {
        this.mView = view;
        this.mContext = context;
        mSession = new SessionManager(context);
        requestQueue = Volley.newRequestQueue(context);
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public String getUserId() {
        String userId;

        HashMap<String, String> user = mSession.getUserId();
        userId = user.get(SessionManager.KEY_USER_ID);

        return userId;
    }

    private String getFbUid() {
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

    public void getSkill() {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_skill");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("with_cat", "1");

        final JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
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

                                        String category = object.getString("name");
                                        List<Skill> skillList = new ArrayList<>();
                                        JSONArray array = object.getJSONArray("children");
                                        if (array != null && array.length() > 0) {
                                            for (int j = 0; j < array.length(); j++) {
                                                JSONObject jsonObject = array.getJSONObject(j);
                                                int postId = jsonObject.getInt("post_id");
                                                String name = jsonObject.getString("name");

                                                Skill skill = new Skill(postId, name);
                                                skillList.add(skill);
                                            }
                                        }
                                        Skill skill = new Skill(category, skillList);
                                        mView.onGetSkill(skill);
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

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(jsObjRequest);
    }

    public void writeNotification(final int id, final String type, final String skills,
                                  final List<String> blockedIds) {
        final Map<String, String> timestamp = ServerValue.TIMESTAMP;
        final List<String> skillList = Arrays.asList(skills.split(","));
        
        mDatabase.child("users").child(getFbUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        name = null;
                        photoUrl = null;

                        if (user != null) {
                            name = user.getName();
                            photoUrl = user.getPhotoUrl();
                        }

                        Notification notifFreeTime = new Notification(id, getFbUid(), name,
                                photoUrl, type, timestamp);

                        final DatabaseReference pushRef = mDatabase.child("notifications").child("push-notification");
                        DatabaseReference appRef = mDatabase.child("notifications").child("app-notification");

                        final String pushedKey = mDatabase.child("notifications").push().getKey();

                        appRef.child("notification-all").child(pushedKey).setValue(notifFreeTime);

                        //TODO: This should be on type timeline
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                        ref.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                    String key = snapshot.getKey();
                                    User mUser = snapshot.getValue(User.class);
                                    String skills = mUser.getSkills();

                                    if (skills != null && !skills.equals("")) {
                                        for(String skill: skillList) {
                                            skill = ","+skill+",";
                                            if (skills.contains(skill)) {
                                                if (!key.equals(getFbUid())) {
                                                    if (!blockedIds.contains(key)) {
                                                        firUids += key + ",";
                                                        //Ready to push data
                                                        pushNotification(key, pushedKey);
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    } else {
                                        if (!key.equals(getFbUid()) && !blockedIds.contains(key)) {
                                            firUids += key + ",";
                                            pushNotification(key, pushedKey);
                                        }
                                    }
                                }
                                if (firUids != null && !firUids.equals("")) {
                                    firUids = firUids.substring(0, firUids.length() - 1);

                                    SystemValuesEn mValuesEn = new SystemValuesEn(mContext);
                                    SystemValuesJp mValuesJp = new SystemValuesJp(mContext);
                                    String timelineMessEn = mValuesEn.getSystemValue("notif_timeline_mess");
                                    String timelineMessJp = mValuesJp.getSystemValue("notif_timeline_mess");
                                    String freetimeMessEn = mValuesEn.getSystemValue("notif_freetime_mess");
                                    String freetimeMessJp = mValuesJp.getSystemValue("notif_freetime_mess");

                                    Notification notifTimeline = new Notification(id, getFbUid(), name,
                                            photoUrl, type, skills, firUids, timelineMessEn, timelineMessJp, timestamp);

                                    Notification freeTime = new Notification(id, getFbUid(), name,
                                            photoUrl, type, freetimeMessEn, freetimeMessJp, timestamp);

//                                    if (type.equals("timeline")) {
//                                        pushRef.child("timeline").setValue(notifTimeline);
//                                    } else {
//                                        pushRef.child("free-time").setValue(freeTime);
//                                    }
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

    private void pushNotification(final String key, String pushedKey) {
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

        final DatabaseReference mRef = mDatabase
                .child("user-badge").child("timeline");

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
    }

}
