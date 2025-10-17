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
import Model.Mapping.UserCredentialIssuingManager;
import Model.Mapping.UserCredentials;
import Model.Mapping.UserLogin;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.annotation.WebServlet;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;

@WebServlet(name = "EditUserCredentialAfterInactive", urlPatterns = {"/EditUserCredentialAfterInactive"})

public class EditUserCredentialAfterInactive extends HttpServlet {

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

        System.out.println("Edit credential");

        try {
            String id = req.getParameter("id");
            String username = req.getParameter("username");
            String password = Security.encrypt(req.getParameter("password"));
            String note = req.getParameter("note");
            String emp = req.getParameter("emp");
            System.out.println("id");
            System.out.println(id);

            System.out.println("username");
            System.out.println(username);

            System.out.println("password");
            System.out.println(password);

            System.out.println("note");
            System.out.println(note);
            System.out.println("emp");
            System.out.println(emp);

            UserCredentialHistory credentialH = new UserCredentialHistory();

            System.out.println("---- hitory adding..... ");
            UserCredentialIssuingManager credissu = (UserCredentialIssuingManager) sess.createQuery("From UserCredentialIssuingManager Where id='" + id + "'").setMaxResults(1).uniqueResult();

            UserCredentials cred = (UserCredentials) sess.createQuery("From UserCredentials Where id='" + credissu.getUserCredentials().getId() + "'").setMaxResults(1).uniqueResult();

            if (cred == null) {

            } else {
                String usernameHistory = cred.getUsername();

                String passwordHistory = cred.getPassword();

                UserLogin gup = (UserLogin) sess.createQuery("From UserLogin Where username='" + emp + "'").setMaxResults(1).uniqueResult();
                int empId = gup.getGeneralUserProfile().getId();
                GeneralUserProfile credentialEmployee = (GeneralUserProfile) sess.load(GeneralUserProfile.class, empId);

                credentialH.setUsername(usernameHistory);
                credentialH.setPassword(passwordHistory);
                credentialH.setUserCredentials(cred);
                credentialH.setUpdatedDate(convertStringToDate(timeStamp));
                credentialH.setGeneralUserProfile(credentialEmployee);
                credentialH.setReason(note);

                sess.save(credentialH);
                System.out.println("--Save Credential history-- ");

                System.out.println("---- ");
                System.out.println("--Save Credential updates-- ");
                cred.setUsername(username);
                cred.setPassword(password);

                sess.update(cred);
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
