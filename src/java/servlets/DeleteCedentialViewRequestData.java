/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.CommonMethod.Commons;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.UserCredentialViewRequest;
import java.io.IOException;
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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;

@WebServlet(name = "DeleteCedentialViewRequestData", urlPatterns = {"/DeleteCedentialViewRequestData"})

public class DeleteCedentialViewRequestData extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, NullPointerException {

        Commons commonsInstance = new Commons();
        String admin_email = commonsInstance.ADMIN_EMAIL;
        String admin_password = commonsInstance.ADMIN_PASSWORD;

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        int status = 200;
        String message = "";
        JSONObject objSend = new JSONObject();
        String credCategory = "";
        String fName = "";
        String lName = "";
        String proName = "";
        String email = "";
        String sup_email = "umesha@exon.lk";

        try {
            String id = req.getParameter("id");

            UserCredentialViewRequest mn = (UserCredentialViewRequest) sess.createQuery("From UserCredentialViewRequest Where id='" + id + "'").setMaxResults(1).uniqueResult();
            fName = mn.getUserCredentialIssuingManager().getGeneralUserProfile().getFirstName();
            lName = mn.getUserCredentialIssuingManager().getGeneralUserProfile().getLastName();
            email = mn.getUserCredentialIssuingManager().getGeneralUserProfile().getEmail();
            proName = mn.getUserCredentialIssuingManager().getUserCredentials().getProjects().getName();
            credCategory = mn.getUserCredentialIssuingManager().getUserCredentials().getUserCredentialCategory().getName();

            if (mn == null) {

            } else {
                mn.setIsActive(false);
                sess.update(mn);
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
                message_1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                MimeBodyPart textPart = new MimeBodyPart();
                Multipart multipart = new MimeMultipart();

                String final_Text = "<div style=\"text-align:justify; \">\n"
                        + "        Dear " + fName + " " + lName + " ,<br>"
                        + "<p>We are pleased to inform you that your request for access to view " + proName + " " + credCategory + " has been denied.</p>\n"
                        + "<p>If you have any questions or require further clarification regarding the denial, please feel free to reach out at " + sup_email +".</p>\n"
                        + "<p>Best Regards,</p>\n"
                        + "<p>Exon Software Solutions (Pvt) Ltd</p>\n"
                        + "</div>\n"
                        + "";
                textPart.setText(final_Text);
                multipart.addBodyPart(textPart);
                message_1.setContent(final_Text, "text/html");
                message_1.setSubject("Access Denied  - " + credCategory);
                Transport.send(message_1);
                System.out.println("emailsending");
            } catch (Exception e) {
                message = "Somthing went wrong!";
            }
//email END

            status = 200;
            message = "Request Rejected Successfully.";
            System.out.println("Request Rejected Successfully.");

            sess.flush();
            sess.clear();

        } catch (Exception e) {
            System.out.println("catch read");
            status = 400;
            message = "Request Not Rejected. Try Again";
            System.out.println("Request Not Rejected. Try Again");
            e.printStackTrace();
        } finally {
            sess.close();

        }

        objSend.put("status", status);
        objSend.put("message", message);
        resp.getWriter().write(objSend.toString());
    }

}
