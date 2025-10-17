/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.CommonMethod.Commons;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.UserCredentialIssuingManager;
import Model.Mapping.UserCredentialViewRequest;
import Model.Mapping.UserLogin;
import java.io.IOException;
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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;

/**
 *
 * @author Jalana
 */
@WebServlet(name = "CreateUserCredentiaLViewRequest", urlPatterns = {"/CreateUserCredentiaLViewRequest"})

public class CreateUserCredentiaLViewRequest extends HttpServlet {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {

        Commons commonsInstance = new Commons();
        String admin_email = commonsInstance.ADMIN_EMAIL;
        String admin_password = commonsInstance.ADMIN_PASSWORD;

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";
        String credCategory = "";
        String fName = "";
        String lName = "";
        String proName = "";

        try {

            String reqCredId = request.getParameter("empAcessId");
            UserCredentialViewRequest req = new UserCredentialViewRequest();

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

            req.setRequestedDate(convertStringToDate(timeStamp));
            req.setUserCredentialIssuingManager((UserCredentialIssuingManager) sess.load(UserCredentialIssuingManager.class, Integer.parseInt(reqCredId)));
            req.setIsActive(Boolean.TRUE);
            sess.save(req);

            UserCredentialIssuingManager mn = (UserCredentialIssuingManager) sess.createQuery("From UserCredentialIssuingManager Where id='" + reqCredId + "'").setMaxResults(1).uniqueResult();
            fName = mn.getGeneralUserProfile().getFirstName();
            lName = mn.getGeneralUserProfile().getLastName();
            credCategory = mn.getUserCredentials().getUserCredentialCategory().getName();
            proName = mn.getUserCredentials().getProjects().getName();

            mn.setIsChanged(true);
            sess.update(mn);

            t.commit();

            UserLogin admin = (UserLogin) sess.createQuery("From UserLogin Where user_role_id='" + 1 + "'").setMaxResults(1).uniqueResult();
            String adminEmail = admin.getGeneralUserProfile().getEmail();

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
                message_1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(adminEmail));
                MimeBodyPart textPart = new MimeBodyPart();
                Multipart multipart = new MimeMultipart();
                String link = "<a href=\"http://system.exon.lk\" style=\"color:#6666CC;\" target=\"_blank\"> <strong><em>Exon Management System</em></strong></a>";

                String final_Text = "<div style=\"text-align:justify; \">\n"
                        + "        Dear Admin ,<br>"
                        + "<p>This is a notification to inform you that " + fName + " " + lName + " has submitted a request for access to view " + proName + " " + credCategory + ".</p>\n"
                        + "<p>Please review the request and take appropriate action by granting or denying access.</p>\n"
                        + "<p>Best Regards,</p>\n"
                        + "<p>Exon Software Solutions (Pvt) Ltd</p>\n"
                        + "</div>\n"
                        + "";
                textPart.setText(final_Text);
                multipart.addBodyPart(textPart);
                message_1.setContent(final_Text, "text/html");
                message_1.setSubject("Credential Access Request - " + fName + " " + lName);
                Transport.send(message_1);
                System.out.println("emailsending");
            } catch (Exception e) {
                message = "Somthing went wrong!";
            }
//email END

            status = 200;
            message = "Request Successfully Send";

            sess.flush();
            sess.clear();

        } catch (Exception e) {
            System.out.println("catch read");
            status = 400;
            message = "Request Not Send. Try Again.";

            e.printStackTrace();
        } finally {
            sess.close();
        }
        objSend.put("status", status);
        objSend.put("message", message);
        resp.getWriter().print(objSend);

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
