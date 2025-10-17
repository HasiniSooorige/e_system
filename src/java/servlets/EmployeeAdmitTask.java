/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.Mapping.ProjectTasks;
import Model.Mapping.Status;
import Model.Mapping.TaskAssignEmployees;
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
@WebServlet(name = "EmployeeAdmitTask", urlPatterns = {"/EmployeeAdmitTask"})
public class EmployeeAdmitTask extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("____Employee Admit Taskt___");

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";

        try {
            String Id = request.getParameter("id");

            TaskAssignEmployees tae = (TaskAssignEmployees) sess.createQuery("From TaskAssignEmployees Where id='" + Id + "'").setMaxResults(1).uniqueResult();
            Integer projectTaskId = tae.getProjectTasks().getId();

            if (tae == null) {
                System.out.println("No Project Task Employee");
            } else {
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
                tae.setStartDate(convertStringToDate(timeStamp));
                
                sess.save(tae);
                
                ProjectTasks pt = (ProjectTasks) sess.createQuery("From ProjectTasks Where id='" + projectTaskId + "'").setMaxResults(1).uniqueResult();

                if (pt.getStartDate() == null) {
                    System.out.println("Task Start Now...");

                    pt.setStartDate(convertStringToDate(timeStamp));
                    pt.setStatus((Status) sess.load(Status.class, 2));

                    sess.update(pt);

                } else {
                    System.out.println("Task Already Started...");
                }

                t.commit();

                status = 200;
                message = "Task admitted successfully!";
                System.out.println("Done");
            }
            sess.flush();
            sess.clear();

        } catch (Exception e) {
            status = 400;
            message = "Task not admitted successfully!";
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
