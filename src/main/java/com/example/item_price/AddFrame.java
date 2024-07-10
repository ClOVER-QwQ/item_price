package com.example.item_price;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

import static com.example.item_price.R.*;

public class AddFrame extends Activity {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private LinearLayout mainLayout;
    private ArrayList<Item> itemsList;
    private int counter = 1;
    private EditText groupNameInput;
    private BufferedWriter currentWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.add);

        mainLayout = findViewById(id.mainLayout);
        itemsList = new ArrayList<>();
        groupNameInput = findViewById(id.groupNameInput);

        Button addButton = findViewById(id.additembtn);
        addButton.setOnClickListener(v -> {
            Log.d("Input", "Add button Clicked");
            addNewItem();
        });

        Button saveButton = findViewById(id.savebtn);
        saveButton.setOnClickListener(v -> {
            Log.d("Input", "Save button Clicked");
            requestStoragePermission();
        });

        Button backButton = findViewById(id.back);
        backButton.setOnClickListener(v -> {
            Log.d("Input", "Back button Clicked");
            finish();
        });
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "为了保存数据，需要获取存储权限", Toast.LENGTH_LONG).show();
            } else {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            saveItemGroup();
        }
    }

    private int getNextFileId() {
        int fileId;
        SharedPreferences prefs = getSharedPreferences("file_counter_prefs", MODE_PRIVATE);
        fileId = prefs.getInt("file_counter", 0);
        fileId++;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("file_counter", fileId);
        editor.apply();
        return fileId;
    }

    private void addNewItem() {
        String groupName = groupNameInput.getText().toString().trim();
        if (groupName.isEmpty()) {
            Toast.makeText(this, "请输入物品组名称", Toast.LENGTH_SHORT).show();
            return;
        }

        View itemView = getLayoutInflater().inflate(R.layout.item, mainLayout, false);

        LinearLayout itemLayout = itemView.findViewById(R.id.item);
        EditText itemNameEditText = itemLayout.findViewById(id.itemName);
        EditText itemPriceEditText = itemLayout.findViewById(id.itemPrice);
        Button removeButton = itemLayout.findViewById(R.id.removeItemButton);
        removeButton.setOnClickListener(v -> {
            removeItem(itemView);  // 调用新的方法来处理移除逻辑
        });

        itemNameEditText.setId(counter * 2 - 1);
        itemPriceEditText.setId(counter * 2);

        mainLayout.addView(itemView);
        ((LinearLayout.LayoutParams) itemView.getLayoutParams()).topMargin = 10 * counter;

        Item newItem = new Item();
        newItem.setId(counter);
        itemsList.add(newItem);
        counter++;
    }
    private void removeItem(View itemView) {
        int index = mainLayout.indexOfChild(itemView);
        if (index!= -1) {
            Log.d("RemoveItem", "Removing item at index: " + index);
            mainLayout.removeView(itemView);

            itemsList.remove(index);
            Log.d("RemoveItem", "Removed item. New size of itemsList: " + itemsList.size());

            counter--;
        }
    }
    private void saveItemGroup() {
        if (!itemsList.isEmpty()) {
            int fileId = getNextFileId();
            saveItemsToFile(fileId);
            saveGroupIdAndName(fileId);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("saved", true);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "没有物品可保存，请先添加物品", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveItemsToFile(int fileId) {
        try {
            File file = new File(getExternalFilesDir(null), "items_" + fileId + ".txt");
            if (currentWriter == null) {
                currentWriter = new BufferedWriter(new FileWriter(file));
            }

            Log.d("SaveItemsToFile", "Starting to save items. Items count: " + itemsList.size());

            for (Item item : itemsList) {
                Log.d("SaveItemsToFile", "Saving item with ID: " + item.getId() + ", Name: " + item.getName() + ", Price: " + item.getPrice());
                EditText itemNameEditText = mainLayout.findViewById(item.getId() * 2 - 1);
                EditText itemPriceEditText = mainLayout.findViewById(item.getId() * 2);

                String itemName = itemNameEditText.getText().toString().trim();
                String itemPrice = itemPriceEditText.getText().toString().trim();

                item.setName(itemName);
                item.setPrice(itemPrice);

                currentWriter.write(item.getId() + "," + item.getName() + "," + item.getPrice() + "\n");
            }

            Log.d("SaveItemsToFile", "Finished saving items.");

            currentWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("SaveItemsToFile", "Failed to save items to file.", e);
        }
    }
    private void saveGroupIdAndName(int fileId) {
        try {
            File groupsFile = new File(getExternalFilesDir(null), "groups.txt");
            BufferedWriter groupsWriter = new BufferedWriter(new FileWriter(groupsFile, true));
            groupsWriter.write(fileId + "," + groupNameInput.getText().toString() + "\n");
            groupsWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("SaveGroupIdAndName", "Failed to save group id and name.", e);
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveItemGroup();
            } else {
                Toast.makeText(this, "无法保存数据，因为存储权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }
}