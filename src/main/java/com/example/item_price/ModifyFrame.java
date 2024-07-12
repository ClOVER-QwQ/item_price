package com.example.item_price;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModifyFrame extends AppCompatActivity {

    private int groupId;
    private String groupName;
    private LinearLayout itemInfoLayout; // 更清晰的变量名
    private int counter = 1;
    private List<Item> itemsList = new ArrayList<>();  // 初始化 itemsList
    private String itemName = "";
    private String itemPrice ="";
    BufferedWriter currentWriter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_frame);

        Intent intent = getIntent();
        if (intent != null) {
            groupId = intent.getIntExtra("groupId", -1);
            groupName = intent.getStringExtra("groupName");
            Log.d("ModifyFrame", "Received groupId: " + groupId);
            Log.d("ModifyFrame", "Received groupName: " + groupName);
        }

        TextView groupNameTextView = findViewById(R.id.modify_group_name_text_view);
        Log.d("ModifyFrame", "groupNameTextView is null: " + (groupNameTextView == null));
        if (groupNameTextView != null) {
            groupNameTextView.setText(groupName);
        }

        itemInfoLayout = findViewById(R.id.modify_item_info_layout);
        loadItemsData();

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> saveModifications());

        Button addButton = findViewById(R.id.add_item_button);
        addButton.setOnClickListener(v -> addNewItem());

        Button bk = findViewById(R.id.back_button);
        bk.setOnClickListener(v -> {
            Intent intent2 = new Intent(ModifyFrame.this, MainActivity.class);
            startActivity(intent2);
        });
    }

    private void loadItemsData() {
        Log.d("ModifyFrame", "loadItemsData started");
        File file = new File(getExternalFilesDir(null), "items_" + groupId + ".txt");
        Log.d("ModifyFrame", "Checking file: " + file.getAbsolutePath());
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                int index = 0;
                while ((line = reader.readLine())!= null) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        String name = parts[1];
                        String price = parts[2];

                        View itemView = getLayoutInflater().inflate(R.layout.item, itemInfoLayout, false);

                        Log.d("LoadData", "Inflated itemView: " + itemView); // 新增：打印 itemView

                        EditText nameText = itemView.findViewById(R.id.itemName);
                        Log.d("LoadData", "Before setting nameText: nameText is null? " + (nameText == null));
                        if (nameText!= null) {
                            nameText.setText(name);
                        }

                        EditText priceEditText = itemView.findViewById(R.id.itemPrice);
                        Log.d("LoadData", "Before setting priceEditText: priceEditText is null? " + (priceEditText == null));
                        if (priceEditText!= null) {
                            priceEditText.setText(price);
                        }

                        Button removeButton = itemView.findViewById(R.id.removeItemButton);
                        Log.d("LoadData", "Before setting removeButton: removeButton is null? " + (removeButton == null));
                        if (removeButton!= null) {
                            removeButton.setOnClickListener(v -> {
                                removeItem(itemView);  // 调用新的方法来处理移除逻辑
                            });
                        }

                        if (nameText!= null) {
                            nameText.setId(counter * 2 - 1);
                        }
                        if (priceEditText!= null) {
                            priceEditText.setId(counter * 2);
                        }

                        itemInfoLayout.addView(itemView);

                        Log.d("LoadData", "Added itemView to itemInfoLayout"); // 新增：记录添加视图到布局

                        Item item = new Item();
                        item.setId(counter);
                        itemsList.add(item);
                        counter++;
                    }
                }
            } catch (IOException e) {
                Log.e("ModifyFrame", "读取文件时发生错误", e);
                Toast.makeText(this, "读取文件时发生错误", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("ModifyFrame", "未找到对应的物品文件");
            Toast.makeText(this, "未找到对应的物品文件", Toast.LENGTH_SHORT).show();
        }
    }

    private void addNewItem() {
        View itemView = getLayoutInflater().inflate(R.layout.item, itemInfoLayout, false);
        LinearLayout itemLayout = itemView.findViewById(R.id.item);
        EditText itemNameEditText = itemLayout.findViewById(R.id.itemName);
        Log.d("AddNewItem", "Before setting itemNameEditText: itemNameEditText is null? " + (itemNameEditText == null));
        EditText itemPriceEditText = itemLayout.findViewById(R.id.itemPrice);
        Log.d("AddNewItem", "Before setting itemPriceEditText: itemPriceEditText is null? " + (itemPriceEditText == null));
        Button removeButton = itemLayout.findViewById(R.id.removeItemButton);
        Log.d("AddNewItem", "Before setting removeButton: removeButton is null? " + (removeButton == null));
        if (removeButton!= null) {
            removeButton.setOnClickListener(v -> {
                removeItem(itemLayout);  // 调用新的方法来处理移除逻辑
            });
        }

        if (itemNameEditText!= null) {
            itemNameEditText.setId(counter * 2 - 1);
        }
        if (itemPriceEditText!= null) {
            itemPriceEditText.setId(counter * 2);
        }

        itemInfoLayout.addView(itemView);

        Item newItem = new Item();
        newItem.setId(counter);
        itemsList.add(newItem);
        counter++;

        Toast.makeText(this, "添加新物品成功", Toast.LENGTH_SHORT).show();  // 添加提示信息
    }

    private void removeItem(View itemView) {
        int index = itemInfoLayout.indexOfChild(itemView);
        if (index!= -1) {
            Log.d("RemoveItem", "Removing item at index: " + index);
            itemInfoLayout.removeView(itemView);

            itemsList.remove(index);
            Log.d("RemoveItem", "Removed item. New size of itemsList: " + itemsList.size());

            counter--;
        }
    }

    private void saveModifications() {
        try {
            File file = new File(getExternalFilesDir(null), "items_" + groupId + ".txt");
            currentWriter = new BufferedWriter(new FileWriter(file));

            for (Item item : itemsList) {
                Log.d("SaveItemsToFile", "Current item ID: " + item.getId());
                EditText itemNameEditText = itemInfoLayout.findViewById(item.getId() * 2 - 1);
                Log.d("SaveItemsToFile", "Item name EditText: " + (itemNameEditText == null? "null" : itemNameEditText.getText().toString()));
                EditText itemPriceEditText = itemInfoLayout.findViewById(item.getId() * 2);
                Log.d("SaveItemsToFile", "Item name EditText: " + (itemPriceEditText == null? "null" : itemPriceEditText.getText().toString()));
                itemName = "";
                if (itemNameEditText!= null) {
                    itemName = itemNameEditText.getText().toString().trim();
                } else {
                    itemName = "未命名";
                }

                itemPrice = "";
                if (itemPriceEditText!= null) {
                    itemPrice = itemPriceEditText.getText().toString().trim();
                } else {
                    itemPrice = "0";
                }

                item.setName(itemName);
                item.setPrice(itemPrice);

                currentWriter.write(item.getId() + "," + item.getName() + "," + item.getPrice() + "\n");
            }

            Log.d("SaveItemsToFile", "Finished saving items.");

            currentWriter.close();
            Toast.makeText(this, "保存修改成功", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("SaveItemsToFile", "Failed to save items to file.", e);
            Toast.makeText(this, "保存修改失败，请重试", Toast.LENGTH_SHORT).show();
        }
        Intent intent2 = new Intent(ModifyFrame.this, MainActivity.class);
        startActivity(intent2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ResourceRelease", "Releasing resources in onDestroy");
        if (currentWriter!= null) {
            try {
                currentWriter.close();
            } catch (IOException e) {
                Log.e("ResourceRelease", "Error closing BufferedWriter in onDestroy: " + e.getMessage());
            }
        }
    }
}