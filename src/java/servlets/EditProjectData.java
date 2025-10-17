/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.CommonMethod.ComPath;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.Projects;
import Model.Mapping.Status;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
 * @author Developer
 */
@WebServlet(name = "EditProjectData", urlPatterns = {"/EditProjectData"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10, // 10 MB
        maxFileSize = 1024 * 1024 * 1000, // 1 GB
        maxRequestSize = 1024 * 1024 * 1000)   	// 1 GB)

public class EditProjectData extends HttpServlet {

    String file_path = ComPath.getFILE_PATH() + "/projectDocument/";

    private static final long serialVersionUID = 1L;

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("_____________Edit  Project  Data_______________");
        response.setContentType("text/html");
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";
        String proStartDate = "";
        String proEndDate = "";
        int statusIdInt = 0;
        Status caa = null;

        try {

            String rowId = request.getParameter("rowIdEdit");
            int rowIdInt = Integer.parseInt(rowId);
            System.out.println(rowId);
            String proName = request.getParameter("editProName1");
            String proDescription = request.getParameter("editProDes1");

            String statusId = request.getParameter("editstatus1");
            if (statusId != null) {
                System.out.println("not null");
                statusIdInt = Integer.parseInt(statusId);
            }

            String startDate = request.getParameter("startDate");
            if (!"".equals(startDate)) {
                SimpleDateFormat startDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date sdate = startDateFormat.parse(startDate);
                SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                proStartDate = outputDateFormat.format(sdate);
                System.out.println(proStartDate);
            }

            String endDate = request.getParameter("endDate");
            if (!"".equals(endDate)) {
                SimpleDateFormat endDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date edate = endDateFormat.parse(endDate);
                SimpleDateFormat outputEndDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                proEndDate = outputEndDateFormat.format(edate);
                System.out.println(proEndDate);
                statusIdInt = 3;
            }

            System.out.println("--1-- " + proName + "," + proDescription + "," + statusId + "," + startDate + "," + endDate);

            Path filepath_photo;

            String uploadDirectory_photo = file_path;
            Part filePart_photo = request.getPart("proDocs");
            String fileName_photo = getFileName(filePart_photo);
            System.out.println("--2-- " + fileName_photo);

            if (!"".equals(fileName_photo)) {
                try (InputStream fileContent_photo = filePart_photo.getInputStream()) {
                    filepath_photo = Paths.get(uploadDirectory_photo, fileName_photo);
                    Files.copy(fileContent_photo, filepath_photo, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            Projects proEdit = (Projects) sess.createQuery("From Projects Where id='" + rowId + "'").setMaxResults(1).uniqueResult();

            if (statusId != null) {
                System.out.println("Not Null");
                Criteria c3 = sess.createCriteria(Status.class);
                c3.add(Restrictions.eq("id", statusIdInt));
                caa = (Status) c3.uniqueResult();
            } else {
                System.out.println("null");
                caa = proEdit.getStatus();
            }

            if (proEdit == null) {
                System.out.println("Project Id Null !!!");
            } else {
                System.out.println("--A-- ");
                proEdit.setName(proName);
                proEdit.setDescription(proDescription);
                proEdit.setStatus(caa);
                proEdit.setProjectsDocument(fileName_photo);
                proEdit.setStartedDate(convertStringToDate(proStartDate));
                proEdit.setCompletedDate(convertStringToDate(proEndDate));

                System.out.println("--B-- ");

                sess.update(proEdit);

                t.commit();
            }

            System.out.println("--C-- ");

            status = 200;
            message = "Project Data Successfully Saved !";

            objSend.put("status", status);
            objSend.put("messageSuccess", message);
            response.sendRedirect("projects/project-list.jsp?messageSuccess=" + "success");
        } catch (Exception e) {
            System.out.println("catch read");
            status = 400;
            message = "Fill All Fields !";

            objSend.put("status", status);
            objSend.put("messageError", message);
            response.sendRedirect("projects/project-list.jsp?messageError=" + "error");
            e.printStackTrace();
        } finally {
            sess.close();
        }

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

    public static Date convertStringToDate(String date) {
        if (!"".equals(date)) {
            try {
                return FORMATTER.parse(date);
            } catch (ParseException e) {
                // nothing we can do if the input is invalid
                throw new RuntimeException(e);
            }
        }
        return null;
    }

}
