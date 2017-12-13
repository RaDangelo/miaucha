package br.com.rafaeldangelobergami.miaucha;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by RafaelDangeloBergami on 12/7/2017.
 */

public class Cat {

    private String name;
    private User user;
    private String description;
    private String phone;
    private List<Bitmap> pictures;
    private CatLocation location;
    private String address;

//    public Cat(Cat c) {
//        this.setLocation(c.getLocation());
//        this.setUser(c.getUser());
//        this.setName(c.getName());
//        this.setDescription(c.getDescription());
//        this.setPhone(c.getPhone());
//        this.setAddress(c.getAddress());
//        this.setPictures(new ArrayList<Bitmap>());
//    }

    public Cat() {
        this.setPictures(new ArrayList<Bitmap>());
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("description", description);
        result.put("location", location);
        result.put("address", address);
        result.put("phone", phone);
        result.put("name", name);

        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Exclude
    public List<Bitmap> getPictures() {
        return pictures;
    }

    public void setPictures(List<Bitmap> pictures) {
        this.pictures = pictures;
    }

    public CatLocation getLocation() {
        return location;
    }

    public void setLocation(CatLocation location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

