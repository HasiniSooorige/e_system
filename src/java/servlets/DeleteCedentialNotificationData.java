/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Connection.NewHibernateUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import Model.Mapping.NotificationManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.annotation.WebServlet;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;

@WebServlet(name = "DeleteCedentialNotificationData", urlPatterns = {"/DeleteCedentialNotificationData"})

public class DeleteCedentialNotificationData extends HttpServlet {

    int status = 200;
    String message = "";
    JSONObject objSend = new JSONObject();

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
//    SessionFactory sessionFactory;
//            Session s;
//            Employee employee;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, NullPointerException {
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        Session sess2 = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t2 = sess2.beginTransaction();

        System.out.println("DELETE CRED NOTIFICATION");

        try {
            String id = req.getParameter("id");

            System.out.println("id");
            System.out.println(id);

            NotificationManager mn = (NotificationManager) sess.createQuery("From NotificationManager Where id='" + id + "'").setMaxResults(1).uniqueResult();

            if (mn == null) {

            } else {
                System.out.println("---- ");

                sess.delete(mn);
                System.out.println("--NOTIFICATION DELETE DONE-- ");
            }

            t.commit();

            status = 200;
            message = "Data Successfully Saved";

            objSend.put("status", status);
            objSend.put("messageSuccess", message);
            resp.getWriter().print(objSend);
        } catch (Exception e) {
            System.out.println("catch read");
            status = 400;
            message = "Fill All Fields !";

            objSend.put("error", status);
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
