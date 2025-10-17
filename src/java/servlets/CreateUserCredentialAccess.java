/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.CommonMethod.Commons;
import Model.Mapping.GeneralUserProfile;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.UserCredentialIssuingManager;
import Model.Mapping.UserCredentials;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
@WebServlet(name = "CreateUserCredentialAccess", urlPatterns = {"/CreateUserCredentialAccess"})

public class CreateUserCredentialAccess extends HttpServlet {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("---credential access- ");

        Commons commonsInstance = new Commons();
        String admin_email = commonsInstance.ADMIN_EMAIL;
        String admin_password = commonsInstance.ADMIN_PASSWORD;

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";
        boolean success = false;
        String credCategory = "";
        String fName = "";
        String lName = "";
        String proName = "";
        String email = "";

        try {

            String creId = request.getParameter("id");

            String credAccessEmp = request.getParameter("accessemp");
            String emp = request.getParameter("emp");

            System.out.println(creId + "," + credAccessEmp + "," + emp);

            String[] string = creId.replaceAll("\\[", "")
                    .replaceAll("]", "")
                    .split(",");

            int[] arr = new int[string.length];

            for (int i = 0; i < string.length; i++) {
                arr[i] = Integer.valueOf(string[i]);
            }

            System.out.print("String : " + creId);
            System.out.print("\nInteger array : "
                    + Arrays.toString(arr));

            UserCredentialIssuingManager credentialM = new UserCredentialIssuingManager();

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

            GeneralUserProfile gup = (GeneralUserProfile) sess.createQuery("From GeneralUserProfile Where id='" + emp + "'").setMaxResults(1).uniqueResult();
            fName = gup.getFirstName();
            lName = gup.getLastName();
            email = gup.getEmail();

            for (int i = 0; i < string.length; i++) {

                UserCredentials credential = (UserCredentials) sess.createQuery("From UserCredentials Where id='" + arr[i] + "'").setMaxResults(1).uniqueResult();
                credCategory = credential.getUserCredentialCategory().getName();
                proName = credential.getProjects().getName();

                if (credential != null) {
                    credentialM.setFirstTimeViewed(false);
                    credentialM.setGeneralUserProfile(gup);
                    credentialM.setIsActive(false);
                    credentialM.setIsChanged(false);
                    credentialM.setIsResigned(false);
                    credentialM.setIssueDate(convertStringToDate(timeStamp));
                    credentialM.setChangedDate(convertStringToDate(timeStamp));
                    credentialM.setUserCredentials(credential);
                    System.out.println(arr[i]);
                    sess.save(credentialM);

                    t.commit();

                    System.out.println("--Sve Credential Access-- ");
                }

            }

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
                message_1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                MimeBodyPart textPart = new MimeBodyPart();
                Multipart multipart = new MimeMultipart();
                String link = "<a href=\"http://system.exon.lk\" style=\"color:#6666CC;\" target=\"_blank\"> <strong><em>Exon Management System</em></strong></a>";

                String final_Text = "<div style=\"text-align:justify; \">\n"
                        + "        Dear " + fName + " " + lName + " ,<br>"
                        + "<p>This is to notify you that access has been granted for you to " + proName + " " + credCategory + ". You can now log in to the system to view this credential. "
                        + "Please note that you will only be able to view them once for security purposes.If you need to view them again, you can request access as needed.</p>\n"
                        + "To view the credentials : <br>\n"
                        + "Visit our login page : " + link + "<br>\n"
                        + "<p>Please ensure that you view and securely store the credentials immediately after logging in, as you will not be able to access them again through this system.</p>\n"
                        + "<p>Best Regards,</p>\n"
                        + "<p>Exon Software Solutions (Pvt) Ltd</p>\n"
                        + "</div>\n"
                        + "";
                textPart.setText(final_Text);
                multipart.addBodyPart(textPart);
                message_1.setContent(final_Text, "text/html");
                message_1.setSubject("Access Granted - " + credCategory );
                Transport.send(message_1);
                System.out.println("emailsending");
            } catch (Exception e) {
                message = "Somthing went wrong!";
            }
//email END

            sess.flush();
            sess.clear();

            status = 200;
            message = "Create Password Access Successfully";
            success = true;

        } catch (Exception e) {
            System.out.println("catch read");
            status = 400;
            message = "Password Access Not Created! Try Again.";

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
