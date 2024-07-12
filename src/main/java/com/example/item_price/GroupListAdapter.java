package com.example.item_price;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class GroupListAdapter extends ArrayAdapter<Group> {

    private List<Group> groups;
    private Context context;
    private MainActivity mainActivity;  // 新增，保存 MainActivity 的引用
    public GroupListAdapter(Context context, List<Group> groups, MainActivity mainActivity) {  // 修改构造函数，接收 MainActivity 的引用
        super(context, 0, groups);
        this.groups = groups;
        this.context = context;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_item_layout, parent, false);
        Group group = groups.get(position);
        TextView groupNameTextView = view.findViewById(R.id.group_name_text);
        Button modifyButton = view.findViewById(R.id.edit_button);
        Button deleteButton = view.findViewById(R.id.delete_button);
        Button calculateButton = view.findViewById(R.id.calculate_button);

        groupNameTextView.setText(group.getName());

        modifyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent doing = new Intent(context, ModifyFrame.class);
                doing.putExtra("groupName", group.getName());
                doing.putExtra("groupId", group.getId());
                context.startActivity(doing);
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理删除逻辑
                groups.remove(group);
                deleteGroupFromFile(group.getId());  // 删除文件中的行记录
                deleteItemFile(group.getId());  // 删除对应的 item 文件
                notifyDataSetChanged();
                Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
            }
        });

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理计算逻辑
                Intent add = new Intent(context, CalculateFrame.class);
                add.putExtra("groupId", group.getId());  // 这里传入 groupId
                add.putExtra("groupName", group.getName());  // 新增：传入物品组名称
                context.startActivity(add);
            }
        });

        return view;
    }

    private void deleteGroupFromFile(int groupId) {
        File groupsFile = new File(context.getExternalFilesDir(null), "groups.txt");
        if (groupsFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(groupsFile));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(groupsFile + ".tmp"))) {
                String line;
                while ((line = reader.readLine())!= null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2 && Integer.parseInt(parts[0])!= groupId) {
                        writer.write(line + System.lineSeparator());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            // 替换原始文件
            if (new File(groupsFile + ".tmp").renameTo(groupsFile)) {
                Toast.makeText(context, "Group 删除成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Group 删除失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteItemFile(int groupId) {
        File itemFile = new File(context.getExternalFilesDir(null), "items_" + groupId + ".txt");
        if (itemFile.exists()) {
            if (itemFile.delete()) {
                Toast.makeText(context, "对应的 item 文件删除成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "对应的 item 文件删除失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}