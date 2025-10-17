/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.CommonMethod.Com;
import Model.CommonMethod.ComPath;
import Model.CommonMethod.Commons;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.Employee;
import Model.Mapping.GeneralOrganizationProfile;
import Model.Mapping.ProjectEmployeeRole;
import Model.Mapping.ProjectEmployees;
import Model.Mapping.Projects;
import Model.Mapping.Status;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author HP
 */
@WebServlet(name = "CreateNewProject", urlPatterns = {"/CreateNewProject"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10, // 10 MB
        maxFileSize = 1024 * 1024 * 1000, // 1 GB
        maxRequestSize = 1024 * 1024 * 1000)   	// 1 GB)

public class CreateNewProject extends HttpServlet {

    String file_path = ComPath.getFILE_PATH() + "/projectLogo/";

    private static final long serialVersionUID = 1L;
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("_____________Create  New  Project_______________");

        Commons commonsInstance = new Commons();
        String admin_email = commonsInstance.ADMIN_EMAIL;
        String admin_password = commonsInstance.ADMIN_PASSWORD;

        String today = Com.getDate(new Date());

        response.setContentType("text/html");
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";

        try {

            String proName = request.getParameter("projectname");
            String proDescription = request.getParameter("projectDescription");
            String clientName = request.getParameter("organization");
            Integer clientInt = Integer.parseInt(clientName);
            System.out.println("--1-- " + proName + "," + proDescription + "," + clientName);

            Path filepath_photo;

            String uploadDirectory_photo = file_path;
            Part filePart_photo = request.getPart("fileProject");
            String fileName_photo = getFileName(filePart_photo);
            System.out.println("--2-- " + fileName_photo);

            if (!"".equals(fileName_photo)) {
                try (InputStream fileContent_photo = filePart_photo.getInputStream()) {
                    filepath_photo = Paths.get(uploadDirectory_photo, fileName_photo);
                    Files.copy(fileContent_photo, filepath_photo, StandardCopyOption.REPLACE_EXISTING);
                }

                String[] empId = request.getParameterValues("valueEmp[]");
                String[] empName = request.getParameterValues("textEmp[]");
                String[] empRoleId = request.getParameterValues("valueRole[]");
                String[] empRole = request.getParameterValues("textRole[]");

                int[] empIdInt = new int[empId.length];
                for (int i = 0; i < empId.length; i++) {
                    empIdInt[i] = Integer.parseInt(empId[i]);
                    System.out.println("Emp Id Int --" + empIdInt[i]);
                }

                int[] empRoleIdInt = new int[empId.length];
                for (int i = 0; i < empRoleId.length; i++) {
                    empRoleIdInt[i] = Integer.parseInt(empRoleId[i]);
                    System.out.println("Emp Role Id Int --" + empRoleIdInt[i]);
                }

                JSONArray jsonArray = new JSONArray();

                for (int i = 0; i < empId.length; i++) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("valueEmp", empIdInt[i]);
                    jsonObject.put("textEmp", empName[i]);
                    jsonObject.put("valueRole", empRoleIdInt[i]);
                    jsonObject.put("textRole", empRole[i]);

                    jsonArray.add(jsonObject);

                }
                String json_authors = jsonArray.toString();

                Criteria c3 = sess.createCriteria(Status.class);
                c3.add(Restrictions.eq("id", 2));
                Status caa = (Status) c3.uniqueResult();

                GeneralOrganizationProfile gop = (GeneralOrganizationProfile) sess.createQuery("From GeneralOrganizationProfile Where id='" + clientInt + "'").setMaxResults(1).uniqueResult();
                int gopId = gop.getId();
                System.out.println("gopId == " + gopId);

                Projects pro = (Projects) sess.createQuery("From Projects Where name='" + proName + "' and general_organization_profile_id='" + gop + "'").setMaxResults(1).uniqueResult();

                String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
                if (pro == null) {
                    pro = new Projects();

                    pro.setName(proName);
                    pro.setGeneralOrganizationProfile(gop);
                    pro.setDescription(proDescription);
                    pro.setStatus(caa);
                    pro.setLogoUrl(fileName_photo);
                    pro.setStartedDate(convertStringToDate(timeStamp));
                    pro.setIsActive(true);
                    sess.save(pro);
                    t.commit();

                    Gson gson = new Gson();
                    Type listType = new TypeToken<ArrayList<ProjectEmployees>>() {
                    }.getType();

                    List<ProjectEmployees> userArray = gson.fromJson(json_authors, listType);

                    Session sess1 = NewHibernateUtil.getSessionFactory().openSession();

                    int i = 0;

                    for (ProjectEmployees ram : userArray) {

                        Transaction t1 = sess1.beginTransaction();

                        Criteria c4 = sess1.createCriteria(Employee.class);
                        c4.add(Restrictions.eq("id", empIdInt[i]));
                        Employee em = (Employee) c4.uniqueResult();

                        Criteria c8 = sess1.createCriteria(ProjectEmployeeRole.class);
                        c8.add(Restrictions.eq("id", empRoleIdInt[i]));
                        ProjectEmployeeRole proRole = (ProjectEmployeeRole) c8.uniqueResult();

                        ProjectEmployees pm = new ProjectEmployees();
                        pm.setAssignedDate(convertStringToDate(timeStamp));
                        pm.setProjects(pro);
                        pm.setEmployee(em);
                        pm.setProjectEmployeeRole(proRole);
                        pm.setIsActive(true);
                        sess1.save(pm);

                        i = i + 1;

                        //                            email START
                        Properties props = new Properties();
                        props.put("mail.smtp.auth", true);
                        props.put("mail.smtp.starttls.enable", true);
                        props.put("mail.smtp.host", "smtp.gmail.com");
                        props.put("mail.smtp.port", "587");
                        javax.mail.Session session = javax.mail.Session.getInstance(props,
                                new javax.mail.Authenticator() {
                            @Override
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(admin_email, admin_password);
                            }
                        });
                        try {
                            Message message_1 = new MimeMessage(session);
                            message_1.setFrom(new InternetAddress("Exon"));
                            message_1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(em.getGeneralUserProfile().getEmail()));
                            MimeBodyPart textPart = new MimeBodyPart();
                            Multipart multipart = new MimeMultipart();
                            String link = "<a href=\"http://system.exon.lk\" style=\"color:#6666CC;\" target=\"_blank\"> <strong><em>Exon Management System</em></strong></a>";

                            String final_Text = "<div style=\"text-align:justify; \">\n"
                                    + "        Dear " + em.getGeneralUserProfile().getFirstName() + " " + em.getGeneralUserProfile().getLastName() + ",<br>"
                                    + "<p>This email is to inform you that you have been assigned to the " + proName + " project, effective " + today + ". Your role in this project will be " + proRole.getName() + ".</p>\n"
                                    + "<p>Please acknowledge receipt of this assignment at your earliest convenience.</p>\n"
                                    + "<p>Best Regards,</p>\n"
                                    + "<p>Exon Software Solutions (Pvt) Ltd</p>\n"
                                    + "</div>\n"
                                    + "";
                            textPart.setText(final_Text);
                            multipart.addBodyPart(textPart);
                            message_1.setContent(final_Text, "text/html");
                            message_1.setSubject("Project Assignment");
                            Transport.send(message_1);
                            System.out.println("Create Project Email Sending - " + em.getGeneralUserProfile().getFirstName());
                        } catch (Exception e) {
                            message = "Somthing went wrong!";
                        }
//emal END

                        t1.commit();
                        sess1.clear();
                    }

                    status = 200;
                    message = "Project Data Successfully Saved !";

                    objSend.put("status", status);
                    objSend.put("messageSuccess", message);
                    response.sendRedirect("projects/create-projects.jsp?messageSuccess=" + "success");

                    sess1.close();

                } else {

                    System.out.println("Project Data Already Exist");
                    status = 400;
                    message = "Project Data Already Exist !";

                    objSend.put("status", status);
                    objSend.put("messageExistError", message);
                    response.sendRedirect("projects/create-projects.jsp?messageExistError=" + "error");
                }
            } else {
                System.out.println("Project Logo Can`t Empty !");
                status = 400;
                message = "Project Logo Can`t Empty !";

                objSend.put("status", status);
                objSend.put("messagelogoError", message);
                response.sendRedirect("projects/create-projects.jsp?messagelogoError=" + "error");
            }
        } catch (Exception e) {
            System.out.println("catch read");
            status = 400;
            message = "Fill All Fields !";

            objSend.put("status", status);
            objSend.put("messageError", message);
            response.sendRedirect("projects/create-projects.jsp?messageError=" + "error");
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
        if (date != null) {
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
