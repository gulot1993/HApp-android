package co.work.fukouka.happ.presenter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import co.work.fukouka.happ.model.User;
import co.work.fukouka.happ.utils.HappPreference;
import co.work.fukouka.happ.utils.JsonObjectRequest;
import co.work.fukouka.happ.utils.SessionManager;
import co.work.fukouka.happ.view.AuthenticationView;

public class AuthenticationPresenter {

    private static final String TAG = "AuthenticationPresenter";

    private AuthenticationView mView;
    private Context mContext;
    private SessionManager mSession;
    private RequestQueue requestQueue;

    //Firebase instance
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public AuthenticationPresenter(AuthenticationView mView, Context context) {
        this.mView = mView;
        this.mContext = context;
        mSession = new SessionManager(context);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        requestQueue = Volley.newRequestQueue(mContext);
    }

    public String getUserEmail() {
        String email;

        HashMap<String, String> user = mSession.getUserEmail();
        email = user.get(SessionManager.EMAIL);

        return email;
    }

    public String getLanguage() {
        String language;

        HashMap<String, String> lang = mSession.getLanguage();
        language = lang.get(SessionManager.LANGUAGE);

        return language;
    }

    public String getUfbId() {
        String userId = null;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        return userId;
    }

    public void register(String email, String name, String password, String skills, String lang) {
        Map<String,String> params = new HashMap<String, String>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","user_update");
        params.put("d","0");
        params.put("lang",getLanguage());
        params.put("user_id","");
        params.put("email",email);
        params.put("passwd",password);
        params.put("name",name);
        params.put("mess","");
        params.put("skills",skills);
        params.put("image","");
        params.put("change_lang", lang);
        params.put("targets","email,passwd,name,skills,change_lang");

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                JSONObject object = response.getJSONObject("result");
                                String message = object.getString("mess");
                                int userId = object.getInt("user_id");
                                mView.onSuccess(String.valueOf(userId));
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
                mView.onFailed(String.valueOf(error));
            }
        });

        requestQueue.add(jsObjRequest);

    }

    public void fbRegister(final String userId, final String email, final String password, final String name,
                           final String skills, final String lang) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) mContext, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mView.onFbLoginSuccess();
                            saveUsertoFirebase(Integer.parseInt(userId), name, email, skills, lang);
                        }
                    }
                })
                .addOnFailureListener((Activity) mContext, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mView.onFailed(String.valueOf(e));
                        Toast.makeText(mContext, String.valueOf(e), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void login(String email, String password, String token) {
        Map<String,String> params = new HashMap<String, String>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","user_login");
        params.put("d","0");
        params.put("lang",getLanguage());
        params.put("email", email);
        params.put("passwd", password);
        params.put("fcmtoken", token);

        //apiRequest(params);
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String status = null;
                        try {
                            status = String.valueOf(response.getBoolean("success"));
                            JSONObject object = response.getJSONObject("result");
                            int userId = object.getInt("user_id");
                            mView.onSuccess(String.valueOf(userId));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //login failed
                            try {
                                status = response.getString("message");
                                mView.onFailed(status);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mView.onFailed("Login fail");
                error.printStackTrace();
            }
        });

        requestQueue.add(jsObjRequest);

    }

    public void fbLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) mContext, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mView.onFbLoginSuccess();
                        }
                    }
                })
                .addOnFailureListener((Activity) mContext, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        mView.onFailed("Login fail. Your account might not yet been registered");
                    }
                });
    }

    public void resetPassword(String email) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","user_rest_pw");
        params.put("d","0");
        params.put("lang",getLanguage());
        params.put("email", email);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                JSONObject object = response.getJSONObject("result");
                                String message = object.getString("mess");
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
                mView.onFailed(String.valueOf(error));
            }
        });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsObjRequest);
    }

    public void getUserInfo(String happId) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_userinfo");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", happId);

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
                            String photoUrl = object.getString("icon");
                            String email = object.getString("email");
                            String skills = object.getString("skills");

                            new HappPreference(mContext).storeSkillIds(skills);

                            User user = new User(id, name, email, photoUrl);
                            //save updates to firebase
                           // mDatabase.child("users").child(getUfbId()).setValue(user);

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

    private void saveUsertoFirebase(int UwpId, String name, String email, String skills, String lang) {
        if (!skills.equals("")) {
            skills = ","+skills+",";
        }
        //get user id
        String userId = null;
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        User user = new User(UwpId, name, email, skills, lang);
        //save details
        mDatabase.child("users").child(userId).setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
    }

    public void saveUserSkills(String userId) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_userinfo");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", userId);

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
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

    public void saveFirebaseIdToWp(String email, String password) {

        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","user_login");
        params.put("d","0");
        params.put("lang",getLanguage());
        params.put("email",email);
        params.put("passwd",password);
        params.put("firebase", getUfbId());

        //apiRequest(params);
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

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
