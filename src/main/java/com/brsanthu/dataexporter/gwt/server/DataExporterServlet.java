package com.brsanthu.dataexporter.gwt.server;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import au.com.bytecode.opencsv.CSVReader;

import com.brsanthu.dataexporter.DataExporter;
import com.brsanthu.dataexporter.model.json.JsonExporter;
import com.brsanthu.dataexporter.output.csv.CsvExporter;
import com.brsanthu.dataexporter.output.html.HtmlExporter;
import com.brsanthu.dataexporter.output.text.TextExporter;
import com.brsanthu.dataexporter.output.texttable.TextTableExporter;
import com.brsanthu.dataexporter.output.wiki.WikiExporter;
import com.brsanthu.dataexporter.output.xml.XmlExporter;

public class DataExporterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
        IOException {
        doPost(req, resp);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        String operation = req.getParameter("operation");
        if ("create".equalsIgnoreCase(operation)) {
            createFile(req, resp);
        } else {
            writeDownloadResponse(req, resp);
        }
    }

    private void createFile(HttpServletRequest req, HttpServletResponse resp) {
        //get the export format.
        String format = req.getParameter("format");
        String data = req.getParameter("data");
        
        System.out.println("Coming to export with format " + format + " and " + data);
        String exportData = exportData(format, data);
        
        String fileId = UUID.randomUUID().toString();
        File file = new File(getTempDir() + "/" + fileId);
        
        try {
            PrintWriter pw = new PrintWriter(file);
            pw.write(exportData);
            pw.close();
        } catch (Throwable e) {
            throw new RuntimeException("", e);
        }

        writeCreateResponse(req, resp, fileId);
    }

    private void writeCreateResponse(HttpServletRequest req, HttpServletResponse resp, String fileId) {
        disableCache(resp);
        resp.setContentType("text/plain");
        try {
            resp.getWriter().print(fileId);
            resp.flushBuffer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String exportData(String exportFormat, String inputData) {
        DataExporter exporter = null;
        StringWriter sw = new StringWriter();
        
        if ("txt".equalsIgnoreCase(exportFormat) || "text".equalsIgnoreCase(exportFormat)) {
            exporter = new TextExporter(sw);
            
        } else if ("html".equalsIgnoreCase(exportFormat)) {
            exporter = new HtmlExporter(sw);
        
        } else if ("xml".equalsIgnoreCase(exportFormat)) {
            exporter = new XmlExporter(sw);
        
        } else if ("wiki".equalsIgnoreCase(exportFormat)) {
            exporter = new WikiExporter(sw);

        } else if ("json".equalsIgnoreCase(exportFormat)) {
            exporter = new JsonExporter(sw);

        } else if ("texttable".equalsIgnoreCase(exportFormat)) {
            exporter = new TextTableExporter(sw);
            
        } else {
            exporter = new CsvExporter(sw);
        }
            
        CSVReader reader = new CSVReader(new StringReader(inputData));
        try {
            String[] headers = reader.readNext();
            exporter.addColumn(headers);
            exporter.startExporting();

            String[] line = null;
            while((line = reader.readNext()) != null) {
                exporter.addRow((Object[]) line);
            }
            exporter.finishExporting();
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
     
        return sw.toString();
    }
    
    protected void writeDownloadResponse(HttpServletRequest req, HttpServletResponse resp) {
        String fileName = req.getParameter("filename");
        String format = req.getParameter("format");
        
        if (fileName == null || fileName.trim().length() == 0) {
            fileName = "data-export";
        }
        
        String localFile = getTempDir() + "/" + req.getParameter("id");
        
        System.out.println("Downloading file " + localFile);
        
        try {
            
            String preferredExtn = null;
            String mimeType = null;
            
            if ("txt".equalsIgnoreCase(format) || "text".equalsIgnoreCase(format) || "texttable".equalsIgnoreCase(format)) {
                preferredExtn = ".txt";
                mimeType = "text/plain";
                
            } else if ("html".equalsIgnoreCase(format)) {
                preferredExtn = ".html";
                mimeType = "text/html";
            
            } else if ("xml".equalsIgnoreCase(format)) {
                preferredExtn = ".xml";
                mimeType = "text/xml";
            
            } else if ("wiki".equalsIgnoreCase(format)) {
                preferredExtn = ".wiki";
                mimeType = "text/plain";
    
            } else if ("json".equalsIgnoreCase(format)) {
                preferredExtn = ".json";
                mimeType = "application/json";
                
            } else {
                preferredExtn = ".csv";
                mimeType = "text/csv";
            }
            
            if (!fileName.endsWith(preferredExtn)) {
                fileName += preferredExtn;
            }
            
            disableCache(resp);
            resp.setContentType(mimeType);
            resp.setHeader( "Content-Disposition", "attachment; filename=\""+fileName+"\"" );
            
            ByteArrayOutputStream bo;
            
            BufferedInputStream bi =
                new BufferedInputStream(new FileInputStream(localFile));
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            bo = new ByteArrayOutputStream();
            while ((bytesRead = bi.read(buffer)) >= 0) {
                bo.write(buffer, 0, bytesRead);
            }
            bi.close();
        
            byte[] data = bo.toByteArray();
            resp.setContentLength(data.length);
            
            System.out.println("Writing the response to file " + fileName + " data " + new String(data));
        
            resp.getWriter().print(new String(data));
            resp.flushBuffer();
        } catch (IOException e) {
            throw new RuntimeException(e);
            
        } finally {
            new File(localFile).delete();
        }
    }

    public void disableCache(HttpServletResponse res) {
        // Set to expire far in the past.
        res.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");

        // Set standard HTTP/1.1 no-cache headers.
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        res.addHeader("Cache-Control", "post-check=0, pre-check=0");

        // Set standard HTTP/1.0 no-cache header.
        res.setHeader("Pragma", "no-cache");        
    }

    public String getTempDir() {
        return System.getProperty("java.io.tmpdir");
    }
    
}
