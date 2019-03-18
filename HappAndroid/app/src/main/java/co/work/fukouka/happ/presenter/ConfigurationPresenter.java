package co.work.fukouka.happ.presenter;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import co.work.fukouka.happ.model.User;
import co.work.fukouka.happ.utils.CustomLoadResource;
import co.work.fukouka.happ.utils.HappPreference;
import co.work.fukouka.happ.utils.JsonObjectRequest;
import co.work.fukouka.happ.utils.SessionManager;
import co.work.fukouka.happ.utils.SystemValuesEn;
import co.work.fukouka.happ.utils.SystemValuesJp;
import co.work.fukouka.happ.view.ConfigurationView;


public class ConfigurationPresenter {

    private static final String TAG = "ConfigurationPresenter";

    private Context mContext;
    private ConfigurationView mView;
    private SessionManager mSession;
    private DatabaseReference mDatabase;
    private CustomLoadResource mRes;
    private RequestQueue requestQueue;
    private HappPreference mPref;

    private String skillIds = "";

    public ConfigurationPresenter(ConfigurationView view, Context context) {
        this.mView = view;
        this.mContext = context;
        mSession = new SessionManager(context);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mRes = new CustomLoadResource(context);
        requestQueue = Volley.newRequestQueue(context);
        mPref = new HappPreference(context);
    }

    public ConfigurationPresenter(Context context) {
        this.mContext = context;
        mSession = new SessionManager(context);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mRes = new CustomLoadResource(context);
        requestQueue = Volley.newRequestQueue(context);
        mPref = new HappPreference(context);
    }

    public String getUserId() {
        String userId;

        HashMap<String, String> user = mSession.getUserId();
        userId = user.get(SessionManager.KEY_USER_ID);

        return userId;
    }

    public String getUfbId() {
        String userId = null;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }
        return userId;
    }

    public void checkUinfoUpdates(String happId) {
        Map<String,String> params = new HashMap<String, String>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_userinfo");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", happId);

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
                            String email = object.getString("email");
                            String skills = object.getString("skills");
                            String lang = object.getString("lang");

                            if (!skills.equals("")) {
                                skills = ","+skills+",";
                            }

                            User user = new User(id, name, email, photoUrl, skills, lang);
                            //save updates to firebase
                            mDatabase.child("users").child(getUfbId()).setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            String token = FirebaseInstanceId.getInstance().getToken();
                                            if (token != null) {
                                                sendRegistrationToServer(token);
                                            }
                                        }
                                    });

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

    public String getUserEmail() {
        String email;

        HashMap<String, String> user = mSession.getUserEmail();
        email = user.get(SessionManager.EMAIL);

        return email;
    }

    private String getUserPassword() {
        String password;
        HashMap<String, String> pass = mSession.getUserPassword();
        password = pass.get(SessionManager.PASSWORD);

        return  password;
    }

    public String getLanguage() {
        String lang;

        HashMap<String, String> user = mSession.getLanguage();
        lang = user.get(SessionManager.LANGUAGE);

        return lang;
    }

    public void getUserInfo(String userId) {
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
                            String happId = object.getString("h_id");
                            String name = object.getString("name");
                            String photoUrl = object.getString("icon");
                            String email = object.getString("email");
                            String message = object.getString("mess");
                            String skills = object.getString("skills");


                            User user = new User(id, happId,name, photoUrl, email, message, skills);
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

        requestQueue.add(jsObjRequest);
    }

    public void changeLanguage(String language) {
        Locale locale = new Locale(language);
        Resources res = mContext.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(locale);
        res.updateConfiguration(conf, dm);

        //add language to session
        mSession.saveSelectedLanguage(language);
        //save user language to wordpress
        updateUserLanguage(getUserId(), language);
    }

    private void updateUserLanguage(final String userId, String language) {
        if (userId != null) {
            Map<String,String> params = new HashMap<>();
            params.put("sercret" ,"jo8nefamehisd");
            params.put("action" ,"api");
            params.put("ac", "user_update");
            params.put("d", "0");
            params.put("lang", getLanguage());
            params.put("user_id", userId);
            params.put("change_lang", language);
            params.put("targets", "change_lang");

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                boolean success = response.getBoolean("success");
                                if (success) {
                                    checkUinfoUpdates(userId);
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
    }

    public void changeEmail(String userId, final String email) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","user_update");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", userId);
        params.put("email",email);
        params.put("targets","email");

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                mView.onSuccess();
                                //changeFbEmail(email);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mRes.hideProgressDialog();
                            mView.onFailed();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mRes.hideProgressDialog();
            }
        });

        requestQueue.add(jsObjRequest);
    }

    private void changeFbEmail(final String email) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(getUserEmail(), getUserPassword());

        if (user != null) {
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mRes.hideProgressDialog();
                                            mView.onSuccess();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                mRes.hideProgressDialog();
                                                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mRes.hideProgressDialog();
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }


    private void sendRegistrationToServer(String token) {
        if (getUfbId() != null) {
            mDatabase.child("registration-token").child(getUfbId()).child("token").setValue(token);
        }
    }

    public void getSystemValues() {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_system_value_all");
        params.put("d","0");
        params.put("lang", "en");

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("Response", response.toString());
                        try {
                            JSONArray jsonArray = response.getJSONArray("result");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                int arrayLength = jsonArray.length();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i)
                                            .getJSONObject("fields");

                                    String key = object.getString("key");
                                    String valueJp = object.getString("value_jp");
                                    String valueEn = object.getString("value_en");

                                    SystemValuesJp systemJp = new SystemValuesJp(mContext);
                                    SystemValuesEn systemEn = new SystemValuesEn(mContext);

                                    //save system values
                                    systemJp.saveSystemValue(key, valueJp);
                                    systemEn.saveSystemValue(key, valueEn);

                                    System.out.println("Counter " + i + " Value " +valueEn);

                                    if (i == arrayLength - 1) {
                                        mView.onSuccess();
                                    }

                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mView.onSuccess();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                mView.onFailed();
            }
        });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsObjRequest);
    }

    public void getSystemSkills() {
        Map<String,String> params = new HashMap<String, String>();
        params.put("sercret", "jo8nefamehisd");
        params.put("action", "api");
        params.put("ac", "get_skill");
        params.put("d", "0");
        params.put("with_cat", "");

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
                                        JSONObject object = jsonArray.getJSONObject(i)
                                                .getJSONObject("fields");

                                        String skillId = String.valueOf(jsonArray.getJSONObject(i).getInt("ID"));
                                        String key = object.getString("key");
                                        String valueJp = object.getString("skill_name_jp");
                                        String valueEn = object.getString("skill_name_en");

                                        SystemValuesJp systemJp = new SystemValuesJp(mContext);
                                        SystemValuesEn systemEn = new SystemValuesEn(mContext);

                                        //save system values
                                        systemJp.saveSystemValue(skillId, valueJp);
                                        systemEn.saveSystemValue(skillId, valueEn);

                                        System.out.println("Value skills en " +valueEn);
                                        System.out.println("Value skills jp " +valueJp);
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
                //mRes.hideProgressDialog();
            }
        });

        requestQueue.add(jsObjRequest);
    }

    public void saveUserSkills() {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_userinfo");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", getUserId());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //get user info
                            JSONObject object = response.getJSONObject("result");
                            String skills = object.getString("skills");

                            //save skills to preference
                            new HappPreference(mContext).storeSkillIds(skills);
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

    public void saveTokenToServer(String token) {
        mDatabase.child("registration-token").child(getUfbId())
                .child("token").setValue(token)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        HappPreference pref = new HappPreference(mContext);
                        pref.removeToken();
                    }
                });
    }

}
