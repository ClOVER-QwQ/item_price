package com.example.item_price;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddFrame extends Activity {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private LinearLayout mainLayout;
    private ArrayList<Item> itemsList;
    private int counter;
    private EditText groupNameInput;
    private BufferedWriter currentWriter; // 用于跟踪当前正在写入的 BufferedWriter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add);

        mainLayout = findViewById(R.id.mainLayout);
        itemsList = new ArrayList<>();
        counter = 1;
        groupNameInput = findViewById(R.id.groupNameInput);

        Button addButton = findViewById(R.id.additembtn);
        addButton.setOnClickListener(v -> {
            Log.d("Input", "Add button Clicked");
            addNewItemGroup();
        });

        Button saveButton = findViewById(R.id.savebtn);
        saveButton.setOnClickListener(v -> {
            Log.d("Input", "Save button Clicked");
            saveItems();
        });

        Button backButton = findViewById(R.id.back);
        backButton.setOnClickListener(v -> {
            Log.d("Input", "Back button Clicked");
            finish();
        });

        requestStoragePermission();
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "为了保存数据，需要获取存储权限", Toast.LENGTH_LONG).show();
            } else {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private void addNewItemGroup() {
        String groupName = groupNameInput.getText().toString().trim();
        if (groupName.isEmpty()) {
            Toast.makeText(this, "请输入物品组名称", Toast.LENGTH_SHORT).show();
            return;
        }

        View itemGroupView = getLayoutInflater().inflate(R.layout.item, mainLayout, false);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText groupNameTextView = itemGroupView.findViewById(R.id.group_name_text);
        if (groupNameTextView!= null) {
            groupNameTextView.setText(groupName);
        }

        mainLayout.addView(itemGroupView);
        ((LinearLayout.LayoutParams) itemGroupView.getLayoutParams()).topMargin = 10;
        counter++;
    }

    private void saveItems() {
        itemsList.clear();

        for (int i = 1; i <= counter; i++) {
            EditText itemNameInput = findViewById(getItemId(R.id.itemName, i));
            EditText itemPriceInput = findViewById(getItemId(R.id.itemPrice, i));

            if (itemNameInput!= null && itemPriceInput!= null) {
                String itemName = itemNameInput.getText().toString();
                double itemPrice;
                try {
                    itemPrice = Double.parseDouble(itemPriceInput.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "价格输入错误", Toast.LENGTH_SHORT).show();
                    return;
                }

                Item newItem = new Item();
                newItem.setId(i);
                newItem.setName(itemName);
                newItem.setPrice(itemPrice);
                itemsList.add(newItem);
            }
        }

        saveItemsToFile();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("saved", true);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void saveItemsToFile() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            HashMap<String, ArrayList<Item>> groupItemsMap = new HashMap<>();

            for (Item item : itemsList) {
                String groupId = "group" + item.getId();
                if (!groupItemsMap.containsKey(groupId)) {
                    groupItemsMap.put(groupId, new ArrayList<>());
                }
                Objects.requireNonNull(groupItemsMap.get(groupId)).add(item);
            }

            File externalDir = getExternalFilesDir(null);
            if (externalDir!= null && externalDir.exists() && externalDir.isDirectory()) {
                for (Map.Entry<String, ArrayList<Item>> entry : groupItemsMap.entrySet()) {
                    String fileName = entry.getKey() + "_" + System.currentTimeMillis() + ".txt";
                    File file = new File(externalDir, fileName);
                    try {
                        if (!file.createNewFile()) {
                            Toast.makeText(this, "无法创建新文件: " + fileName, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        currentWriter = new BufferedWriter(new FileWriter(file)); // 初始化并跟踪当前的 BufferedWriter

                        for (Item item : entry.getValue()) {
                            currentWriter.write(item.getName() + " - " + item.getPrice() + "\n");
                        }

                        currentWriter.close(); // 关闭 BufferedWriter
                        currentWriter = null; // 置空，以便后续判断是否已关闭
                    } catch (IOException e) {
                        Toast.makeText(this, "保存文件时出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("FileSaveError", "Error saving file: " + e.getMessage());
                    }
                }
            } else {
                Toast.makeText(this, "外部存储目录不可用或不是目录", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "外部存储不可写，无法保存文件", Toast.LENGTH_SHORT).show();
        }
    }

    private int getItemId(int baseId, int index) {
        return baseId + index;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ResourceRelease", "Releasing resources in onDestroy");
        if (currentWriter!= null) { // 如果在销毁时仍有未关闭的 BufferedWriter，进行关闭
            try {
                currentWriter.close();
            } catch (IOException e) {
                Log.e("ResourceRelease", "Error closing BufferedWriter in onDestroy: " + e.getMessage());
            }
        }
    }
}