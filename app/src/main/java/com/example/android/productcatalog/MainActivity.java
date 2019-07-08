package com.example.android.productcatalog;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import com.example.android.productcatalog.ui.login.LoginActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static boolean userisadmin = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ProductParser parser = new ProductParser();
        final ListView list = (ListView) findViewById(R.id.product_list_view);

        ArrayList<Product> values = parser.parseProducts(getApplicationContext());

        final MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(this,values);
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
                startActivity(new Intent(this, LoginActivity.class));
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

            imageView.setImageDrawable(resources.getDrawable(resourceId));

            return rowView;
        }
    }
}
