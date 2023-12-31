package com.eacg.frame.maintenanceframe;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.eacg.frame.deviceinfoframe.DeviceInfoFrame;
import com.eacg.tools.DBToolSet;

import cn.hutool.core.date.DateUtil;
import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;

public class DeviceMaintenanceFrame extends JFrame {

    static String editingId;
    static String loginUserName;

    private JTextField deviceIdField, maintenanceIdField, maintenanceContentField;
    private JDatePickerImpl maintenanceDatePicker;
    private JButton deleteMaintenanceButton, updateMaintenanceButton, searchMaintenanceButton,
            clearMaintenanceButton;
    private JTable table;
    private DefaultTableModel tableModel;

    public DeviceMaintenanceFrame(DeviceInfoFrame deviceInfoFrame) {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setTitle("设备维护");

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout());
        filterPanel.add(new JLabel("设备ID"));
        deviceIdField = new JTextField(10);
        filterPanel.add(deviceIdField);

        filterPanel.add(new JLabel("维护ID"));
        maintenanceIdField = new JTextField(10);
        filterPanel.add(maintenanceIdField);

        filterPanel.add(new JLabel("维护内容"));
        maintenanceContentField = new JTextField(10);
        filterPanel.add(maintenanceContentField);

        filterPanel.add(new JLabel("维护日期"));
        UtilDateModel maintenanceDateModel = new UtilDateModel();
        JDatePanelImpl maintenanceDatePanel = new JDatePanelImpl(maintenanceDateModel);
        maintenanceDatePicker = new JDatePickerImpl(maintenanceDatePanel);
        filterPanel.add(maintenanceDatePicker);

        JPanel actionsPanel = new JPanel();
        deleteMaintenanceButton = new JButton("删除维护信息");
        deleteMaintenanceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(DeviceMaintenanceFrame.this, "请先选择要删除的维护信息");
                    return;
                }

                if (JOptionPane.showConfirmDialog(DeviceMaintenanceFrame.this, "确定要删除吗？") != JOptionPane.OK_OPTION) {
                    return;
                }
                String deviceId = (String) tableModel.getValueAt(selectedRow, 0);
                DBToolSet.updateSQL("UPDATE busi_device_maintenance SET is_deleted = 1 WHERE id = ?", deviceId);
                JOptionPane.showMessageDialog(DeviceMaintenanceFrame.this, "删除成功");
                tableModel.setRowCount(0);
                loadData();

            }
        });
        updateMaintenanceButton = new JButton("修改维护信息");
        updateMaintenanceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(DeviceMaintenanceFrame.this, "请先选择要修改的维护信息");
                    return;
                }
                DeviceMaintenanceFrame.editingId = (String) tableModel.getValueAt(selectedRow, 0);
                DeviceMaintenanceFrame.loginUserName = deviceInfoFrame.getUserName();
                new ModifyDeviceMaintenanceFrame(DeviceMaintenanceFrame.this).setVisible(true);
                tableModel.setRowCount(0);
                loadData();
            }
        });
        searchMaintenanceButton = new JButton("查询维护信息");
        searchMaintenanceButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String deviceId = deviceIdField.getText();
                String maintenanceId = maintenanceIdField.getText();
                String maintenanceContent = maintenanceContentField.getText();
                String maintenanceDate = maintenanceDatePicker.getJFormattedTextField().getText();
                ArrayList<Object> params = new ArrayList<>();
                String sql = """
                        SELECT
                        	bdf.id AS deviceId,
                        	bdm.id AS MaintanceId,
                        	bdf.device_name AS deviceName,
                        	bdf.device_use_limit AS deviceUseLimit,
                        	bdf.device_status AS deviceStatus,
                        	bdf.device_is_maintain AS deviceIsMaintain,
                        	bdm.maintenance_info AS deviceMaintenceInfo,
                        	bdm.create_user AS maintenanceUser,
                            bdm.create_date AS maintenanceDate
                        FROM
                        	busi_device_maintenance AS bdm LEFT
                        JOIN busi_device_info AS bdf ON bdf.id = bdm.device_id
                        WHERE
                        	bdm.is_deleted = 0
                        	AND bdf.is_deleted = 0
                        	""";
                if (deviceId != null && !deviceId.isEmpty()) {
                    sql += " AND bdf.id LIKE ?";
                    params.add(deviceId);
                }
                if (maintenanceId != null && !maintenanceId.isEmpty()) {
                    sql += " AND bdm.id LIKE ?";
                    params.add(maintenanceId);
                }
                if (maintenanceContent != null && !maintenanceContent.isEmpty()) {
                    sql += " AND bdm.maintenance_info LIKE ?";
                    params.add(maintenanceContent);
                }
                if (maintenanceDate != null && !maintenanceDate.isEmpty()) {
                    sql += " AND bdm.create_date>=? AND bdm.create_date<=?";
                    params.add(DateUtil.parse(maintenanceDate + " 00:00:00"));
                    params.add(DateUtil.parse(maintenanceDate + " 23:59:59"));
                }

                List<Map<String, Object>> devices = DBToolSet.selectSQL(sql, params.toArray());
                tableModel.setRowCount(0);
                for (Map<String, Object> device : devices) {
                    Object[] rowData = new Object[9];
                    rowData[0] = device.get("MaintanceId");
                    rowData[1] = device.get("deviceId");
                    rowData[2] = device.get("deviceName");
                    rowData[3] = device.get("deviceUseLimit");
                    rowData[4] = device.get("deviceStatus");
                    rowData[5] = device.get("deviceIsMaintain");
                    rowData[6] = device.get("deviceMaintenceInfo");
                    rowData[7] = device.get("maintenanceUser");
                    rowData[8] = device.get("maintenanceDate");
                    tableModel.addRow(rowData);
                }
            }

        });
        clearMaintenanceButton = new JButton("清空输入框");
        clearMaintenanceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deviceIdField.setText("");
                maintenanceIdField.setText("");
                maintenanceContentField.setText("");
                maintenanceDatePicker.getJFormattedTextField().setText("");
            }
        });

        actionsPanel.add(deleteMaintenanceButton);
        actionsPanel.add(updateMaintenanceButton);
        actionsPanel.add(searchMaintenanceButton);
        actionsPanel.add(clearMaintenanceButton);

        String[] columnNames = { "维护ID", "设备ID", "设备名称", "使用期限", "设备状态", "是否维护过", "维护信息", "维护人员", "维护日期" };
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Add all panels to the frame
        setLayout(new BorderLayout());
        add(filterPanel, BorderLayout.NORTH);
        add(actionsPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        // Load data
        loadData();
    }

    private void loadData() {
        String sql = """
                SELECT
                 	bdf.id AS deviceId,
                 	bdm.id AS MaintanceId,
                 	bdf.device_name AS deviceName,
                 	bdf.device_use_limit AS deviceUseLimit,
                 	bdf.device_status AS deviceStatus,
                 	bdf.device_is_maintain AS deviceIsMaintain,
                 	bdm.maintenance_info AS deviceMaintenceInfo,
                 	bdm.create_user AS maintenanceUser,
                    bdm.create_date AS maintenanceDate
                 FROM
                 	busi_device_maintenance AS bdm LEFT
                 	JOIN busi_device_info AS bdf ON bdf.id = bdm.device_id
                 WHERE
                 	bdm.is_deleted = 0
                 	AND bdf.is_deleted = 0;""";
        List<Map<String, Object>> devices = DBToolSet.selectSQL(sql);
        for (Map<String, Object> device : devices) {
            Object[] rowData = new Object[9];
            rowData[0] = device.get("MaintanceId");
            rowData[1] = device.get("deviceId");
            rowData[2] = device.get("deviceName");
            rowData[3] = device.get("deviceUseLimit");
            rowData[4] = device.get("deviceStatus");
            rowData[5] = device.get("deviceIsMaintain");
            rowData[6] = device.get("deviceMaintenceInfo");
            rowData[7] = device.get("maintenanceUser");
            rowData[8] = device.get("maintenanceDate");

            tableModel.addRow(rowData);
        }
    }

    public void reloadData() {
        // Clear table
        tableModel.setRowCount(0);
        // Reload data
        loadData();
    }

}
