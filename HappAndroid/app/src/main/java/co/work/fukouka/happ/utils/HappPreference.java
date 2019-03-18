package co.work.fukouka.happ.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import co.work.fukouka.happ.model.Skill;

public class HappPreference {
    private static final String PREF_NAME = "HappPref";

    private SharedPreferences mPref;
    private Editor mEditor;
    private Context mContext;

    public HappPreference(Context context) {
        this.mContext = context;
        mPref = context.getSharedPreferences(PREF_NAME, 0);
        mEditor = mPref.edit();
    }

    public void storeToken(String token) {
        mEditor.putString("token", token);
        mEditor.commit();
    }

    public void storeFirstPostId(int id) {
        mEditor.putInt("post_id", id);
        mEditor.commit();
    }

    public void storeTimelineNotifCount(int notifCount) {
        mEditor.putInt("notif_count", notifCount);
        mEditor.commit();
    }

    public void storeSkills(Skill skills) {
        String category = skills.getCategory();
        Set<String> skillId = null;
        Set<String> skillName = null;

        for (int i =  0; i < skills.getSkills().size(); i++) {
            String name = skills.getSkills().get(i).getName();
            int id = skills.getSkills().get(i).getSkillId();
            skillName.add(name);
            skillId.add(String.valueOf(id));
        }

        mEditor.putString("category", category);
        mEditor.putStringSet("skill_id", skillId);
        mEditor.putStringSet("skill_name", skillName);
    }

    public Skill getSkills() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("category", mPref.getString("category", null));
        hashMap.put("skill_id", mPref.getStringSet("skill_id", null));
        hashMap.put("skill_name", mPref.getStringSet("skill_name", null));

        String category = (String) hashMap.get("category");
        String skillId = (String) hashMap.get("skill_id");
        String skillName = (String) hashMap.get("skill_name");

        Skill skill = new Skill(1, skillName);
        List<Skill> skillList = new ArrayList<>();
        skillList.add(skill);

        Skill skillObj = new Skill(category, skillList);

        return skillObj;
    }

    public void storeSkillIds(String id) {
        mEditor.putString("skill_id", id);
        mEditor.commit();
    }

    public void removeSkillIds() {
        mEditor.remove("skill_id");
        mEditor.commit();
    }

    public String getSkillIds() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("skill_id", mPref.getString("skill_id", null));

        return hashMap.get("skill_id");
    }

    public void enableAutoStart() {
        mEditor.putBoolean("autoStart", true);
        mEditor.commit();
    }

    public String getToken() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("token", mPref.getString("token", null));

        return hashMap.get("token");
    }

    public int getFirstPostId() {
        HashMap<String, Integer> hashMap = new HashMap<>();
        hashMap.put("post_id", mPref.getInt("post_id", 0));

        return hashMap.get("post_id");
    }

    public int getTimelineNotifCount() {
        HashMap<String, Integer> hashMap = new HashMap<>();
        hashMap.put("notif_count", mPref.getInt("notif_count", 0));

        return hashMap.get("notif_count");
    }

    public boolean isAutoStartEnabled() {
        HashMap<String, Boolean> hashMap = new HashMap<>();
        hashMap.put("autoStart", mPref.getBoolean("autoStart", false));

        return hashMap.get("autoStart");
    }

    public void removeToken() {
        mEditor.remove("token");
        mEditor.commit();
    }

    public void removeNotifCount() {
        mEditor.remove("notif_count");
        mEditor.commit();
    }

}
