package com.eacg.frame.userframe;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.eacg.frame.deviceinfoframe.DeviceInfoFrame;
import com.eacg.tools.DBToolSet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserInfoFrame extends JFrame {
    static String userId;
    static String loginNickname;

    private JTextField nicknamField, usernameField, phoneNumberField;
    private JButton searchButton, addButton, modifyButton, deleteButton, clearButton;
    private JComboBox<String> departmentComboBox;
    private JTable table;
    private DefaultTableModel tableModel;
    public UserInfoFrame(DeviceInfoFrame deviceInfoFrame) {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setTitle("用户信息管理");
        UserInfoFrame.loginNickname = deviceInfoFrame.getUserName();
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout());
        filterPanel.add(new JLabel("昵称"));
        nicknamField = new JTextField(10);
        filterPanel.add(nicknamField);
        filterPanel.add(new JLabel("用户名"));
        usernameField = new JTextField(10);
        filterPanel.add(usernameField);
        filterPanel.add(new JLabel("电话号"));
        phoneNumberField = new JTextField(11);
        filterPanel.add(phoneNumberField);
        filterPanel.add(new JLabel("部门"));
        departmentComboBox = new JComboBox<>();
        departmentComboBox.addItem("全部");
        filterPanel.add(departmentComboBox);

        JPanel actionsPanel = new JPanel();
        actionsPanel.setLayout(new FlowLayout());
        addButton = new JButton("添加用户");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AddUserFrame(UserInfoFrame.this).setVisible(true);
            }
        });

        modifyButton = new JButton("修改用户");
        modifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(UserInfoFrame.this, "请选中一行数据");
                    return;
                }
                String id = (String) table.getValueAt(selectedRow, 0);
                UserInfoFrame.userId = id;
                new ModifyUserFrame(UserInfoFrame.this).setVisible(true);
            }
        });

        deleteButton = new JButton("删除用户");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(UserInfoFrame.this, "请选中一行数据");
                    return;
                }
                String id = (String) table.getValueAt(selectedRow, 0);
                int result = JOptionPane.showConfirmDialog(UserInfoFrame.this, "确认删除？");
                if (result == JOptionPane.OK_OPTION) {
                    DBToolSet.updateSQL("update busi_userinfo set is_deleted = 1 where id = ?", id);
                    tableModel.setRowCount(0);
                    JOptionPane.showMessageDialog(UserInfoFrame.this, "删除成功");
                    loadData();
                }
            }
        });
        searchButton = new JButton("查询");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sql = "SELECT * FROM busi_userinfo where is_deleted = 0";
                List<Object> params = new ArrayList<>();
                String nickname = nicknamField.getText().trim();
                if (!nickname.isEmpty()) {
                    sql += " and nickname like ?";
                    params.add("%" + nickname + "%");
                }
                String username = usernameField.getText().trim();
                if (!username.isEmpty()) {
                    sql += " and username like ?";
                    params.add("%" + username + "%");
                }
                String phoneNumber = phoneNumberField.getText().trim();
                if (!phoneNumber.isEmpty()) {
                    sql += " and phone_number like ?";
                    params.add("%" + phoneNumber + "%");
                }
                String department = (String) departmentComboBox.getSelectedItem();
                if (!department.equals("全部")) {
                    sql += " and department_id = ?";
                    params.add(department);
                }
                HashMap<String, String> departmentMap = new HashMap<String, String>();
                List<Map<String, Object>> selectSQL = DBToolSet
                        .selectSQL("select id,department_name from busi_department_info");
                for (Map<String, Object> map : selectSQL) {
                    departmentMap.put((String) map.get("id"), (String) map.get("department_name"));
                }
                List<Map<String, Object>> users = DBToolSet.selectSQL(sql, params.toArray());
                tableModel.setRowCount(0);
                for (Map<String, Object> user : users) {
                    Object[] rowData = new Object[6];
                    rowData[0] = user.get("id");
                    rowData[1] = user.get("nickname");
                    rowData[2] = user.get("username");
                    rowData[3] = user.get("phone_number");
                    rowData[4] = departmentMap.get(user.get("department_id"));
                    rowData[5] = user.get("create_date");
                    tableModel.addRow(rowData);
                }
            }
        });

        clearButton = new JButton("清空");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nicknamField.setText("");
                usernameField.setText("");
                phoneNumberField.setText("");
                departmentComboBox.setSelectedIndex(0);
            }
        });
        actionsPanel.add(addButton);
        actionsPanel.add(modifyButton);
        actionsPanel.add(deleteButton);
        actionsPanel.add(searchButton);
        actionsPanel.add(clearButton);

        String[] columnNames = { "ID", "昵称", "用户名", "电话号", "部门", "创建时间" };
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        setLayout(new BorderLayout());
        add(filterPanel, BorderLayout.NORTH);
        add(actionsPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        // Load data
        loadData();

    }

    private void loadData() {
        HashMap<String, String> departmentMap = new HashMap<String, String>();
        List<Map<String, Object>> selectSQL = DBToolSet
                .selectSQL("select id,department_name from busi_department_info");
        for (Map<String, Object> map : selectSQL) {
            departmentMap.put((String) map.get("id"), (String) map.get("department_name"));
        }

        List<Map<String, Object>> users = DBToolSet.selectSQL("SELECT * FROM busi_userinfo where is_deleted = 0");
        for (Map<String, Object> user : users) {
            Object[] rowData = new Object[6];
            rowData[0] = user.get("id");
            rowData[1] = user.get("nickname");
            rowData[2] = user.get("username");
            rowData[3] = user.get("phone_number");
            rowData[4] = departmentMap.get(user.get("department_id"));
            rowData[5] = user.get("create_date");

            tableModel.addRow(rowData);
        }
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        loadData();
    }
}
