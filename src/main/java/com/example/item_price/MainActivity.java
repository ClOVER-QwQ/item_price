package com.example.item_price;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Group> groups;
    private GroupListAdapter adapter;
    private ActivityResultLauncher<Intent> addFrameLauncher;
    private HashMap<Integer, Group> groupMap; // 用于快速查找Group
    private BufferedReader currentReader; // 用于跟踪当前正在使用的 BufferedReader

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.listView);
        groups = new ArrayList<>();
        groupMap = new HashMap<>(); // 初始化Group查找Map
        adapter = new GroupListAdapter(this, groups);
        listView.setAdapter(adapter);

        addFrameLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data!= null && data.getBooleanExtra("saved", false)) {
                            // 数据已保存，现在可以刷新列表视图
                            loadItemsFromFiles(); // 调用你的加载数据的方法
                            adapter.notifyDataSetChanged(); // 刷新适配器
                        }
                    }
                }
        );

        // 当点击添加按钮时调用
        Button addbutton = findViewById(R.id.add);
        addbutton.setOnClickListener(view -> {
            Intent add = new Intent(MainActivity.this, AddFrame.class);
            addFrameLauncher.launch(add);
        });

        addFrameLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data!= null) {
                            Group newGroup = data.getParcelableExtra("newGroup");
                            if (newGroup!= null) {
                                groups.add(newGroup);
                                groupMap.put(newGroup.getId(), newGroup); // 同步到Map
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
        );

        // 加载已保存的物品组
        loadItemsFromFiles();

        Button aboutbutton = findViewById(R.id.about);

        aboutbutton.setOnClickListener(view -> {
            Intent about = new Intent(MainActivity.this, AboutFrame.class);
            startActivity(about);
        });
    }

    private void loadItemsFromFiles() {
        File dir = getFilesDir(); // 改为使用内部存储
        File[] files = dir.listFiles((dir1, name) -> name.endsWith(".txt"));

        if (files!= null) {
            for (File file : files) {
                try {
                    currentReader = new BufferedReader(new FileReader(file)); // 初始化并跟踪当前的 BufferedReader

                    String line;
                    while ((line = currentReader.readLine())!= null) {
                        String[] parts = line.split(",");
                        if (parts.length == 3) {
                            int id = Integer.parseInt(parts[0]);
                            String name = parts[1];
                            double price = Double.parseDouble(parts[2]);

                            Item item = new Item();
                            // 假设每个文件对应一个Group，这里可以根据文件名解析出Group ID
                            int groupId = Integer.parseInt(file.getName().replace("group_", "").replace(".txt", ""));
                            // 创建或查找对应的Group对象，然后将Item添加到Group中
                            Group group = findOrCreateGroup(groupId);
                            group.addItem(item);
                        }
                    }

                    currentReader.close(); // 关闭 BufferedReader
                    currentReader = null; // 置空，以便后续判断是否已关闭
                } catch (IOException e) {
                    Log.e("MainActivity", "读取文件时发生错误: " + file.getName(), e);
                }
            }
        }
    }

    private Group findOrCreateGroup(int groupId) {
        // 首先尝试从Map中查找Group
        Group group = groupMap.get(groupId);
        if (group == null) {
            // 如果找不到，创建一个新的Group
            group = new Group(groupId, "新物品组 " + groupId);
            groups.add(group);
            groupMap.put(groupId, group); // 同步到Map
            adapter.notifyDataSetChanged(); // 刷新ListView以显示新Group
        }
        return group;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ResourceRelease", "Releasing resources in onDestroy");
        if (currentReader!= null) { // 如果在销毁时仍有未关闭的 BufferedReader，进行关闭
            try {
                currentReader.close();
            } catch (IOException e) {
                Log.e("ResourceRelease", "Error closing BufferedReader in onDestroy: " + e.getMessage());
            }
        }
    }
}