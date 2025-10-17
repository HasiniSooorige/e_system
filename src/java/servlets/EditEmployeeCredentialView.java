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
import Model.Mapping.UserCredentialIssuingManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.annotation.WebServlet;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;

@WebServlet(name = "EditEmployeeCredentialView", urlPatterns = {"/EditEmployeeCredentialView"})

public class EditEmployeeCredentialView extends HttpServlet {

    int status = 200;
    String message = "";
    JSONObject objSend = new JSONObject();

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, NullPointerException {
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";

        try {
            String id = req.getParameter("id");

            UserCredentialIssuingManager mn = (UserCredentialIssuingManager) sess.createQuery("From UserCredentialIssuingManager Where id='" + id + "'").setMaxResults(1).uniqueResult();

            if (mn == null) {

            } else {
                
                mn.setFirstTimeViewed(true);
                sess.update(mn);
            }

            t.commit();

            status = 200;
            message = "Data Successfully Saved";

            objSend.put("status", status);
            objSend.put("messageSuccess", message);

        } catch (Exception e) {
            System.out.println("catch read");
            status = 400;
            message = "Fill All Fields !";

            objSend.put("status", status);
            objSend.put("messageError", message);
            e.printStackTrace();
        } finally {
            sess.close();

        }

    }

}
