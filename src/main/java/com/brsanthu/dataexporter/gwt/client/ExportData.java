package com.brsanthu.dataexporter.gwt.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.http.client.URL;

public class ExportData implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String csvColSeparator = ",";
	private static final String csvRowSeparator = "\n";

	public static String replReservedSymbs(String txt) {
		String result = txt;
		result = result.replaceAll("&", "&amp;");
		return result;
	}

	private List<List<String>> rows = new ArrayList<List<String>>();
	private List<String> headers = new ArrayList<String>();
	private ExportFormat format = null;
	private String fileName = null;
	
	public ExportData() {
	}

	public String getCsv() {
		String result = "";
		for (int i = 0; i < headers.size(); i++) {
		    
		    if (i == headers.size() - 1) {
		        result = result + headers.get(i);
		    } else {
		        result = result + headers.get(i) + csvColSeparator;
		    }
		}
		
		result = result + csvRowSeparator;
		for (int r = 0; r < rows.size(); r++) {
			for (int c = 0; c < rows.get(r).size(); c++) {
			    
			    if (c == rows.get(r).size() - 1) {
			        result = result + encode(rows.get(r).get(c));
			    } else {
			        result = result + encode(rows.get(r).get(c)) + csvColSeparator;
			    }
			        
			}
			
			result = result + csvRowSeparator;
		}
		return result;
	}
	
	private String encode(String input) {
	    if (input == null) {
	        return "";
	    }
	    
	    if (input.contains("\"")) {
	        input = input.replace("\"", "\"\"");
	    }
	    
        input = "\"" + input + "\"";
        
        return URL.encodeComponent(input);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<String> getHeaders() {
        return headers;
    }
	
	public List<List<String>> getRows() {
        return rows;
    }
	
	public ExportFormat getFormat() {
        return format;
    }

    public void setFormat(ExportFormat format) {
        this.format = format;
    }

    public void addHeader(String header) {
	    headers.add(header);
	}
	
	public void addRow(List<String> row) {
	    rows.add(row);
	}
}