package co.work.fukouka.happ.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    int id;
    String happId;
    String name;
    String photoUrl;
    String email;
    String message;
    String skills;
    String language;

    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public User(int id, String name, String photoUrl) {
        this.id = id;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public User(String name, String email, String skills) {
        this.name = name;
        this.email = email;
        this.skills = skills;
    }

    public User(String name, String email, String skills, String language) {
        this.name = name;
        this.email = email;
        this.skills = skills;
        this.language = language;
    }

    public User(int id, String name, String email, String skills, String language) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.skills = skills;
        this.language = language;
    }

    public User(int id, String name, String email, String photoUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    public User(int id, String name, String email, String photoUrl, String skills, String language) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
        this.skills = skills;
        this.language = language;
    }

    public User(int id, String happId, String name, String photoUrl, String email, String message, String skills) {
        this.id = id;
        this.happId = happId;
        this.name = name;
        this.photoUrl = photoUrl;
        this.email = email;
        this.message = message;
        this.skills = skills;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHappId() {
        return happId;
    }

    public void setHappId(String happId) {
        this.happId = happId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}
