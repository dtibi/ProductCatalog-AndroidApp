package com.example.android.productcatalog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class AddProduct extends AppCompatActivity implements View.OnClickListener {

    EditText name;
    EditText desc;
    EditText price;
    EditText img;
    ImageView imgClick;
    Bitmap photo=null;
    // REQ #2 Add product layout activity allowing admin user to add products to database
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        name = findViewById(R.id.name);
        desc = findViewById(R.id.description);
        price = findViewById(R.id.price);
        // img will be used to save image data for later retrial from storage
        img = name;
        imgClick = findViewById(R.id.camera);
        FloatingActionButton fab = findViewById(R.id.fab);
        // REQ #4 use dynamic listener (part 1)
        fab.setOnClickListener(this);
    }
    // REQ #4 use static listener (part 1)
    public void getCaptureImage(View view) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent, 0);
    }

    @Override
    public void onClick(View view) {
        if(!inputIsLegal()) {
            Toast.makeText(getApplicationContext(),"Fill All Fields To Add New Product", Toast.LENGTH_LONG).show();
            return;
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("products/" + name.getText().hashCode());
//        database.getReference().getDatabase();
        myRef.setValue(new Product(name.getText().toString(),desc.getText().toString(),Float.parseFloat(price.getText().toString()),img.getText().toString()));

        finish();
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 0) {

            photo = (Bitmap) data.getExtras().get("data");
            imgClick.setImageBitmap(photo);

            // TODO: upload image to firebase storage for users to be able to download them and see them as icons
        }
    }

}
