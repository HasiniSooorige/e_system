/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.Mapping.CommentsManager;
import Model.Mapping.GeneralUserProfile;
import Model.Mapping.ProjectEmployees;
import Model.Mapping.ProjectTasks;
import Model.Mapping.Projects;
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
 * @author HP
 */
@WebServlet(name = "CreateNewTaskComment", urlPatterns = {"/CreateNewTaskComment"})
public class CreateNewTaskComment extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("____Create New Task Comment___");

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";

        try {
            String comment = request.getParameter("comment");
            String userId = request.getParameter("userId");
            Integer userLoginId = Integer.parseInt(userId);
            String commentTaskId = request.getParameter("commentTaskId");
            String commentProjectId = request.getParameter("commentProjectId");
            String mentionEmpId = request.getParameter("mentionEmpId");

            System.out.println(comment + " - " + userId + " - " + commentTaskId + " - " + commentProjectId);

            UserLogin ul = (UserLogin) sess.createQuery("From UserLogin Where id='" + userLoginId + "'").setMaxResults(1).uniqueResult();
            Integer gupId = ul.getGeneralUserProfile().getId();

            CommentsManager cm = new CommentsManager();

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

            cm.setComment(comment);
            cm.setAddedDate(convertStringToDate(timeStamp));
            cm.setGeneralUserProfile((GeneralUserProfile) sess.load(GeneralUserProfile.class, gupId));
            cm.setProjects((Projects) sess.load(Projects.class, Integer.parseInt(commentProjectId)));
            cm.setProjectTasks((ProjectTasks) sess.load(ProjectTasks.class, Integer.parseInt(commentTaskId)));
            cm.setProjectEmployees((ProjectEmployees) sess.load(ProjectEmployees.class, Integer.parseInt(mentionEmpId)));

            sess.save(cm);
            t.commit();

            status = 200;
            message = "Task Comment Save Successfully!";
            System.out.println("Done");

            sess.flush();
            sess.clear();

        } catch (Exception e) {
            status = 400;
            message = "Task Comment Not Saved!";
            e.printStackTrace();
        } finally {
            sess.close();
        }
        objSend.put("status", status);
        objSend.put("message", message);
        response.getWriter().print(objSend);
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
