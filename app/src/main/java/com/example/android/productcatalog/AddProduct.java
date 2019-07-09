package com.example.android.productcatalog;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddProduct extends AppCompatActivity implements View.OnClickListener {

    EditText name;
    EditText desc;
    EditText price;
    EditText img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        name = findViewById(R.id.name);
        desc = findViewById(R.id.description);
        price = findViewById(R.id.price);
        img = findViewById(R.id.name);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // TODO: add product to database
        if(!inputIsLegal()) {
            Toast.makeText(getApplicationContext(),"Fill All Fields To Add New Product", Toast.LENGTH_LONG).show();
            return;
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("products/" + name.getText().hashCode());
//        database.getReference().getDatabase();
        myRef.setValue(new Product(name.getText().toString(),desc.getText().toString(),Float.parseFloat(price.getText().toString()),img.getText().toString()));
        startActivity(new Intent(this, MainActivity.class));
    }

    private boolean inputIsLegal() {
        if (name.getText().toString().isEmpty() ||
                desc.getText().toString().isEmpty() ||
                price.getText().toString().isEmpty() ||
                img.getText().toString().isEmpty()) {
            return false;
        }
        return true;
    }
}
