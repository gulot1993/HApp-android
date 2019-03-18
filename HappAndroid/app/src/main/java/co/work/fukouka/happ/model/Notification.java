package co.work.fukouka.happ.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

@IgnoreExtraProperties
public class Notification {
    int id;
    String userId;
    String name;
    String photoUrl;
    String type;
    String skills;
    String firIDs;
    String messageEN;
    String messageJP;
    Map<String, String> timestamp;

    public Notification() {
    }

    public Notification(int id, String userId, String name, String photoUrl, String type,
                        Map<String, String> timestamp) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.photoUrl = photoUrl;
        this.type = type;
        this.timestamp = timestamp;
    }

    public Notification(int id, String userId, String name, String photoUrl, String type,
                        String messageEN, String messageJP, Map<String, String> timestamp) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.photoUrl = photoUrl;
        this.type = type;
        this.messageEN = messageEN;
        this.messageJP = messageJP;
        this.timestamp = timestamp;
    }

    public Notification(int id, String firIDs, String userId, String name, String photoUrl, String type,
                        String messageEN, String messageJP, Map<String, String> timestamp) {
        this.id = id;
        this.firIDs = firIDs;
        this.userId = userId;
        this.name = name;
        this.photoUrl = photoUrl;
        this.type = type;
        this.messageEN = messageEN;
        this.messageJP = messageJP;
        this.timestamp = timestamp;
    }

    public Notification(int id, String userId, String name, String photoUrl, String type, String skills,
                        Map<String, String> timestamp) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.photoUrl = photoUrl;
        this.type = type;
        this.timestamp = timestamp;
        this.skills = skills;
    }

    public Notification(int id, String userId, String name, String photoUrl, String type,
                        String skills, String firIDs, String messageEN, String messageJP,
                        Map<String, String> timestamp) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.photoUrl = photoUrl;
        this.type = type;
        this.skills = skills;
        this.firIDs = firIDs;
        this.messageEN = messageEN;
        this.messageJP = messageJP;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Map<String, String> timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getFirIDs() {
        return firIDs;
    }

    public void setFirIDs(String firIDs) {
        this.firIDs = firIDs;
    }

    public String getMessageEN() {
        return messageEN;
    }

    public void setMessageEN(String messageEN) {
        this.messageEN = messageEN;
    }

    public String getMessageJP() {
        return messageJP;
    }

    public void setMessageJP(String messageJP) {
        this.messageJP = messageJP;
    }
}
