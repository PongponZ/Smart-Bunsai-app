package com.example.smartbunsai;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.Timer;

public class HomeActivity extends AppCompatActivity {
    String TAG = "TAG";
    //Firebase fireStore
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference  water_switch_auto = database.getReference("switch/water_auto/status");
    DatabaseReference  water_switch = database.getReference("switch/water_switch/status");
    Query light_qry = database.getReference("sensor/light/values").orderByKey().limitToLast(1);
    Query temperature_qry = database.getReference("sensor/temperature/values").orderByKey().limitToLast(1);
    Query moisture_qry = database.getReference("sensor/moisture/values").orderByKey().limitToLast(1);

    String light_string, temperature_string, moisture_string;
    TextView light_Text, temperature_Text, moisture_Text, status_Text;
    ImageView img_watering, img_light, img_temperature, img_moisture;
    Switch autoSwitch, autoSwitch2;
    LinearLayout water_layout;

    //value
    boolean auto_switch = false, isOpen = false;
    int moisture_value = 0;
    int light_value = 0;
    double temp_value = 0;
    String WATER_AUTO_STATUS = "WATER_AUTO_STATUS";
    SharedPreferences sp;
    SharedPreferences.Editor editor;

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

        water_layout = findViewById(R.id.linear_layout_watering);
        status_Text = findViewById(R.id.myStatus);

        autoSwitch2 = findViewById(R.id.auto_switch);
        //PREF
        sp = getSharedPreferences(WATER_AUTO_STATUS, Context.MODE_PRIVATE);
        editor = sp.edit();

        if(!sp.contains("initialized")){
            //Indicate that the default shared prefs have been set
            editor.putBoolean("initialized", true);
            //Set some default shared pref
            editor.putBoolean("WATER_AUTO", false);
            editor.commit();
        }
        if(sp.contains("WATER_AUTO")){
            autoSwitch2.setChecked(sp.getBoolean("WATER_AUTO", false));
            auto_switch = sp.getBoolean("WATER_AUTO", false);
            if(auto_switch) water_layout.setBackgroundResource(R.drawable.btn_disable);


        }

        fetchData();

        //Watering Button
        img_watering.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!auto_switch){
                    if(!isOpen){
                        water_switch.setValue(true);
                        water_layout.setBackgroundResource(R.drawable.btn_on);
                        isOpen = true;
                    }else{
                        water_layout.setBackgroundResource(R.drawable.btn_shape_xlong);
                        water_switch.setValue(false);
                        isOpen = false;

                    }

                }else{
                    Toast.makeText(getApplicationContext(), "WATERING IS AUTO",Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    public void autoSwitch(View view){
        autoSwitch = findViewById(view.getId());
        auto_switch = autoSwitch.isChecked();

        //set value to realtime database
        water_switch_auto.setValue(auto_switch);
        if(auto_switch){
            water_layout.setBackgroundResource(R.drawable.btn_disable);
        }else{
            water_layout.setBackgroundResource(R.drawable.btn_shape_xlong);
        }

        editor.putBoolean("WATER_AUTO", auto_switch);
        editor.commit();
    }

    //Temp 15 – 40 องศาเซลเซียส
    //Moisture ต้องการน้ำ < 1500 | > 2200 ไม่ต้องการน้ำ | > 3000 น้ำเยอะเกินไป

    private void calculateStatus(){
        String status, temp_string, moisture_string, light_string;

        //check moisture
        if(moisture_value < 1500) moisture_string = "เราต้องการน้ำ";
        else if(moisture_value > 3000) moisture_string = "น้ำท่วมแล้วจ้าา";
        else moisture_string = "ปริมาณน้ำเพียงพอแล้ว";

        //check Temp
        if(temp_value < 15) temp_string = "อากาศเย็นมากเลยแม่";
        else if(temp_value > 40) temp_string = "อากาศร้อนมากเราจะตายแล้ว";
        else temp_string = "อากาศพอดีเลย เย้";

        //check light
        //if(light_value > 4000) light_string="แสงเยอะมาก";

        status = moisture_string + " " + temp_string;
        status_Text.setText(status);
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
                light_value = Integer.valueOf(light_string);
                calculateStatus();
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
                temp_value = Double.valueOf(temperature_string);
                calculateStatus();
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
                moisture_value = Integer.valueOf(moisture_string);
                calculateStatus();
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
