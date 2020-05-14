package com.example.smartbunsai;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

public class HomeActivity extends AppCompatActivity {
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

    }
}
