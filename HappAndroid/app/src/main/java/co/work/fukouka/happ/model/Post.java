package co.work.fukouka.happ.model;

import java.util.List;


public class Post {
    int postId;
    String author;
    String authorProfile;
    String dateModified;
    String skills;
    String body;
    String fromUserId;
    String imageUrl;
    List<String> images;

    public Post() {
    }

    public Post(int postId, String dateModified, String skills, String body, String fromUserId, List<String> images) {
        this.postId = postId;
        this.dateModified = dateModified;
        this.skills = skills;
        this.body = body;
        this.fromUserId = fromUserId;
        this.images = images;
    }


    public Post(int postId, String author, String authorProfile, String dateModified, String skills,
                String body, String fromUserId, List<String> images) {

        this.postId = postId;
        this.author = author;
        this.authorProfile = authorProfile;
        this.dateModified = dateModified;
        this.skills = skills;
        this.body = body;
        this.fromUserId = fromUserId;
        this.images = images;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorProfile() {
        return authorProfile;
    }

    public void setAuthorProfile(String authorProfile) {
        this.authorProfile = authorProfile;
    }

    public String getDateModified() {
        return dateModified;
    }

    public void setDateModified(String dateModified) {
        this.dateModified = dateModified;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
