/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.CommonMethod.ComPath;
import Model.CommonMethod.Commons;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.GeneralUserProfile;
import Model.Mapping.HelpTicket;
import Model.Mapping.HelpTicketRespond;
import Model.Mapping.HelpTicketRespondType;
import Model.Mapping.HelpTicketStatus;
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
@WebServlet(name = "HelpTicketResponseAdd", urlPatterns = {"/HelpTicketResponseAdd"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10, // 10 MB
        maxFileSize = 1024 * 1024 * 1000, // 1 GB
        maxRequestSize = 1024 * 1024 * 1000)   	// 1 GB)
public class HelpTicketResponseAdd extends HttpServlet {

    String file_path = ComPath.getFILE_PATH() + "/HelpTicketResponse/";

    private static final long serialVersionUID = 1L;

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

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
        String tikID = "";
        try {
            String adminGUP = request.getParameter("adminGUP");
            String ticketid = request.getParameter("ticketid");
            tikID = request.getParameter("tikID");
            String messageinput = request.getParameter("message");
            String tikcketStatus = request.getParameter("tikcketStatus");

            Path filepath_photo;

            String uploadDirectory_photo = file_path;
            Part filePart_photo = request.getPart("helpres");
            String fileName_photo = getFileName(filePart_photo);

            if (!"".equals(fileName_photo)) {
                try (InputStream fileContent_photo = filePart_photo.getInputStream()) {
                    filepath_photo = Paths.get(uploadDirectory_photo, fileName_photo);
                    Files.copy(fileContent_photo, filepath_photo, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            HelpTicketRespond htr = new HelpTicketRespond();

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

            htr.setRespondDate(convertStringToDate(timeStamp));
            htr.setComment(messageinput);
            if (!"".equals(fileName_photo)) {
                htr.setDoc(fileName_photo);
            }
            htr.setHelpTicketRespondType((HelpTicketRespondType) sess.load(HelpTicketRespondType.class, 1));
            htr.setHelpTicket((HelpTicket) sess.load(HelpTicket.class, Integer.parseInt(tikID)));
            htr.setRespondedGup((GeneralUserProfile) sess.load(GeneralUserProfile.class, Integer.parseInt(adminGUP)));

            sess.save(htr);

            HelpTicket ht = (HelpTicket) sess.createQuery("From HelpTicket Where id='" + tikID + "'").setMaxResults(1).uniqueResult();
            if ((tikcketStatus == null) || ("undefined".equals(tikcketStatus))) {
                System.out.println("Status not Changed");
            } else {
                ht.setHelpTicketStatus((HelpTicketStatus) sess.load(HelpTicketStatus.class, Integer.parseInt(tikcketStatus)));
                sess.update(ht);
            }

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
                message_1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(ht.getRequestedBy().getEmail()));
                MimeBodyPart textPart = new MimeBodyPart();
                Multipart multipart = new MimeMultipart();

                String helpTicketDashboard = "<a href=\"http://system.exon.lk/help-ticket/user-help-ticket-dashboard.jsp\" style=\"color:#000000;\" target=\"_blank\"> <strong><em>Help Desk System</em></strong></a>";

                String final_Text = "<div style=\"text-align:justify; \">\n"
                        + "        Dear " + ht.getRequestedBy().getFirstName() + " " + ht.getRequestedBy().getLastName() + ",<br>"
                        + "<p>This is the latest update regarding the help ticket you submitted with the following details:</p>\n"
                        + "Ticket ID&nbsp;:&nbsp;&nbsp;" + ticketid + "<br>\n"
                        + "Category&nbsp;&nbsp;:&nbsp;&nbsp;" + ht.getHelpTicketCategory().getName() + "<br>\n"
                        + "<p>Please feel free to respond to this log in to our help desk system at " + helpTicketDashboard + ".</p>\n"
                        + "<p>Best Regards,</p>\n"
                        + "<p>Exon Software Solutions (Pvt) Ltd</p>\n"
                        + "</div>\n"
                        + "";

                textPart.setText(final_Text);
                multipart.addBodyPart(textPart);
                message_1.setContent(final_Text, "text/html");

                message_1.setSubject("Help Ticket Update - Ticket ID " + ticketid);
                Transport.send(message_1);
                System.out.println("emailsending");
            } catch (Exception e) {
                message = "Somthing went wrong!";
            }
//emal END

            status = 200;
            message = "Response Successfully Saved !";

            objSend.put("status", status);
            objSend.put("messageSuccess", message);
            response.sendRedirect("help-ticket/admin-ticket-view.jsp?tid=" + tikID + "&messageSuccess=" + "success");

            sess.flush();
            sess.clear();

        } catch (Exception e) {
            status = 400;
            message = "Response Not Saved !";

            objSend.put("status", status);
            objSend.put("messageError", message);
            response.sendRedirect("help-ticket/admin-ticket-view.jsp?tid=" + tikID + "&messageError=" + "error");
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
