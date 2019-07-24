package com.example.android.productcatalog;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ProdCutDebug_tibi";
    public static String CHANNEL_ID = "main_channel";
    public static boolean userisadmin = false;
    public static boolean logged = false;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 1;
    private AlertDialog.Builder builder;
    FirebaseDatabase database;
    DatabaseReference myRef;
    ArrayList<Product> values;
    BroadcastReceiver br;
    ArrayList<String> loginCheck;
    boolean connection;

    // REQ #1 : primary layout activity showing user the list of products available
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loginCheck=SPread();
        if(loginCheck.get(0).equals("logged"))
        {
            logged=true;
        }
        if(loginCheck.get(1).equals("admin"))
        {
            userisadmin=true;
        }
        builder = new AlertDialog.Builder(this);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("products");
        // Read from the database
        // REQ #4 another dynamic listener
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Iterable<DataSnapshot> refs = dataSnapshot.getChildren();
                values = new ProductParser(refs).parseProducts();
                updateListView(values);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read list view value.", error.toException());
            }
        });

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void updateListView(ArrayList<Product> products) {
        final MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(this,products);
        final ListView list = findViewById(R.id.product_list_view);
        list.setAdapter(adapter);
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view,
                                           int position, long id) {
                if (userisadmin) {
                    // REQ #5 dialog COMPLETE
                    final Product item = (Product) parent.getItemAtPosition(position);
                    builder.setMessage("Are you certain you want to remove this item from the database?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    Toast.makeText(getApplicationContext(),"you choose yes action for alertbox",
                                            Toast.LENGTH_SHORT).show();
                                    deleteFireBaseItem(item);
                                    removeImage(item.name);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //  Action for 'NO' Button
                                    dialog.cancel();
                                    Toast.makeText(getApplicationContext(),"you choose no action for alertbox",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                    AlertDialog alert = builder.create();
                    //Setting the title manually
                    alert.setTitle("Delete Item?");
                    alert.show();
                    return false;
                }
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        br = new MyReciver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(br, filter);
        if(!((MyReciver)br).connection)
        {
            connection=true;
        }
        else
            connection=false;
    }

    private void signIn() {
        Intent intent = new Intent(this, Login.class);
        startActivityForResult(intent,1);
    }


    public void subscription() {
        builder.setMessage("would you like to get notifications on new products?")
                .setCancelable(false)
                .setPositiveButton("Subscribe", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        registerTopic();
                    }
                })
                .setNegativeButton("Unsubscribe", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        unregisterTopic();
                    }
                });
        AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle("Subscribe?");
        alert.show();
    }

    public void registerTopic () {
        FirebaseMessaging.getInstance().subscribeToTopic("new_products")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "subscribed to New Products";
                        if (!task.isSuccessful()) {
                            msg = "unable to subscribe to New Products";
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void unregisterTopic () {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("new_products")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "unsubscribed from New Products";
                        if (!task.isSuccessful()) {
                            msg = "unable to unsubscribe to New Products";
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    // REQ #6 menu with at least 2 entries. We have 3 (log-in, add product, exit)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.login:
                if(((MyReciver)br).connection) {
                    if (!loginCheck.get(0).equals("logged") ) {
                        signIn();
                    } else {
                        Toast.makeText(getApplicationContext(), "Already logged in", Toast.LENGTH_LONG).show();
                    }
                }
                else
                    Toast.makeText(getApplicationContext(), "Connect to the internet first!", Toast.LENGTH_LONG).show();
                break;
            case R.id.add_product:
                if(((MyReciver)br).connection) {
                    if (logged) {
                        startActivity(new Intent(this, AddProduct.class));
                    } else {
                        Toast.makeText(getApplicationContext(), "Log In First!", Toast.LENGTH_LONG).show();
                    }
                }
                else
                    Toast.makeText(getApplicationContext(), "Connect to the internet first!", Toast.LENGTH_LONG).show();
                break;
            case R.id.exit:
                System.exit(0);
                break;
            case R.id.subscribe:
                if(((MyReciver)br).connection) {
                    subscription();
                }
                else
                    Toast.makeText(getApplicationContext(), "Connect to the internet first!", Toast.LENGTH_LONG).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            String result = data.getStringExtra("result");
            String result2=data.getStringExtra("state");
            if(result2.equals("logged"))
            {
                if (result.equals("user"))
                    SPwrite("user");
                else
                    SPwrite("admin");
            }
            if (result.equals("user")) {
                Toast.makeText(getApplicationContext(), "User Login successful",
                        Toast.LENGTH_SHORT).show();
                logged = true;
            }
            if (result.equals("admin")) {
                Toast.makeText(getApplicationContext(), "Admin Login successful",
                        Toast.LENGTH_SHORT).show();
                userisadmin = true;
                logged = true;
            }
        }
        if(resultCode == Activity.RESULT_CANCELED)
        {
            Toast.makeText(getApplicationContext(), "Back botton pressed , Login failed!",
                    Toast.LENGTH_SHORT).show();
        }
    }


    // REQ #7 working with adapter info of firebase
    // TODO: move the handling of getting products from firebase to actually work with adapter. currently the query for getting products is done in OnCreate function when it should happen in the adapter as well as deleting items
    public class MySimpleArrayAdapter extends ArrayAdapter<Product> {
        private final Context context;
        private final ArrayList<Product> values;

        public MySimpleArrayAdapter(Context context, ArrayList<Product> values) {
            super(context, -1, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.row_item, parent, false);
            TextView title =  rowView.findViewById(R.id.firstLine);
            TextView description = rowView.findViewById(R.id.secondLine);
            ImageView imageView = rowView.findViewById(R.id.icon);
            description.setText(values.get(position).getDescription());
            loadImage(imageView,values.get(position).getName());
            title.setText(values.get(position).getName());
            Resources resources = context.getResources();
            final int resourceId = resources.getIdentifier(values.get(position).getImage(), "drawable",
                    context.getPackageName());
            TextView price = rowView.findViewById(R.id.price_row);
            price.setText(values.get(position).getPrice() + " $");
            //imageView.setImageDrawable(resources.getDrawable(resourceId));

            // TODO: after DOING the firebase storage upload of images make sure to now donwload these images to show user icons of products

            return rowView;
        }
    }

    public void loadImage(final ImageView imageView,String name)
    {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference islandRef = storageRef.child("images/"+name+".jpg");

        final long ONE_MEGABYTE = 1024 * 1024;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }

    public void SPwrite(String userType)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("login","logged");
        editor.putString("type",userType);
        editor.apply();
    }
    public ArrayList<String> SPread()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = preferences.getString("login", "");
        String type = preferences.getString("type", "");
        ArrayList<String> ret=new ArrayList<>();
        ret.add(name);
        ret.add(type);
        return ret;
    }
    public void deleteFireBaseItem(Product item)
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query applesQuery = ref.child("products").orderByChild("name").equalTo(item.getName());

        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }
    public void removeImage(String name)
    {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

// Create a reference to the file to delete
        StorageReference desertRef = storageRef.child("images/"+name+".jpg");

// Delete the file
        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        clearPref();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearPref();
    }

    private void clearPref() {
        loginCheck=SPread();
        if(loginCheck.size()>0)
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            // Clearing all data from Shared Preferences
            editor.clear();  // where editor is an Editor for Shared Preferences
            editor.putString("login","logged");
            editor.putString("type",loginCheck.get(1));
            editor.apply();
        }


    }
}
