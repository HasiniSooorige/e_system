/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.Mapping.ProjectTaskPriority;
import Model.Mapping.ProjectTasks;
import Model.Mapping.Status;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
@WebServlet(name = "EditTaskDetails", urlPatterns = {"/EditTaskDetails"})
public class EditTaskDetails extends HttpServlet {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("_____Edit Task Details_____");

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";

        try {
            String taskId = request.getParameter("taskId");
            Integer taskIdInt = Integer.parseInt(taskId);
            String taskNote = request.getParameter("taskNote");
            String taskPriority = request.getParameter("taskPriority");
//            String taskStatus = request.getParameter("taskStatus");
            String StartDate = request.getParameter("StartDate");
            String CompleteDate = request.getParameter("CompleteDate");
            String EndDate = request.getParameter("EndDate");

            System.out.println(taskId + " - " + taskNote + " - " + taskPriority
                    + " - " + " - " + StartDate + " - " + CompleteDate);

            ProjectTasks pt = (ProjectTasks) sess.createQuery("From ProjectTasks Where id='" + taskIdInt + "'").setMaxResults(1).uniqueResult();

            if (pt != null) {

                pt.setNote(taskNote);
                pt.setProjectTaskPriority((ProjectTaskPriority) sess.load(ProjectTaskPriority.class, Integer.parseInt(taskPriority)));

                if (!"".equals(StartDate)) {
                    pt.setStartDate(convertStringToDate(StartDate));
                    pt.setStatus((Status) sess.load(Status.class, 2));
                } else {
                    pt.setStatus((Status) sess.load(Status.class, pt.getStatus().getId()));
                }

                if (!"".equals(EndDate)) {
                    pt.setEndDate(convertStringToDate(EndDate));
                    pt.setStatus((Status) sess.load(Status.class, 4));
                } else {
                    pt.setStatus((Status) sess.load(Status.class, pt.getStatus().getId()));
                }

                if (!"".equals(CompleteDate)) {
                    pt.setCompletedDate(convertStringToDate(CompleteDate));
                    pt.setStatus((Status) sess.load(Status.class, 3));
                } else {
                    pt.setStatus((Status) sess.load(Status.class, pt.getStatus().getId()));
                }
                sess.update(pt);
                t.commit();

                status = 200;
                message = "Task Update Successfully!";
                System.out.println("Done");

            } else {

                status = 400;
                message = "Task Not Found!!!";
                System.out.println("Task Not Found!!!");

            }

            sess.flush();
            sess.clear();

        } catch (Exception e) {
            status = 500;
            message = "Error Occurred";
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
