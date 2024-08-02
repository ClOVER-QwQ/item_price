package com.example.item_price;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.item_price.R.*;
import static com.example.item_price.R.id.*;

public class AboutFrame extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.about);

        Button back = findViewById(id.back);
        back.setOnClickListener(view -> {
            Intent add = new Intent();
            add.setClass(AboutFrame.this,MainActivity.class);
            startActivity(add);
        });
    }
}
