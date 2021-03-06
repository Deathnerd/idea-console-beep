package net.ishchenko.consolebeep;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.TableUtil;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 16.04.11
 * Time: 16:56
 */
public class BeepSettingsConfigurable extends BaseConfigurable {

    private Project project;
    private JBTable table;
    private boolean dirty;

    public BeepSettingsConfigurable(Project project) {
        this.project = project;
    }

    @Nls
    public String getDisplayName() {
        return "Console Beep Patterns";
    }

    public JComponent createComponent() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        JPanel buttonsPanel = new JPanel();
        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TableUtil.stopEditing(table);
                int index = table.getSelectionModel().getMinSelectionIndex();
                if (index != -1) {
                    ((BeepSettingsTableModel) table.getModel()).removeRow(index);
                }
            }
        });
        buttonsPanel.add(removeButton);
        panel.add(buttonsPanel, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout());
        table = new JBTable();
        table.setRowHeight(25);
        tablePanel.add(table.getTableHeader(), BorderLayout.NORTH);
        tablePanel.add(table, BorderLayout.CENTER);

        panel.add(new JBScrollPane(tablePanel), BorderLayout.CENTER);

        return panel;

    }

    public void apply() throws ConfigurationException {

        BeepSettings settings = new BeepSettings();
        settings.setSettings(((BeepSettingsTableModel) table.getModel()).settings);
        ServiceManager.getService(project, BeeperProjectComponent.class).loadState(settings);

        dirty = false;

    }

    public void reset() {

        BeeperApplicationComponent beeperApplicationComponent = ServiceManager.getService(BeeperApplicationComponent.class);
        BeeperProjectComponent beeperProjectComponent = ServiceManager.getService(project, BeeperProjectComponent.class);

        table.setModel(new BeepSettingsTableModel(beeperProjectComponent.getState().getSettings()));
        table.getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                dirty = true;
            }
        });

        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JComboBox(beeperApplicationComponent.getSoundKeys())));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

    }

    @Override
    public boolean isModified() {
        return dirty;
    }

    public Icon getIcon() {
        return null;
    }

    public String getHelpTopic() {
        return null;
    }

    public void disposeUIResources() {
    }

    private class BeepSettingsTableModel extends AbstractTableModel {

        private String[] columns = {"Enabled", "Pattern", "Sound"};

        private List<BeepSettings.PatternBeep> settings;

        public BeepSettingsTableModel(List<BeepSettings.PatternBeep> settings) {
            this.settings = new ArrayList<BeepSettings.PatternBeep>(settings.size());
            for (BeepSettings.PatternBeep beep : settings) {
                this.settings.add(beep.clone());
            }
        }

        public int getRowCount() {
            return settings.size();
        }

        public int getColumnCount() {
            return 3;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            BeepSettings.PatternBeep row = settings.get(rowIndex);
            if (columnIndex == 0) {
                return row.isEnabled();
            } else if (columnIndex == 1) {
                return row.getPattern();
            } else if (columnIndex == 2) {
                return row.getBeep();
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return rowIndex < settings.size() && columnIndex != 3;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            BeepSettings.PatternBeep row = settings.get(rowIndex);
            if (columnIndex == 0) {
                row.setEnabled((Boolean) aValue);
            } else if (columnIndex == 1) {
                row.setPattern((String) aValue);
            } else if (columnIndex == 2) {
                row.setBeep((String) aValue);
            } else {
                return;
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Boolean.class;
            } else if (columnIndex == 1) {
                return String.class;
            }
            return Object.class;
        }

        public void removeRow(int index) {
            settings.remove(index);
            fireTableDataChanged();
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }
    }

}



