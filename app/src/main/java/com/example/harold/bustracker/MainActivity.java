package com.example.harold.bustracker;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.harold.bustracker.AccountActivity.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String[] routeNames = {"Link 426: POINCIANA/CIRCULATOR CIRCULATOR", "Link 08: W. OAK RIDGE RD/INTL. DR INBOUND",
                                    "Link 10: E. U.S. 192/ST. CLOUD EASTBOUND", "Link 07: S. ORANGE AVE./FLORIDA MALL INBOUND",
                                    "Link 50: DOWNTOWN ORLANDO/MAGIC KINGDOM INBOUND"};
    private String[] colors = {"B1BCE8", "89CD66", "63C9B3", "557393", "A87000"};

    private int[] routeNumbers = {567, 424, 434, 423, 596};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Choose A Bus Route");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);

        FloatingActionButton signOut = (FloatingActionButton) findViewById(R.id.fab_signout);

        populateListView();

        mAuth = FirebaseAuth.getInstance();


        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void populateListView() {


        CustomAdapter customAdapter = new CustomAdapter();

        ListView list = (ListView) findViewById(R.id.listview_route);
        list.setAdapter(customAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(MainActivity.this, User.class);
                i.putExtra("BusSTop", position);
                i.putExtra("RouteNumber", routeNumbers[position]);
                i.putExtra("RouteName", routeNames[position]);
                startActivity(i);
                overridePendingTransition(R.anim.righttoleft,R.anim.stable);
            }
        }
        );
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return routeNames.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            view = getLayoutInflater().inflate(R.layout.route_item, null);

            ImageView imageView = view.findViewById(R.id.imageView_route);
            imageView.setColorFilter(Integer.decode("0x7f" + colors[i]));

            TextView route = (TextView) view.findViewById(R.id.textview_route);
            route.setTextColor(Integer.decode("0x7f" + colors[i]));
            route.setText(routeNames[i]);


            return view;
        }
    }
}
