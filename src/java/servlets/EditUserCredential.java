/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Com.Tools.Security;
import Model.Connection.NewHibernateUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import Model.Mapping.GeneralUserProfile;
import Model.Mapping.UserCredentialHistory;
import Model.Mapping.UserCredentials;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.annotation.WebServlet;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;

@WebServlet(name = "EditUserCredential", urlPatterns = {"/EditUserCredential"})

public class EditUserCredential extends HttpServlet {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, NullPointerException {

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        int status = 200;
        String message = "";
        JSONObject objSend = new JSONObject();
        System.out.println("Edit credential");

        try {
            String id = req.getParameter("id");
            String username = req.getParameter("username");
            String password = Security.encrypt(req.getParameter("password"));
            String note = req.getParameter("note");
            String emp = req.getParameter("emp");

            UserCredentials cred = (UserCredentials) sess.createQuery("From UserCredentials Where id='" + id + "'").setMaxResults(1).uniqueResult();
            String UsernameHistory = cred.getUsername();
            String passwordHistory = cred.getPassword();

            UserCredentialHistory credentialH = new UserCredentialHistory();

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

            credentialH.setUsername(UsernameHistory);
            credentialH.setPassword(passwordHistory);
            credentialH.setUserCredentials((UserCredentials) sess.load(UserCredentials.class, Integer.parseInt(id)));
            credentialH.setUpdatedDate(convertStringToDate(timeStamp));
            credentialH.setGeneralUserProfile((GeneralUserProfile) sess.load(GeneralUserProfile.class, Integer.parseInt(emp)));
            credentialH.setReason(note);

            sess.save(credentialH);

            cred.setUsername(username);
            cred.setPassword(password);
            sess.update(cred);

            t.commit();

            status = 200;
            message = "Data Successfully Saved.";

            sess.flush();
            sess.clear();
        } catch (Exception e) {
            System.out.println("catch read");
            status = 400;
            message = "Fill All Fields !";
            e.printStackTrace();
        } finally {
            sess.close();
        }

        objSend.put("status", status);
        objSend.put("message", message);
        resp.getWriter().print(objSend);
        System.out.println(objSend);

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
