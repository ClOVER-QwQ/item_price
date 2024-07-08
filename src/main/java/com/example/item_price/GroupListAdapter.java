package com.example.item_price;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;

import java.util.List;
public class GroupListAdapter extends ArrayAdapter<Group> {

    private List<Group> groups;
    private MainActivity mainActivity;

    public GroupListAdapter(MainActivity context, List<Group> groups) {
        super(context, 0, groups);
        this.groups = groups;
        this.mainActivity = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mainActivity).inflate(R.layout.group_item_layout, parent, false);
        }

        TextView groupNameText = convertView.findViewById(R.id.group_name_text);
        Button editButton = convertView.findViewById(R.id.edit_button);
        Button deleteButton = convertView.findViewById(R.id.delete_button);
        Button calculateButton = convertView.findViewById(R.id.calculate_button);

        Group group = groups.get(position);

        groupNameText.setText(group.getName());

        editButton.setOnClickListener(v -> {
            // 修改按钮点击事件
            // 可以启动一个新的Activity或Fragment来编辑Group
        });

        deleteButton.setOnClickListener(v -> {
            // 删除按钮点击事件
            groups.remove(position);
            notifyDataSetChanged();
        });

        calculateButton.setOnClickListener(v -> {
            // 计算按钮点击事件
            // 显示Group的总价或执行其他计算逻辑
        });

        return convertView;
    }
}
