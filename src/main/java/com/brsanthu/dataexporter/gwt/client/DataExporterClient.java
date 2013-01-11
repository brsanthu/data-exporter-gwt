package com.brsanthu.dataexporter.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;

@SuppressWarnings("synthetic-access")
public class DataExporterClient {
        
    private String serverExportPath = null;
    
    public DataExporterClient() {
        serverExportPath = GWT.getHostPageBaseURL() + "/gwtExporter";
    }
    
    public String getServerExportPath() {
        return serverExportPath;
    }

    public void setServerExportPath(String serverExportPath) {
        this.serverExportPath = serverExportPath;
    }

    public void export(final ExportData data) {
        
        GWT.log("Exporting using server url " + serverExportPath + " and data is \n" + data.getCsv());
        
        RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, serverExportPath);
        rb.setHeader("Content-Type", "application/x-www-form-urlencoded");
        try {
            rb.sendRequest("operation=create&format=" + data.getFormat() + "&data=" + data.getCsv(), new RequestCallback() {
                
                public void onResponseReceived(Request request, Response response) {
                    String downloadFileUrl = serverExportPath + "?operation=download&id=" + response.getText() + "&filename=" + data.getFileName() + "&format=" + data.getFormat();
                    GWT.log("Downloading the file " + downloadFileUrl);
                    Window.Location.replace(downloadFileUrl);
                }
                
                public void onError(Request request, Throwable exception) {
                    GWT.log("Coming to on error " + exception.getMessage());
                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }
}
