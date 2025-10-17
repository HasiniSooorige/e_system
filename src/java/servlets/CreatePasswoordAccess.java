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
@WebServlet(name = "CreatePasswoordAccess", urlPatterns = {"/CreatePasswoordAccess"})
public class CreatePasswoordAccess extends HttpServlet {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("---credential access--- ");

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";
        boolean success = false;

        try {

            String creType = request.getParameter("credType");
            String credUsername = request.getParameter("credUsername");
            String credRole = request.getParameter("credRole");
            String credSubtype = request.getParameter("credSubtype");
            String password = Security.encrypt(request.getParameter("password"));
            String credProject = request.getParameter("credProject");
            String credNote = request.getParameter("credNote");
            String credEmp = request.getParameter("em");
            System.out.println(credEmp + "," + creType + "," + credUsername + "," + credRole + "," + credSubtype + "," + password + "," + credProject + "," + credNote);

            UserCredentials credential = new UserCredentials();

            System.out.println("---- ");

            UserCredentialType credentialType = (UserCredentialType) sess.load(UserCredentialType.class, Integer.valueOf(creType));
            UserCredentialCategory credentialCat = (UserCredentialCategory) sess.load(UserCredentialCategory.class, Integer.valueOf(credSubtype));
            Projects credentialProject = (Projects) sess.load(Projects.class, Integer.valueOf(credProject));
            UserLogin gup = (UserLogin) sess.createQuery("From UserLogin Where username='" + credEmp + "'").setMaxResults(1).uniqueResult();
            int empId = gup.getGeneralUserProfile().getId();
            GeneralUserProfile credentialEmployee = (GeneralUserProfile) sess.load(GeneralUserProfile.class, empId);

            credential.setUserCredentialType(credentialType);
            credential.setUserCredentialCategory(credentialCat);
            credential.setUsername(credUsername);
            credential.setPassword(password);
            credential.setNote(credNote);
            credential.setProjects(credentialProject);
            credential.setGeneralUserProfile(credentialEmployee);
            credential.setEnteredDate(convertStringToDate(timeStamp));
            credential.setIsActive(true);
            sess.save(credential);
            System.out.println("--Sve Credential-- ");

            System.out.println("---- ");
            UserCredentialRole credentialrole = new UserCredentialRole();
            CredentialRoles credentialRole = (CredentialRoles) sess.load(CredentialRoles.class, Integer.valueOf(credRole));
            credentialrole.setUserCredentials(credential);
            credentialrole.setCredentialRoles(credentialRole);
            sess.save(credentialrole);
            System.out.println("--Sve Credential role-- ");
            t.commit();

            status = 200;
            message = "Data Successfully Saved";
            success = true;

            objSend.put("status", status);
            objSend.put("messageSuccess", message);
            resp.sendRedirect("main-pages/all-credentials.jsp?messageSuccess=" + "success");

        } catch (Exception e) {
            System.out.println("catch read");
            status = 400;
            message = "Fill All Fields !";

            objSend.put("status", status);
            objSend.put("messageError", message);
            resp.sendRedirect("main-pages/all-credentials.jsp?messageError=" + "error");
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
