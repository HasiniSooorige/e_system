/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.CommonMethod.ComPath;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.DocumentType;
import Model.Mapping.Employee;
import Model.Mapping.EmployeeDocuments;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.json.simple.JSONObject;

/**
 *
 * @author HP
 */
@WebServlet(name = "EmployeeDocumentUpload", urlPatterns = {"/EmployeeDocumentUpload"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10, // 10 MB
        maxFileSize = 1024 * 1024 * 1000, // 1 GB
        maxRequestSize = 1024 * 1024 * 1000)   	// 1 GB)
public class EmployeeDocumentUpload extends HttpServlet {

    String file_path = ComPath.getFILE_PATH() + "/EmployeeDocument/";

    private static final long serialVersionUID = 1L;
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("_____________Employee  Document  Upload__________________");

        response.setContentType("text/html");
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();

        int status = 200;
        String message = "";

        String empId = request.getParameter("empId");
        int empIdInt = Integer.parseInt(empId);
        String docType = request.getParameter("docType");
        int docTypeInt = Integer.parseInt(docType);
        String docName = request.getParameter("docName");
        System.out.println("--1-- " + empId + "," + docType + "," + docName);

        Path filepath_photo;

        String uploadDirectory_photo = file_path;
        Part filePart_photo = request.getPart("empDoc");
        String fileName_photo = getFileName(filePart_photo);
        System.out.println("--2-- " + fileName_photo);

        if (!"".equals(fileName_photo)) {
            try (InputStream fileContent_photo = filePart_photo.getInputStream()) {
                filepath_photo = Paths.get(uploadDirectory_photo, fileName_photo);
                Files.copy(fileContent_photo, filepath_photo, StandardCopyOption.REPLACE_EXISTING);
            }

            Criteria c = sess.createCriteria(DocumentType.class);
            c.add(Restrictions.eq("id", docTypeInt));
            DocumentType dt = (DocumentType) c.uniqueResult();

            Criteria c1 = sess.createCriteria(Employee.class);
            c1.add(Restrictions.eq("id", empIdInt));
            Employee emp = (Employee) c1.uniqueResult();

            EmployeeDocuments empDoc = new EmployeeDocuments();
            empDoc.setName(docName);
            empDoc.setUrl(fileName_photo);
            empDoc.setDocumentType(dt);
            empDoc.setEmployee(emp);

            sess.save(empDoc);

            t.commit();
            sess.flush();
            sess.clear();

            status = 200;
            message = "Document Successfully Saved !";

            objSend.put("status", status);
            objSend.put("messageDocSuccess", message);
            response.sendRedirect("main-pages/employee-list.jsp?messageDocSuccess=" + "success");

        } else {
            System.out.println("Document Name Can't Empty !");
            status = 400;
            message = "Document Name Can't Empty !";

            objSend.put("status", status);
            objSend.put("messageDocError", message);
            response.sendRedirect("main-pages/employee-list.jsp?messageDocError=" + "error");
        }

        sess.close();
    }

    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        String[] parts = contentDisposition.split(";");
        for (String partValue : parts) {
            if (partValue.trim().startsWith("filename")) {
                return partValue.substring(partValue.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
}
