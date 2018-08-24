package com.example.abinashbhattarai.roomiesphotoblog;
import java.util.Date;


public class BlogPost extends BlogPostId {
    private String user_id;
    private String image_url;
    private String description;
    private Date timestamp;



    public BlogPost() {

    }

    public BlogPost(String user_id, String image_url, String description, String thumb_url, Date timestamp) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.description = description;
        this.thumb_url = thumb_url;
        this.timestamp=timestamp;
    }

    private String thumb_url;


    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }




}