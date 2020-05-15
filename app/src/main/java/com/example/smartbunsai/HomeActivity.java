package com.example.smartbunsai;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    String TAG = "TAG";
    //Firebase fireStore
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    Query light_qry = database.getReference("sensor/light/values").orderByKey().limitToLast(1);
    Query temperature_qry = database.getReference("sensor/temperature/values").orderByKey().limitToLast(1);
    Query moisture_qry = database.getReference("sensor/moisture/values").orderByKey().limitToLast(1);

    String light_string, temperature_string, moisture_string;
    TextView light_Text, temperature_Text, moisture_Text;
    ImageView img_watering, img_light, img_temperature, img_moisture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //set icon image
        img_watering = findViewById(R.id.img_watering);
        img_watering.setImageResource(R.drawable.wateringtool);

        img_light = findViewById(R.id.img_light);
        img_light.setImageResource(R.drawable.illumination);

        img_temperature = findViewById(R.id.img_temp);
        img_temperature.setImageResource(R.drawable.tempurature);

        img_moisture = findViewById(R.id.img_moisture);
        img_moisture.setImageResource(R.drawable.raindrops);

        light_Text = findViewById(R.id.light_Text);
        temperature_Text = findViewById(R.id.temperature_Text);
        moisture_Text = findViewById(R.id.moisture_Text);
        fetchData();

    }

    private void fetchData(){
        // Read from the database
        light_qry.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                String subString = map.values().toString();
                int length = subString.length();
                light_string = subString.substring(1,length-1);
                light_Text.setText("Light: " + light_string);
                Log.d(TAG, "Light Value is: " + map.values().toString());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        temperature_qry.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                String subString = map.values().toString();
                int length = subString.length();
                temperature_string = String.format("%.2f", Float.valueOf(subString.substring(1,length-1)));
                temperature_Text.setText("Temp: " + temperature_string);
                Log.d(TAG, "temperature Value is: " + map.values().toString());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        moisture_qry.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                String subString = map.values().toString();

                int length = subString.length();
                moisture_string = subString.substring(1,length-1);

                moisture_Text.setText("Moisture: " + moisture_string);
                Log.d(TAG, "moisture Value is: " + map.values().toString());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
}
