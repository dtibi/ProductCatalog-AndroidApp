package com.example.android.productcatalog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Login  extends AppCompatActivity {
    EditText email,pass;
    Button Sign , log;
    FirebaseDatabase database;
    DatabaseReference myRef;
    ArrayList<User> values , admins;
    private static final String TAG = "ProdCutDebug_tibi";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginwindow);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Iterable<DataSnapshot> refs = dataSnapshot.getChildren();
                values = new UserParser(refs).parseProducts();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read list view value.", error.toException());
            }
        });
        myRef = database.getReference("admins");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Iterable<DataSnapshot> refs = dataSnapshot.getChildren();
                admins = new UserParser(refs).parseProducts();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read list view value.", error.toException());
            }
        });
        email=(EditText)findViewById(R.id.editText);
        pass=(EditText)findViewById(R.id.editText1);
        Sign=(Button)findViewById(R.id.button);
        Sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(email.getText().toString().isEmpty() || pass.getText().toString().isEmpty())
               {
                   Toast.makeText(getApplicationContext(),"fill the missing information",
                           Toast.LENGTH_SHORT).show();
               }
               else
               {
                   int found=0;
                   for(int i=0;i<values.size();i++)
                   {
                       if(email.getText().toString().equals(values.get(i).getEmail()) &&
                               pass.getText().toString().equals(values.get(i).getPassword()))
                       {
                           Toast.makeText(getApplicationContext(),"this account exists , press Login",
                                   Toast.LENGTH_SHORT).show();
                           found=1;
                       }
                   }
                   for(int i=0;i<admins.size();i++)
                   {
                       if(email.getText().toString().equals(admins.get(i).getEmail()) &&
                               pass.getText().toString().equals(admins.get(i).getPassword()))
                       {
                           Toast.makeText(getApplicationContext(),"this account exists , press Login",
                                   Toast.LENGTH_SHORT).show();
                           found=1;
                       }
                   }
                   if(found==0)
                   {
                       FirebaseDatabase database = FirebaseDatabase.getInstance();
                       DatabaseReference myRef = database.getReference("users/" +"user "+(values.size()+1));
//        database.getReference().getDatabase();
                       myRef.setValue(new User(email.getText().toString(),pass.getText().toString()));
                       Intent returnIntent = new Intent();
                       returnIntent.putExtra("result",1);
                       setResult(Activity.RESULT_OK,returnIntent);
                       finish();
                   }

               }
            }
        });
        log=(Button)findViewById(R.id.button2);
        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(email.getText().toString().isEmpty() || pass.getText().toString().isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"fill the missing information",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    int found=0;
                    for(int i=0;i<values.size();i++)
                    {
                        if(email.getText().toString().equals(values.get(i).getEmail()) &&
                                pass.getText().toString().equals(values.get(i).getPassword()))
                        {
                            found=1;
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("result","user");
                            setResult(Activity.RESULT_OK,returnIntent);
                            finish();
                        }
                    }
                    for(int i=0;i<admins.size();i++)
                    {
                        if(email.getText().toString().equals(admins.get(i).getEmail()) &&
                                pass.getText().toString().equals(admins.get(i).getPassword()))
                        {
                            found=1;
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("result","admin");
                            setResult(Activity.RESULT_OK,returnIntent);
                            finish();
                        }
                    }
                    if(found==0)
                    {
                        Toast.makeText(getApplicationContext(),"Sign in first",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
