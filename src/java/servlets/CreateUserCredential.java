/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Com.Tools.Security;
import Model.Mapping.GeneralUserProfile;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.CredentialRoles;
import Model.Mapping.Projects;
import Model.Mapping.UserCredentialCategory;
import Model.Mapping.UserCredentialRole;
import Model.Mapping.UserCredentialType;
import Model.Mapping.UserCredentials;
import Model.Mapping.UserLogin;
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
@WebServlet(name = "CreateUserCredential", urlPatterns = {"/CreateUserCredential"})

public class CreateUserCredential extends HttpServlet {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("---credential- ");

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";
        boolean success = false;

        try {

            String credType = request.getParameter("credType");
            String credCategory = request.getParameter("credCategory");
            String credRole = request.getParameter("credRole");
            String credProject = request.getParameter("credProject");
            String credUsername = request.getParameter("credUsername");
            String credPassword = Security.encrypt(request.getParameter("credPassword"));
            String credNote = request.getParameter("credNote");
            String userGUPID = request.getParameter("userGUPID");

            System.out.println(credType + "," + credCategory + "," + credProject + "," + credRole + "," + credUsername + "," + credPassword + "," + credNote + "," + userGUPID);

            UserCredentials credential = new UserCredentials();

            credential.setUsername(credUsername);
            credential.setPassword(credPassword);
            credential.setUserCredentialType((UserCredentialType) sess.load(UserCredentialType.class, Integer.valueOf(credType)));
            credential.setUserCredentialCategory((UserCredentialCategory) sess.load(UserCredentialCategory.class, Integer.valueOf(credCategory)));
            credential.setEnteredDate(convertStringToDate(timeStamp));
            credential.setNote(credNote);
            credential.setProjects((Projects) sess.load(Projects.class, Integer.valueOf(credProject)));
            credential.setGeneralUserProfile((GeneralUserProfile) sess.load(GeneralUserProfile.class, Integer.parseInt(userGUPID)));
            credential.setIsActive(true);

            sess.save(credential);
            System.out.println("--Save user_credentials-- ");

            UserCredentialRole credentialrole = new UserCredentialRole();

            credentialrole.setUserCredentials(credential);
            credentialrole.setCredentialRoles((CredentialRoles) sess.load(CredentialRoles.class, Integer.valueOf(credRole)));

            sess.save(credentialrole);

            System.out.println("--Save Credential role-- ");

            t.commit();

            status = 200;
            message = "User Credentials Successfully Saved";

            sess.flush();
            sess.clear();
        } catch (Exception e) {
            System.out.println("catch read");
            status = 400;
            message = "User Credentials Not Saved";

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
