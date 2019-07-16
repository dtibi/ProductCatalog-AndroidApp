package com.example.android.productcatalog;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

public class UserParser {
    Iterable<DataSnapshot> itData;

    public UserParser(Iterable<DataSnapshot> itData) {
        this.itData = itData;
    }

    public ArrayList<User> parseProducts() {
        ArrayList<User> data = new ArrayList<>();
        for (DataSnapshot ds : itData) {
            User value = ds.getValue(User.class);
            Log.d("parsing product", "Value is: " + value);
            data.add(new User(value.getEmail(),value.getPassword()));
        }
        return data;
    }
}
