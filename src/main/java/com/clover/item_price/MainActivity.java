package com.clover.item_price;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Group> groups;
    private GroupListAdapter adapter;
    private ActivityResultLauncher<Intent> addFrameLauncher;
    private ActivityResultLauncher<String> importFileLauncher;
    private ActivityResultLauncher<Intent> exportFileLauncher;
    private HashMap<Integer, Group> groupMap;
    private static int groupId;

    private static final int REQUEST_EXPORT_DIRECTORY = 1;
    private static final int REQUEST_IMPORT_FILE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化列表视图和相关数据结构
        ListView listView = findViewById(R.id.listView);
        groups = new ArrayList<>();
        groupMap = new HashMap<>();
        adapter = new GroupListAdapter(this, groups);
        listView.setAdapter(adapter);

        // 注册活动结果启动器
        registerActivityResultLaunchers();

        // 设置按钮的点击事件
        setupButtons();

        // 加载组数据
        loadGroupsFromFile();
    }

    private void registerActivityResultLaunchers() {
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

        importFileLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri!= null) {
                        Log.d("MainActivity", "Received groupId for import: " + groupId);
                        File targetFile = new File(getExternalFilesDir(null), "items_" + groupId + ".txt");
                        handleImportFile(uri, targetFile);
                        loadGroupsFromFile();  // 导入完成后重新加载组数据
                        adapter.notifyDataSetChanged();
                    }
                }
        );

        exportFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data!= null) {
                            Uri uri = data.getData();
                            if (uri!= null) {
                                Log.d("MainActivity", "Passed groupId for export: " + groupId);
                                File sourceFile = new File(getExternalFilesDir(null), "items_" + groupId + ".txt");
                                copyFileToUri(sourceFile, uri);
                            }
                        }
                    }
                }
        );
    }

    private void setupButtons() {
        Button addButton = findViewById(R.id.add);
        addButton.setOnClickListener(view -> {
            Intent add = new Intent(this, AddFrame.class);
            addFrameLauncher.launch(add);
        });

        Button aboutButton = findViewById(R.id.about);
        aboutButton.setOnClickListener(view -> {
            Intent about = new Intent(this, AboutFrame.class);
            startActivity(about);
        });

        Button exportButton = findViewById(R.id.export);
        exportButton.setOnClickListener(view -> showExportDialog());

        Button introduceButton = findViewById(R.id.introduce);
        introduceButton.setOnClickListener(view -> showImportDialog());

        ListView listView = findViewById(R.id.listView);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Group selectedGroup = groups.get(position);
            Intent calculateIntent = new Intent(this, CalculateFrame.class);
            calculateIntent.putExtra("groupId", selectedGroup.getId());
            startActivity(calculateIntent);
        });
    }

    // 显示导出对话框
    private void showExportDialog() {
        Log.d("MainActivity", "Showing export dialog.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Export Group");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String groupName = input.getText().toString().trim();
            Log.d("MainActivity", "Export OK button Clicked. Group name: " + groupName);
            exportGroup(groupName);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            Log.d("MainActivity", "Export Cancel button Clicked.");
            dialog.cancel();
        });

        builder.show();
    }

    // 执行组的导出操作
    private void exportGroup(String groupName) {
        groupId = findGroupId(groupName);
        if (groupId!= -1) {
            File groupFile = new File(getExternalFilesDir(null), "items_" + groupId + ".txt");
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_TITLE, groupFile.getName());
            intent.putExtra("groupId", groupId);
            Log.d("MainActivity", "Passed groupId for export: " + groupId);
            exportFileLauncher.launch(intent);
        } else {
            Toast.makeText(this, "找不到要导出的组", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImportDialog() {
        Log.d("MainActivity", "Showing import dialog.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Import Group");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String groupName = input.getText().toString().trim();
            Log.d("MainActivity", "Import OK button Clicked. Group name: " + groupName);
            importGroup(groupName);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            Log.d("MainActivity", "Import Cancel button Clicked.");
            dialog.cancel();
        });

        builder.show();
    }

    // 导入组
    private void importGroup(String groupName) {
        int newGroupId = getNextGroupId();
        groupId = newGroupId;
        saveGroupToFile(newGroupId, groupName);

        // 调用系统文件管理器选择导入文件
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra("groupId", groupId);
        Log.d("MainActivity", "Passed groupId for import: " + groupId);
        importFileLauncher.launch(intent.getType());
    }

    private void handleImportFile(Uri uri, File targetFile) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile))) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer))!= -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Log.e("MainActivity", "文件导入时出错", e);
            Toast.makeText(this, "导入文件时出错", Toast.LENGTH_SHORT).show();
        }
    }

    private int findGroupId(String groupName) {
        for (Group group : groups) {
            if (group.getName().equals(groupName)) {
                return group.getId();
            }
        }
        return -1;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EXPORT_DIRECTORY && resultCode == RESULT_OK) {
            if (data!= null) {
                Uri uri = data.getData();
                if (uri!= null) {
                    Log.d("MainActivity", "Received groupId after export: " + groupId);
                    File sourceFile = new File(getExternalFilesDir(null), "items_" + groupId + ".txt");
                    copyFileToUri(sourceFile, uri);
                }
            }
        } else if (requestCode == REQUEST_IMPORT_FILE && resultCode == RESULT_OK) {
            if (data!= null) {
                Uri uri = data.getData();
                if (uri!= null) {
                    Log.d("MainActivity", "Received groupId after import: " + groupId);
                    importFileFromUri(data);
                }
            }
        }
    }

    // 将文件复制到指定的 Uri
    private void copyFileToUri(File sourceFile, Uri uri) {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile));
             OutputStream os = getContentResolver().openOutputStream(uri)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = bis.read(buffer))!= -1) {
                os.write(buffer, 0, bytesRead);
            }

        } catch (IOException e) {
            Log.e("MainActivity", "文件复制时出错", e);
            Toast.makeText(this, "导出文件时出错", Toast.LENGTH_SHORT).show();
        }
    }

    // 从 Uri 导入文件
    private void importFileFromUri(Intent data) {
        try (InputStream inputStream = getContentResolver().openInputStream(data.getData())) {
            File targetFile = new File(getExternalFilesDir(null), "items_" + groupId + ".txt");

            try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile))) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer))!= -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            Log.e("MainActivity", "文件导入时出错", e);
            Toast.makeText(this, "导入文件时出错", Toast.LENGTH_SHORT).show();
        }
    }

    // 获取下一个组 ID
    private int getNextGroupId() {
        int maxId = 0;
        for (Group group : groups) {
            if (group.getId() > maxId) {
                maxId = group.getId();
            }
        }
        return maxId + 1;
    }

    // 将组保存到文件
    private void saveGroupToFile(int groupId, String groupName) {
        File groupsFile = new File(getExternalFilesDir(null), "groups.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(groupsFile, true))) {
            writer.write(groupId + "," + groupName + "\n");
        } catch (IOException e) {
            Log.e("MainActivity", "Error saving group to file", e);
            Toast.makeText(this, "保存组信息时出错", Toast.LENGTH_SHORT).show();
        }
    }

    // 从文件加载组数据
    private void loadGroupsFromFile() {
        File groupsFile = new File(getExternalFilesDir(null), "groups.txt");
        if (groupsFile.exists()) {
            groups.clear();
            groupMap.clear();
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
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ResourceRelease", "Releasing resources in onDestroy");
    }
}