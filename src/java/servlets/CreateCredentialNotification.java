/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.Mapping.NotificationManager;
import Model.Mapping.NotificationType;
import Model.Mapping.UserCredentialIssuingManager;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
@WebServlet(name = "CreateCredentialNotification", urlPatterns = {"/CreateCredentialNotification"})

public class CreateCredentialNotification extends HttpServlet {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("---credential notification- ");

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";
        boolean success = false;

        try {

            String id = request.getParameter("id");

            System.out.println(id);

            NotificationManager notification = new NotificationManager();

            System.out.println("---- ");

            UserCredentialIssuingManager credential = (UserCredentialIssuingManager) sess.load(UserCredentialIssuingManager.class, Integer.valueOf(id));
            NotificationType type = (NotificationType) sess.load(NotificationType.class, 1);

            notification.setDescription(credential.getGeneralUserProfile().getFirstName() + " " + credential.getGeneralUserProfile().getLastName() + " Resigned from ID (Credential Issuing ID) - " + credential.getId() + ". Project name -" + credential.getUserCredentials().getProjects().getName());
            notification.setGeneralUserProfile(credential.getUserCredentials().getGeneralUserProfile());
            notification.setIsViewed(false);
            notification.setNotificationType(type);
            notification.setNotifyDate(convertStringToDate(timeStamp));
            notification.setReference(Integer.valueOf(id));

            sess.save(notification);
            System.out.println("--Sve Credential notification-- ");

            t.commit();

            status = 200;
            message = "Data Successfully Saved";
            success = true;

            objSend.put("status", status);
            objSend.put("messageSuccess", message);
            resp.getWriter().print(objSend);
        } catch (Exception e) {
            System.out.println("catch read");
            status = 400;
            message = "Fill All Fields !";

            objSend.put("status", status);
            objSend.put("messageError", message);
            resp.getWriter().print(objSend);
            e.printStackTrace();
        } finally {
            sess.close();

        }

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
