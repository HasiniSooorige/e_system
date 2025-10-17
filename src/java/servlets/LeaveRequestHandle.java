/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.CommonMethod.Commons;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.Attendance;
import Model.Mapping.Employee;
import Model.Mapping.Gender;
import Model.Mapping.GeneralUserProfile;
import Model.Mapping.LeaveApprovalStatus;
import Model.Mapping.LeaveRequest;
import Model.Mapping.LeaveType;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import org.json.simple.JSONObject;

@WebServlet(name = "LeaveRequestHandle", urlPatterns = {"/LeaveRequestHandle"})

public class LeaveRequestHandle extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Commons commonsInstance = new Commons();
        String admin_email = commonsInstance.ADMIN_EMAIL;
        String admin_password = commonsInstance.ADMIN_PASSWORD;

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String reqDate = request.getParameter("reqdate");
        String nic = request.getParameter("nic");
        String reasonLeave = request.getParameter("reasonLeave");
        String comment = request.getParameter("comment");
        String startdate = request.getParameter("startdate");
        String enddate = request.getParameter("enddate");
        String dayType = request.getParameter("dayType");
        String halfDate = request.getParameter("halfDate");
        String shTimeFrom = request.getParameter("shTimeFrom");
        String shTimeTo = request.getParameter("shTimeTo");
        String shortLeaveDate = request.getParameter("shortLeaveDate");
        String leaveType = request.getParameter("leaveType");

        if (dayType.equals("AM")) {
            startdate = halfDate + " 08:00:00";
            enddate = halfDate + " 12:00:00";
        }
        if (dayType.equals("PM")) {
            startdate = halfDate + " 12:00:00";
            enddate = halfDate + " 17:00:00";
        }

        if (leaveType.equals("Short Leave")) {
            startdate = shortLeaveDate + " " + shTimeFrom + ":00";
            enddate = shortLeaveDate + " " + shTimeTo + ":00";
        }

        Date date = null;
        Date sdate = null;
        Date edate = null;
        Date shalfDay = null;
        Date ehalfDay = null;

//        fullday block  
        try {
            date = dateFormat.parse(reqDate);
            sdate = dateFormat.parse(startdate);
            edate = dateFormat.parse(enddate);
        } catch (ParseException ex) {
            System.out.println("full day error");
        }

//      halfday block
        try {
            shalfDay = dateTimeFormat.parse(startdate);
            ehalfDay = dateTimeFormat.parse(enddate);
        } catch (Exception e) {
            System.out.println("Half day error");
        }

//      short Leave Block
        try {
            shalfDay = dateTimeFormat.parse(startdate);
            ehalfDay = dateTimeFormat.parse(enddate);

        } catch (Exception e) {
            System.out.println("short leave error");
        }

        GeneralUserProfile userId = (GeneralUserProfile) sess.createQuery("From GeneralUserProfile Where nic='" + nic + "'").setMaxResults(1).uniqueResult();

        Employee employee = (Employee) sess.createQuery("From Employee Where general_user_profile_id='" + userId.getId() + "'").setMaxResults(1).uniqueResult();

        if (leaveType.equals("FullDay")) {

            LeaveRequest leaveRequest = new LeaveRequest();

            leaveRequest.setRequestedDate(date);
            leaveRequest.setReason(reasonLeave);
            leaveRequest.setComment(comment);

            Criteria c12 = sess.createCriteria(LeaveType.class);
            c12.add(Restrictions.eq("name", "Full Day"));
            LeaveType leaveType2 = (LeaveType) c12.uniqueResult();
            leaveRequest.setLeaveType(leaveType2);

            leaveRequest.setDateFrom(sdate);
            leaveRequest.setDateTo(edate);

            Criteria c13 = sess.createCriteria(LeaveApprovalStatus.class);
            c13.add(Restrictions.eq("name", "Pending"));
            LeaveApprovalStatus leaveApp = (LeaveApprovalStatus) c13.uniqueResult();
            leaveRequest.setLeaveApprovalStatus(leaveApp);

            leaveRequest.setEmployeeByEmployeeId(employee);

            sess.save(leaveRequest);
            t.commit();
            status = 201;

            objSend.put("status", status);
            objSend.put("message", message);
            response.getWriter().print(objSend);

        } else if (leaveType.equals("Half Day")) {

            LeaveRequest leaveRequest = new LeaveRequest();

            leaveRequest.setRequestedDate(date);
            leaveRequest.setEmployeeByEmployeeId(employee);
            leaveRequest.setReason(reasonLeave);
            leaveRequest.setComment(comment);

            Criteria c12 = sess.createCriteria(LeaveType.class);
            c12.add(Restrictions.eq("name", "Half Day"));
            LeaveType leaveType2 = (LeaveType) c12.uniqueResult();
            leaveRequest.setLeaveType(leaveType2);

            leaveRequest.setDateFrom(shalfDay);
            leaveRequest.setDateTo(ehalfDay);

            Criteria c13 = sess.createCriteria(LeaveApprovalStatus.class);
            c13.add(Restrictions.eq("name", "Pending"));
            LeaveApprovalStatus leaveApp = (LeaveApprovalStatus) c13.uniqueResult();
            leaveRequest.setLeaveApprovalStatus(leaveApp);

            sess.save(leaveRequest);
            t.commit();
            status = 202;

            objSend.put("status", status);
            objSend.put("message", message);
            response.getWriter().print(objSend);

        } else if (leaveType.equals("Short Leave")) {

            LeaveRequest leaveRequest = new LeaveRequest();

            leaveRequest.setRequestedDate(date);
            leaveRequest.setEmployeeByEmployeeId(employee);
            leaveRequest.setReason(reasonLeave);
            leaveRequest.setComment(comment);

            Criteria c12 = sess.createCriteria(LeaveType.class);
            c12.add(Restrictions.eq("name", "Short Leave"));
            LeaveType leaveType2 = (LeaveType) c12.uniqueResult();
            leaveRequest.setLeaveType(leaveType2);

            leaveRequest.setDateFrom(shalfDay);
            leaveRequest.setDateTo(ehalfDay);

            Criteria c13 = sess.createCriteria(LeaveApprovalStatus.class);
            c13.add(Restrictions.eq("name", "Pending"));
            LeaveApprovalStatus leaveApp = (LeaveApprovalStatus) c13.uniqueResult();
            leaveRequest.setLeaveApprovalStatus(leaveApp);

            sess.save(leaveRequest);

            t.commit();
            status = 203;

            objSend.put("status", status);
            objSend.put("message", message);
            response.getWriter().print(objSend);

        } else {
            status = 300;
            objSend.put("status", status);
            objSend.put("message", message);
            response.getWriter().print(objSend);
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
            message_1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(admin_email));
            MimeBodyPart textPart = new MimeBodyPart();
            Multipart multipart = new MimeMultipart();

            String formattedFromDate = null;
            String formattedToDate = null;
            String typeofLeave = null;
            if (leaveType.equals("FullDay")) {
                typeofLeave = "Full Day Leave";
                SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy/MM/dd");
                formattedFromDate = targetFormat.format(sdate);
                formattedToDate = targetFormat.format(edate);
            } else if (leaveType.equals("Half Day")) {
                typeofLeave = "Half Day Leave";
                SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                formattedFromDate = targetFormat.format(shalfDay);
                formattedToDate = targetFormat.format(ehalfDay);
            } else if (leaveType.equals("Short Leave")) {
                typeofLeave = "Short Leave";
                SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                formattedFromDate = targetFormat.format(shalfDay);
                formattedToDate = targetFormat.format(ehalfDay);
            }

            String final_Text = "<div style=\"text-align:justify; \">\n"
                    + "Dear Admin, <br>"
                    + "<p>This is a notification to inform you that " + employee.getGeneralUserProfile().getFirstName() + " " + employee.getGeneralUserProfile().getLastName() + " has submitted a leave request. "
                    + "Please find the details of the request below and review it at your earliest convenience: </p>\n"
                    + "<p>Employee Name: " + employee.getGeneralUserProfile().getFirstName() + " " + employee.getGeneralUserProfile().getLastName() + "</p>\n"
                    + "<p>Leave Type:  " + typeofLeave + "</p>\n"
                    + "<p>Date(s) of Leave:  " + formattedFromDate + " - " + formattedToDate + "</p>\n"
                    + "<p>Please review the request at your earliest convenience and provide your approval or any further instructions. </p>\n"
                    + "<p>Thank you for your attention to this matter. </p>\n"
                    + "<p>Best Regards,</p>\n"
                    + "<p>Exon Software Solutions (Pvt) Ltd</p>\n"
                    + "</div>\n"
                    + "";
            textPart.setText(final_Text);
            multipart.addBodyPart(textPart);
            message_1.setContent(final_Text, "text/html");
            message_1.setSubject(" Leave Request Review Required for " + employee.getGeneralUserProfile().getFirstName() + " " + employee.getGeneralUserProfile().getLastName());
            Transport.send(message_1);
            System.out.println("EMAIL SENT -Leave Request Review Required for " + employee.getGeneralUserProfile().getFirstName() + " " + employee.getGeneralUserProfile().getLastName());
        } catch (Exception e) {
            message = "Somthing went wrong!";
        }
//emal END
//        sess.flush();
//        sess.clear();
//        sess.close();
    }

}
