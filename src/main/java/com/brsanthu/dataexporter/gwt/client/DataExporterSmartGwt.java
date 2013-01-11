package com.brsanthu.dataexporter.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.MenuItemIfFunction;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;

public class DataExporterSmartGwt extends DataExporterClient {
    
    public static DataExporterSmartGwt instance = new DataExporterSmartGwt();
    
    public void export(ListGrid grid, boolean allRows, ExportFormat format, String fileName) {
        ExportData exportData = new ExportData();
        exportData.setFormat(format);
        exportData.setFileName(fileName);
        List<String> headerNames = new ArrayList<String>();
        
        for (ListGridField field : grid.getFields()) {
            if (!field.getCanHide()) {
                
                String title = field.getTitle();
                
                //Strip the html tags around it.
                title = title.replace("<b>", "");
                title = title.replace("</b>", "");
                exportData.addHeader(title);
                
                headerNames.add(field.getName());
            }
        }

        ListGridRecord[] records = null;
        if (allRows) {
            records = grid.getRecords();
        } else {
            records = grid.getSelection();
        }
        
        for (ListGridRecord record : records) {
            List<String> row = new ArrayList<String>();
            
            for (String name : headerNames) {
                row.add(record.getAttribute(name));
            }
            
            exportData.addRow(row);
        }
        
        export(exportData);
    }
    
    public void createGridExportMenu(final ListGrid grid) {
        
        Menu menu = new Menu();
        menu.setWidth(150);
        MenuItem exportSelected = new MenuItem("Export Selected Rows");
        exportSelected.setEnableIfCondition(new MenuItemIfFunction() {
            public boolean execute(Canvas target, Menu menu, MenuItem item) {
                return grid.getSelectedRecord() != null;
            }
        });
        exportSelected.setSubmenu(createExportOptions(grid, false));
        menu.addItem(exportSelected);
        
        MenuItem exportAll = new MenuItem("Export All Rows");
        exportAll.setEnableIfCondition(new MenuItemIfFunction() {
            public boolean execute(Canvas target, Menu menu, MenuItem item) {
                return grid.getRecords().length != 0;
            }
        });
        exportAll.setSubmenu(createExportOptions(grid, true));
        menu.addItem(exportAll);
        
        grid.setContextMenu(menu);
    }
    
    public Menu createExportOptions(final ListGrid grid, final boolean allRows) {
        
        Menu menu = new Menu();
        menu.addItem(createExportOption(grid, allRows, ExportFormat.Csv));
        menu.addItem(createExportOption(grid, allRows, ExportFormat.Html));
        menu.addItem(createExportOption(grid, allRows, ExportFormat.Json));
        menu.addItem(createExportOption(grid, allRows, ExportFormat.Text));
        //menu.addItem(createExportOption(grid, allRows, ExportFormat.TextTable));
        menu.addItem(createExportOption(grid, allRows, ExportFormat.Xml));
        
        return menu;
    }
    
    public MenuItem createExportOption(final ListGrid grid, final boolean allRows, final ExportFormat format) {
        MenuItem exportOption = new MenuItem(format.toString());
        exportOption.setEnableIfCondition(new MenuItemIfFunction() {
            public boolean execute(Canvas target, Menu menu, MenuItem item) {
                
                if (allRows) {
                    return grid.getSelection().length != 0;
                }
                
                return grid.getRecords().length != 0;
            }
        });
        exportOption.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
            public void onClick(MenuItemClickEvent event) {
                export(grid, allRows, format, "data-export");
            }
        });
        
        return exportOption;
    }
}
