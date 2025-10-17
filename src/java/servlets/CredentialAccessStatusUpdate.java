/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Com.Tools.Security;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.GeneralUserProfile;
import Model.Mapping.UserCredentialHistory;
import Model.Mapping.UserCredentialIssuingManager;
import Model.Mapping.UserCredentials;
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
 * @author HP
 */
@WebServlet(name = "CredentialAccessStatusUpdate", urlPatterns = {"/CredentialAccessStatusUpdate"})
public class CredentialAccessStatusUpdate extends HttpServlet {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        int status = 200;
        String message = "";
        JSONObject objSend = new JSONObject();

        try {

            String id = request.getParameter("id");
            String activestatus = request.getParameter("status");
            String username = request.getParameter("username");
            String password = Security.encrypt(request.getParameter("password"));
            String note = request.getParameter("reason");
            String emp = request.getParameter("emp");

            Boolean uaction_check;

            if (activestatus.equals("true")) {
                uaction_check = false;
                System.out.println("0");
            } else {
                uaction_check = true;
                System.out.println("1");
            }

            UserCredentialIssuingManager mn = (UserCredentialIssuingManager) sess.createQuery("From UserCredentialIssuingManager Where id='" + id + "'").setMaxResults(1).uniqueResult();
            Integer cred_id = mn.getUserCredentials().getId();

            UserCredentials cred = (UserCredentials) sess.createQuery("From UserCredentials Where id='" + cred_id + "'").setMaxResults(1).uniqueResult();

            if (mn == null) {

                status = 404; // Not Found
                message = "User Credential Access not found";
                System.out.println("User Credential Access not found");

            } else {

                String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

                mn.setIsActive(uaction_check);
                mn.setChangedDate(convertStringToDate(timeStamp));
                sess.update(mn);

                UserCredentialHistory credentialH = new UserCredentialHistory();

                String usernameHistory = cred.getUsername();
                String passwordHistory = cred.getPassword();

                credentialH.setUsername(usernameHistory);
                credentialH.setPassword(passwordHistory);
                credentialH.setUserCredentials(cred);
                credentialH.setUpdatedDate(convertStringToDate(timeStamp));
                credentialH.setGeneralUserProfile((GeneralUserProfile) sess.load(GeneralUserProfile.class, Integer.parseInt(emp)));
                credentialH.setReason(note);

                sess.save(credentialH);

                cred.setUsername(username);
                cred.setPassword(password);

                sess.update(cred);

                if (uaction_check == false) {
                    status = 200;
                    message = "Credential Access Deactivated Successfully.";
                    System.out.println("Credential Access Deactivate");
                    objSend.put("status", status);
                    objSend.put("message", message);
                    response.getWriter().write(objSend.toString());
                } else {
                    status = 300;
                    message = "Credential Access Activated Successfully.";
                    System.out.println("Credential Access Activated");
                    objSend.put("status", status);
                    objSend.put("message", message);
                    response.getWriter().write(objSend.toString());
                }

                t.commit();
            }

            sess.flush();
            sess.clear();

        } catch (Exception e) {
            System.out.println("catch read");
            status = 400;
            message = "Credential Access Updated not Saved. Please try again!";

            objSend.put("status", status);
            objSend.put("message", message);
            response.getWriter().print(objSend);
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
