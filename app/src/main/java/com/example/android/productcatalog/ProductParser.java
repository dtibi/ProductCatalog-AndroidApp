package com.example.android.productcatalog;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

class ProductParser {
    Iterable<DataSnapshot> itData;

    public ProductParser(Iterable<DataSnapshot> itData) {
        this.itData = itData;
    }

    public ArrayList<Product> parseProducts() {
        ArrayList<Product> data = new ArrayList<>();
        for (DataSnapshot ds : itData) {
            Product value = ds.getValue(Product.class);
            Log.d("parsing product", "Value is: " + value);
            data.add(new Product(value.getName(),value.getDescription(),value.getPrice(),value.getImage()));
        }
        return data;
    }
}
