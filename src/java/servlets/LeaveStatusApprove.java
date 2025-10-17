/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.CommonMethod.Commons;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.Employee;
import Model.Mapping.LeaveApprovalStatus;
import Model.Mapping.LeaveRequest;
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
 * @author HP
 */
@WebServlet(name = "LeaveStatusApprove", urlPatterns = {"/LeaveStatusApprove"})
public class LeaveStatusApprove extends HttpServlet {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("___________Approve Leave Status________________");

        Commons commonsInstance = new Commons();
        String admin_email = commonsInstance.ADMIN_EMAIL;
        String admin_password = commonsInstance.ADMIN_PASSWORD;

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";

        String leaveid = request.getParameter("leaveid");
        String userId = request.getParameter("userId");

        UserLogin userlogin = (UserLogin) sess.createQuery("From UserLogin Where id='" + userId + "'").setMaxResults(1).uniqueResult();

        Employee employee = (Employee) sess.createQuery("From Employee Where general_user_profile_id='" + userlogin.getGeneralUserProfile().getId() + "'").setMaxResults(1).uniqueResult();

        LeaveRequest leavereq = (LeaveRequest) sess.createQuery("From LeaveRequest Where id='" + leaveid + "'").setMaxResults(1).uniqueResult();

        if (leavereq == null) {
            System.out.println("No Leave Request on this Id");
        } else {

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

            leavereq.setLeaveApprovalStatus((LeaveApprovalStatus) sess.load(LeaveApprovalStatus.class, 2));
            leavereq.setEmployeeByEmployeeIdApprovedBy(employee);
            leavereq.setApprovedDate(convertStringToDate(timeStamp));

            sess.update(leavereq);

            t.commit();

            status = 200;
            message = "Leave Request Approved";
            System.out.println("Leave Request Approved");
            objSend.put("status", status);
            objSend.put("message", message);
            response.getWriter().write(objSend.toString());

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
                message_1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(leavereq.getEmployeeByEmployeeId().getGeneralUserProfile().getEmail()));
                MimeBodyPart textPart = new MimeBodyPart();
                Multipart multipart = new MimeMultipart();

                String formattedFromDate = null;
                String formattedToDate = null;
                String typeofLeave = null;
                if (leavereq.getLeaveType().getName().equals("FullDay")) {
                    typeofLeave = "Full Day Leave";
                    SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy/MM/dd");
                    formattedFromDate = targetFormat.format(leavereq.getDateFrom());
                    formattedToDate = targetFormat.format(leavereq.getDateFrom());
                } else if (leavereq.getLeaveType().getName().equals("Half Day")) {
                    typeofLeave = "Half Day Leave";
                    SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    formattedFromDate = targetFormat.format(leavereq.getDateFrom());
                    formattedToDate = targetFormat.format(leavereq.getDateTo());
                } else if (leavereq.getLeaveType().getName().equals("Short Leave")) {
                    typeofLeave = "Short Leave";
                    SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    formattedFromDate = targetFormat.format(leavereq.getDateFrom());
                    formattedToDate = targetFormat.format(leavereq.getDateTo());
                }

                String final_Text = "<div style=\"text-align:justify; \">\n"
                        + "Dear " + leavereq.getEmployeeByEmployeeId().getGeneralUserProfile().getFirstName() + " " + leavereq.getEmployeeByEmployeeId().getGeneralUserProfile().getLastName() + ",<br>"
                        + "<p>This is a notification to inform you that your leave request has been approved. Please find the details of your approved leave below: </p>\n"
                        + "<p>Leave Type:  " + typeofLeave + "</p>\n"
                        + "<p>Date(s) of Leave:  " + formattedFromDate + " - " + formattedToDate + "</p>\n"
                        + "<p>If you have any questions or need further assistance, please contact your supervisor. </p>\n"
                        + "<p>Thank you. </p>\n"
                        + "<p>Best Regards,</p>\n"
                        + "<p>Exon Software Solutions (Pvt) Ltd</p>\n"
                        + "</div>\n"
                        + "";
                textPart.setText(final_Text);
                multipart.addBodyPart(textPart);
                message_1.setContent(final_Text, "text/html");
                message_1.setSubject("Leave Request Status Update");
                Transport.send(message_1);
                System.out.println("EMAIL SENT - Leave Request Approved");
            } catch (Exception e) {
                message = "Somthing went wrong!";
            }
//emal END
        }
//        sess.flush();
//        sess.clear();
//        sess.close();
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
