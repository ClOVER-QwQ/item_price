package com.example.item_price;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CalculateFrame extends AppCompatActivity {

    private int groupId;
    private TextView totalPriceTextView;
    private List<Item> items;  // 用于存储读取到的物品列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculate_frame);

        Log.d("CalculateFrame", "onCreate started");

        Intent intent = getIntent();
        if (intent!= null) {
            groupId = intent.getIntExtra("groupId", -1);
            Log.d("CalculateFrame", "Received groupId: " + groupId);
        }

        totalPriceTextView = findViewById(R.id.total_price_text_view);
        Button calculateButton = findViewById(R.id.calculate_button);
        Button backButton = findViewById(R.id.back_button);

        // 加载物品数据
        loadItemsData();

        calculateButton.setOnClickListener(v -> calculateTotalPrice());

        backButton.setOnClickListener(v -> finish());
    }

    private void loadItemsData() {
        Log.d("CalculateFrame", "loadItemsData started");
        items = new ArrayList<>();
        File file = new File(getExternalFilesDir(null), "items_" + groupId + ".txt");
        Log.d("CalculateFrame", "Checking file: " + file.getAbsolutePath());
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine())!= null) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {  // 因为现在有 4 个字段
                        int id = Integer.parseInt(parts[0]);
                        String name = parts[1];
                        String price = parts[2];

                        Item item = new Item();
                        item.setId(id);
                        item.setName(name);
                        item.setPrice(price);
                        items.add(item);
                        Log.d("CalculateFrame", "Read item: " + item.getName());  // 新增：打印读取到的物品名称
                    }
                }
            } catch (IOException e) {
                Log.e("CalculateFrame", "读取文件时发生错误", e);
            }
        } else {
            Toast.makeText(this, "未找到对应的物品文件", Toast.LENGTH_SHORT).show();
        }

        // 显示物品名称、价格和数量输入框
        displayItems();
    }

    private void displayItems() {
        LinearLayout itemLayout = findViewById(R.id.item_info_layout);
        Log.d("CalculateFrame", "Found item layout: " + (itemLayout!= null));  // 新增：打印是否成功获取到布局

        for (Item item : items) {
            @SuppressLint("InflateParams") View itemView = getLayoutInflater().inflate(R.layout.item_row, null);  // 假设创建了一个单独的物品行布局

            TextView nameTextView = itemView.findViewById(R.id.item_name_text_view);
            TextView priceTextView = itemView.findViewById(R.id.item_price_text_view);
            EditText quantityEditText = itemView.findViewById(R.id.item_quantity_edit_text);

            nameTextView.setText(item.getName());
            Log.d("CalculateFrame", "Set name: " + item.getName());  // 新增：打印设置的物品名称

            priceTextView.setText(item.getPrice());
            Log.d("CalculateFrame", "Set price: " + item.getPrice());  // 新增：打印设置的物品价格

            quantityEditText.setText(String.valueOf(item.getNum()));  // 设置初始数量值
            Log.d("CalculateFrame", "Set quantity: " + item.getNum());  // 新增：打印设置的物品数量

            if (itemLayout != null) {
                itemLayout.addView(itemView);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void calculateTotalPrice() {
        double totalPrice = 0;
        for (Item item : items) {
            LinearLayout itemLayout = findViewById(R.id.item_info_layout);

            for (int i = 0; i < itemLayout.getChildCount(); i++) {
                View childView = itemLayout.getChildAt(i);

                TextView nameTextView = childView.findViewById(R.id.item_name_text_view);
                TextView priceTextView = childView.findViewById(R.id.item_price_text_view);
                EditText quantityEditText = childView.findViewById(R.id.item_quantity_edit_text);

                if (item.getName().equals(nameTextView.getText().toString())) {  // 找到对应的物品行
                    String numText = quantityEditText.getText().toString();
                    int quantity = 0;  // 设置默认值
                    try {
                        quantity = Integer.parseInt(numText);
                        if (quantity <= 0) {
                            Toast.makeText(this, "数量必须为正整数", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "输入的数量格式不正确，已使用默认值 0", Toast.LENGTH_SHORT).show();
                    }

                    double itemPrice = Double.parseDouble(priceTextView.getText().toString());
                    totalPrice += itemPrice * quantity;
                }
            }
        }
        totalPriceTextView.setText("总价: " + totalPrice);
    }
}