package com.example.android.productcatalog;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ProdCutDebug_tibi";
    public static boolean userisadmin = false;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 1;
    FirebaseDatabase database;
    DatabaseReference myRef;
    ArrayList<Product> values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


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
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });



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
                    final Product item = (Product) parent.getItemAtPosition(position);
                    adapter.remove(item);
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
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
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
        Log.d(TAG, account.getDisplayName());
        if (account.getDisplayName().contentEquals("cky master")) {
            userisadmin = true;
        }
    }


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

            //imageView.setImageDrawable(resources.getDrawable(resourceId));

            return rowView;
        }
    }
}
