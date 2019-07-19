package com.example.android.productcatalog;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


// TODO: add broadcast receiver to check internet connectivity (REQ #8)

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ProdCutDebug_tibi";
    public static boolean userisadmin = true;
    public static String CHANNEL_ID = "main_channel";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 1;
    private AlertDialog.Builder builder;
    FirebaseDatabase database;
    DatabaseReference myRef;
    ArrayList<Product> values;


    // REQ #1 : primary layout activity showing user the list of products available
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        builder = new AlertDialog.Builder(this);
        mAuth = FirebaseAuth.getInstance();
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

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
                                    adapter.remove(item);
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
        signIn();
    }

    private void signIn() {
        // REQ #3 sign-in screen made by google COMPLETE
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
                signIn();
                break;
            case R.id.add_product:
                if (userisadmin) {
                    startActivity(new Intent(this, AddProduct.class));
                } else {
                    Toast.makeText(getApplicationContext(),"Log In First!",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.exit:
                System.exit(0);
                break;
            case R.id.subscribe:
                subscription();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        // TODO: check if admin connected and allow deletion of products
        if (account==null) {
            // Login Failed
            Toast.makeText(getApplicationContext(),"Login Failed",Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, account.getDisplayName());
            if (account.getDisplayName().contentEquals("cky master")) {
                userisadmin = true;
            }
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
}
