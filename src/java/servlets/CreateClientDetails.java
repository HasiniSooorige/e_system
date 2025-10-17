/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Com.Tools.Security;
import Model.CommonMethod.ComPath;
import Model.CommonMethod.Commons;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.Country;
import Model.Mapping.GeneralOrganizationProfile;
import Model.Mapping.GeneralUserProfile;
import Model.Mapping.GupGopManager;
import Model.Mapping.OrganizationType;
import Model.Mapping.UserLogin;
import Model.Mapping.UserRole;
import Utilities.PassGen;
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
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;

/**
 *
 * @author HP
 */
@WebServlet(name = "CreateClientDetails", urlPatterns = {"/CreateClientDetails"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10, // 10 MB
        maxFileSize = 1024 * 1024 * 1000, // 1 GB
        maxRequestSize = 1024 * 1024 * 1000)   	// 1 GB)
public class CreateClientDetails extends HttpServlet {

    String file_path = ComPath.getFILE_PATH() + "/clientLogo/";

    private static final long serialVersionUID = 1L;
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Commons commonsInstance = new Commons();
        String admin_email = commonsInstance.ADMIN_EMAIL;
        String admin_password = commonsInstance.ADMIN_PASSWORD;

        response.setContentType("text/html");
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";

        try {

            String cname = request.getParameter("cname");
            String countryId = request.getParameter("country");
            String address1 = request.getParameter("address1");
            String address2 = request.getParameter("address2");
            String address3 = request.getParameter("address3");
            String cEmail = request.getParameter("cEmail");
            String cMobile = request.getParameter("cMobile");
            String cFaxNo = request.getParameter("cFaxNo");

            String clientFirstName = request.getParameter("clientFirstName");
            String clientLastName = request.getParameter("clientLastName");
            String clientNic = request.getParameter("clientNic");
            String clientemail = request.getParameter("clientemail");
            String companycontactNo = request.getParameter("companycontactNo");
            String clientMobileNo = request.getParameter("clientMobileNo");

            Path filepath_photo;

            String uploadDirectory_photo = file_path;
            Part filePart_photo = request.getPart("file1");
            String fileName_photo = getFileName(filePart_photo);

            try (InputStream fileContent_photo = filePart_photo.getInputStream()) {
                filepath_photo = Paths.get(uploadDirectory_photo, fileName_photo);
                Files.copy(fileContent_photo, filepath_photo, StandardCopyOption.REPLACE_EXISTING);
            }

            GeneralOrganizationProfile gop = (GeneralOrganizationProfile) sess.createQuery("From GeneralOrganizationProfile Where name='" + cname + "'").setMaxResults(1).uniqueResult();
            if (gop == null) {

                GeneralUserProfile gup = (GeneralUserProfile) sess.createQuery("From GeneralUserProfile Where nic='" + clientNic + "' or email='" + clientemail + "'").setMaxResults(1).uniqueResult();
                if (gup == null) {

                    UserLogin userlogin = (UserLogin) sess.createQuery("From UserLogin Where username='" + clientNic + "'").setMaxResults(1).uniqueResult();
                    if (userlogin == null) {

                        gop = new GeneralOrganizationProfile();

                        gop.setName(cname);
                        gop.setAddress1(address1);
                        gop.setAddress2(address2);
                        gop.setAddress3(address3);
                        gop.setCreatedDate(convertStringToDate(timeStamp));
                        gop.setOrganizationType((OrganizationType) sess.load(OrganizationType.class, 3));
                        gop.setCountry((Country) sess.load(Country.class, Integer.parseInt(countryId)));
                        gop.setLogo(fileName_photo);
                        gop.setEmail(cEmail);
                        gop.setContactNo(cMobile);
                        gop.setFaxNo(cFaxNo);

                        gup = new GeneralUserProfile();

                        gup.setNic(clientNic);
                        gup.setFirstName(clientFirstName);
                        gup.setLastName(clientLastName);
                        gup.setAddress1(address1);
                        gup.setAddress2(address2);
                        gup.setAddress3(address3);
                        gup.setMobileNo(clientMobileNo);
                        gup.setHomeNo(companycontactNo);
                        gup.setEmail(clientemail);
                        gup.setProfileCreatedDate(convertStringToDate(timeStamp));
                        gup.setCountry((Country) sess.load(Country.class, Integer.parseInt(countryId)));

                        GupGopManager gupGopM = new GupGopManager();

                        gupGopM.setGeneralUserProfile(gup);
                        gupGopM.setGeneralOrganizationProfile(gop);
                        gupGopM.setIsActive(Boolean.TRUE);

                        String password = new PassGen().generate(PassGen.getValid("[a-z]", Character.MAX_VALUE), 8);

                        userlogin = new UserLogin();

                        userlogin.setUsername(clientNic);
                        userlogin.setPassword(Security.encrypt(password));
                        userlogin.setMaxLoginAttemp(3);
                        userlogin.setCountAttempt(0);
                        userlogin.setIsActive(Boolean.TRUE);
                        userlogin.setGeneralUserProfile(gup);
                        userlogin.setUserRole((UserRole) sess.load(UserRole.class, 4));

                        sess.save(gop);
                        sess.save(gup);
                        sess.save(gupGopM);
                        sess.save(userlogin);

                        t.commit();

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
                            message_1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(clientemail));
                            MimeBodyPart textPart = new MimeBodyPart();
                            Multipart multipart = new MimeMultipart();

                            String link = "<a href=\"http://system.exon.lk\" style=\"color:#6666CC;\" target=\"_blank\"> <strong><em>Exon Management System</em></strong></a>";

                            String final_Text = "<div style=\"text-align:justify; \">\n"
                                    + "        Dear " + clientFirstName + " " + clientLastName + ",<br>"
                                    + "<p>We are delighted to welcome you to Exon Software Solutions (PVT) Ltd! Your registration is complete, and we are excited to have you as part of our valued client community.</p>\n"
                                    + "<p>To access your client portal, please find your login credentials below:</p>\n"
                                    + "Username&nbsp;:&nbsp;&nbsp;" + clientNic + "<br>\n"
                                    + "Temporary Password&nbsp;&nbsp;:&nbsp;&nbsp;" + password + "<br>\n"
                                    + "<p><span style=\"background-color: yellow;\"><b>For security reasons, we recommend changing your password upon your first login.</b></span></p>\n"
                                    + "Visit our client portal login page&nbsp;:&nbsp;&nbsp;" + link + "<br>\n"
                                    + "<p>If you have any questions or encounter any issues during the login process, feel free to reach out to our support team at umesha@exon.lk</p>\n"
                                    + "<p>Once again, welcome to Exon Software Solutions. We look forward to delivering exceptional services and building a successful partnership with you.</p>\n"
                                    + "<p>Best Regards,</p>\n"
                                    + "<p>Exon Software Solutions (Pvt) Ltd</p>\n"
                                    + "</div>\n"
                                    + "";

                            textPart.setText(final_Text);
                            multipart.addBodyPart(textPart);
                            message_1.setContent(final_Text, "text/html");

                            message_1.setSubject("Welcome to Exon Software Solutions - Your Client Portal Login Credentials");
                            Transport.send(message_1);
                            System.out.println("emailsending");
                        } catch (Exception e) {
                            message = "Somthing went wrong!";
                        }
//emal END

                        status = 200;
                        message = "Client Data Successfully Saved !";

                        objSend.put("status", status);
                        objSend.put("messageClientSuccess", message);
                        response.sendRedirect("projects/create-projects.jsp?messageClientSuccess=" + "success");

                    } else {

                        status = 400;
                        message = "UserName Already Used !";

                        objSend.put("status", status);
                        objSend.put("clientUserNameError", message);
                        response.sendRedirect("projects/create-projects.jsp?clientUserNameError=" + "error");
                    }
                } else {
                    status = 400;
                    message = "Client Reference Member Already Exists !";

                    objSend.put("status", status);
                    objSend.put("clientMemkberExistsError", message);
                    response.sendRedirect("projects/create-projects.jsp?clientMemkberExistsError=" + "error");
                }

            } else {

                status = 400;
                message = "Client Already Exists !";

                objSend.put("status", status);
                objSend.put("clientExistsError", message);
                response.sendRedirect("projects/create-projects.jsp?clientExistsError=" + "error");
            }

            sess.flush();
            sess.clear();

        } catch (Exception e) {
            System.out.println("catch read");
            status = 400;
            message = "Fill All Fields !";

            objSend.put("status", status);
            objSend.put("messageClientError", message);
            response.sendRedirect("projects/create-projects.jsp?messageClientError=" + "error");
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
