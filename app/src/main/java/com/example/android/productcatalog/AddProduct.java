package com.example.android.productcatalog;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.provider.MediaStore;

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
    Bitmap imageBitmap=null;
    FirebaseStorage storage;
    StorageReference storageReference;
    boolean photo_sign=false;
    // REQ #2 Add product layout activity allowing admin user to add products to database
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
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
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, 0);
    }
    @Override
    public void onClick(View view) {
        if(!inputIsLegal()) {
            Toast.makeText(getApplicationContext(),"Fill All Fields To Add New Product", Toast.LENGTH_LONG).show();
            return;
        }
        if(!photo_sign)
        {
            Toast.makeText(getApplicationContext(),"take a picture of the Product", Toast.LENGTH_LONG).show();
            return;
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("products/" + name.getText().hashCode());
//        database.getReference().getDatabase();
        myRef.setValue(new Product(name.getText().toString(),desc.getText().toString(),Float.parseFloat(price.getText().toString()),img.getText().toString()));
        uploadImage(imageBitmap,name.getText().toString());
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
            if (requestCode == 0 && resultCode == RESULT_OK && data != null)
            {
                imageBitmap = (Bitmap) data.getExtras().get("data");
                imgClick.setImageBitmap(imageBitmap);
                photo_sign=true;
            }
    }
    //uploading to storage methood
    private void uploadImage(Bitmap bitmap,String name) {
        final StorageReference ref = storageReference.child("images/" + name + ".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] data = baos.toByteArray();
        final UploadTask uploadTask = ref.putBytes(data);
    }

}
