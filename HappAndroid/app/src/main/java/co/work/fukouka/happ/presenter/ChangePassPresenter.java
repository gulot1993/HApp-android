package co.work.fukouka.happ.presenter;

import android.content.Context;
import android.support.annotation.NonNull;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import co.work.fukouka.happ.R;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.utils.JsonObjectRequest;
import co.work.fukouka.happ.utils.SessionManager;
import co.work.fukouka.happ.view.ChangePassView;


public class ChangePassPresenter {

    private ChangePassView mView;
    private Context mContext;
    private SessionManager mSession;
    private boolean credential;
    private HappHelper helper;
    private GetSystemValue system;

    public ChangePassPresenter(ChangePassView view, Context context) {
        this.mView = view;
        this.mContext = context;
        mSession = new SessionManager(context);
        helper = new HappHelper(context);
        system = new GetSystemValue(context);
    }

    public String getUserId() {
        String userId;

        HashMap<String, String> user = mSession.getUserId();
        userId = user.get(SessionManager.KEY_USER_ID);

        return userId;
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

    public boolean checkCredential(String currentPass) {
        Map<String,String> params = new HashMap<String, String>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","user_login");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("email", getUserEmail());
        params.put("passwd", currentPass);

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                mView.isAuthenticated();
                                credential = true;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            boolean error = response.getBoolean("error");
                            if (error) {
                                helper.throwToastMessage(system.getValue("mess_incorrect_password"),
                                        mContext.getString(R.string.password_incorrect));
                                credential = false;
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

        return credential;
    }

    public void changePassword(String newPass) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","user_update");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("user_id", getUserId());
        params.put("passwd", newPass);
        params.put("targets","passwd");

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                mView.onUpdateSuccess();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            mView.onUpdateFailed();
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

    public void changeFbPassword(String currentPassword, final String newPassword) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(getUserEmail(), currentPassword);
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                mView.onFbUpdateSuccess();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mView.onFbUpdateFailed(e.getMessage());
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mView.onFbUpdateFailed(e.getMessage());
            }
        });
    }
}
