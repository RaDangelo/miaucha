package br.com.rafaeldangelobergami.miaucha;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by RafaelDangeloBergami on 12/6/2017.
 */

public class User {

    private static User instance;
    private String user;
    private String password;
    private String name;
    private Bitmap picture;

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    @Exclude
    private String key;

    @Exclude
    public Bitmap getPicture() {
        return picture;
    }

    @Exclude
    public static User getInstance() {
        return instance == null ? instance = new User() : instance;
    }

    @Exclude
    public static User clear() {
        if (instance == null) {
            return instance = new User();
        } else {
            instance.setUser(null);
            instance.setName(null);
            instance.setPassword(null);
            instance.setPicture(null);
            instance.setKey(null);
            return instance;
        }
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("password", password);
        result.put("name", name);

        return result;
    }

    private User() {

    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
