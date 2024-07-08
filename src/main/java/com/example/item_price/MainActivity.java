package com.example.item_price;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
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
    private HashMap<Integer, Group> groupMap;
    private BufferedReader currentReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.listView);
        groups = new ArrayList<>();
        groupMap = new HashMap<>();
        adapter = new GroupListAdapter(this, groups, this);  // 创建 GroupListAdapter 时传递 MainActivity 引用

        listView.setAdapter(adapter);

        addFrameLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data!= null && data.getBooleanExtra("saved", false)) {
                            loadGroupsFromFile();
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
        );

        Button addbutton = findViewById(R.id.add);
        addbutton.setOnClickListener(view -> {
            Intent add = new Intent(MainActivity.this, AddFrame.class);
            addFrameLauncher.launch(add);
        });

        loadGroupsFromFile();

        Button aboutbutton = findViewById(R.id.about);
        aboutbutton.setOnClickListener(view -> {
            Intent about = new Intent(MainActivity.this, AboutFrame.class);
            startActivity(about);
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Group selectedGroup = groups.get(position);
                Intent calculateIntent = new Intent(MainActivity.this, CalculateFrame.class);
                calculateIntent.putExtra("groupId", selectedGroup.getId());  // 传递组 ID
                startActivity(calculateIntent);
            }
        });
    }

    private void loadGroupsFromFile() {
        File groupsFile = new File(getExternalFilesDir(null), "groups.txt");
        if (groupsFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(groupsFile))) {
                String line;
                while ((line = reader.readLine())!= null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        int groupId = Integer.parseInt(parts[0]);
                        String groupName = parts[1];
                        Group group = new Group(groupId, groupName);
                        groups.add(group);
                        groupMap.put(groupId, group);
                    }
                }
            } catch (IOException e) {
                Log.e("MainActivity", "读取 groups 文件时发生错误", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ResourceRelease", "Releasing resources in onDestroy");
        if (currentReader!= null) {
            try {
                currentReader.close();
            } catch (IOException e) {
                Log.e("ResourceRelease", "Error closing BufferedReader in onDestroy: " + e.getMessage());
            }
        }
    }
}
