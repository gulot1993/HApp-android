package co.work.fukouka.happ.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import co.work.fukouka.happ.R;
import co.work.fukouka.happ.activity.LoginActivity;
import co.work.fukouka.happ.activity.MainActivity;

public class SessionManager {
    SharedPreferences pref;
    Editor editor;
    Context context;
    // Shared pref mode
    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "HappPreference";
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_USER_ID = "userID";
    public static final String EMAIL = "email";
    public static final String LANGUAGE = "language";
    public static final String NAME = "name";
    public static final String PHOTO_URL = "photoUrl";
    public static final String STATEMENT = "statement";
    public static final String SKILLS = "skills";
    public static final String HAPPID = "happ_id";
    public static final String FREE_TIME_STATUS = "status";
    public static final String PASSWORD = "password";
    public static final String FIRST_TIME_LOAD = "firstTimeLoad";

    public SessionManager(Context context){
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String userid) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_USER_ID, userid);
        editor.commit();
    }

    public void saveSelectedLanguage(String language) {
        editor.putString(LANGUAGE, language);
        editor.commit();
    }

    public void saveUserEmail(String email) {
        editor.putString(EMAIL, email);
        editor.commit();
    }

    public void savePassword(String password) {
        editor.putString(PASSWORD, password);
        editor.commit();

    }

    public void saveUserInfo(String name, String photoUrl, String statement, String skills, String happId) {
        editor.putString(NAME, name);
        editor.putString(PHOTO_URL, photoUrl);
        editor.putString(STATEMENT, statement);
        editor.putString(SKILLS, skills);
        editor.putString(HAPPID, happId);
        editor.commit();
    }

    public void saveUserInfo(String name, String photoUrl, String statement, String skills) {
        editor.putString(NAME, name);
        editor.putString(PHOTO_URL, photoUrl);
        editor.putString(STATEMENT, statement);
        editor.putString(SKILLS, skills);
        editor.commit();
    }

    public void freeStatusOn(String status) {
        editor.putString(FREE_TIME_STATUS, status);
        editor.commit();
    }

    public void flagFirstTimeLoad() {
        editor.putBoolean(FIRST_TIME_LOAD, false);
        editor.commit();
    }

    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserId() {
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_USER_ID, pref.getString(KEY_USER_ID, null));

        return user;
    }

    /**
     * Get stored user email
     * */
    public HashMap<String, String> getUserEmail() {
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(EMAIL, pref.getString(EMAIL, null));

        return user;
    }

    public HashMap<String, String> getUserPassword() {
        HashMap<String, String> password = new HashMap<String, String>();
        password.put(PASSWORD, pref.getString(PASSWORD, null));

        return password;
    }

    /**
     *  Get stored user language
    * */
    public HashMap<String, String> getLanguage() {
        HashMap<String, String> language = new HashMap<String, String>();
        language.put(LANGUAGE, pref.getString(LANGUAGE, null));

        return language;
    }

    public HashMap<String, String> getUserInfo() {
        HashMap<String, String> info = new HashMap<String, String>();
        info.put(NAME, pref.getString(NAME, null));
        info.put(PHOTO_URL, pref.getString(PHOTO_URL, null));
        info.put(STATEMENT, pref.getString(STATEMENT, null));
        info.put(SKILLS, pref.getString(SKILLS, null));
        info.put(HAPPID, pref.getString(HAPPID, null));

        return info;
    }

    public HashMap<String, String> getStatus() {
        HashMap<String, String> status = new HashMap<String, String>();
        status.put(FREE_TIME_STATUS, pref.getString(FREE_TIME_STATUS, null));

        return status;
    }


    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else redirect to dashboard page
     * */
    public void checkLogin() {
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent intent = new Intent(context, LoginActivity.class);
            // Closing all the Activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // Add new Flag to start new Activity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

    }

    /**
     * Clear session details
     * */
    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.remove(KEY_USER_ID);
        editor.remove(PASSWORD);
        editor.remove(IS_LOGIN);
        editor.remove(EMAIL);
        editor.remove(NAME);
        editor.remove(PHOTO_URL);
        editor.remove(STATEMENT);
        editor.remove(SKILLS);
        editor.remove(HAPPID);

        new HappPreference(context).removeSkillIds();

        boolean success = editor.commit();
        if (success) {
            removeUserToken();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
            ((Activity) context).finish();
        } else {
            Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    public boolean isFirstTimeLoad() {
        return pref.getBoolean(FIRST_TIME_LOAD, true);
    }

    private void removeUserToken() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        String userId = getUfbId();

        if (userId != null)
            db.child("registration-token").child(userId).child("token").setValue(null);
    }

    private String getUfbId() {
        String userId = null;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }
        return userId;
    }
}
