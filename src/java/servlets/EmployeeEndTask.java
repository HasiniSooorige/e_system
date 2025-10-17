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
import static Servlets.EmployeeAdmitTask.convertStringToDate;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
@WebServlet(name = "EmployeeEndTask", urlPatterns = {"/EmployeeEndTask"})
public class EmployeeEndTask extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("____Employee End Taskt___");

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";

        // Initialize counters
        int endDateNull = 0;
        int endDateNotNull = 0;


        try {
            String Id = request.getParameter("id");
            String taskAssignProjectId = request.getParameter("taskAssignProjectId");
            String taskAssignTaskId = request.getParameter("taskAssignTaskId");

            TaskAssignEmployees tae = (TaskAssignEmployees) sess.createQuery("From TaskAssignEmployees Where id='" + Id + "'").setMaxResults(1).uniqueResult();

            if (tae == null) {
                System.out.println("No Project Task Employee");
            } else {
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
                tae.setEndDate(convertStringToDate(timeStamp));
                sess.save(tae);

                String hql = "FROM TaskAssignEmployees WHERE project_tasks_id = :projectTasksId AND is_active = 1";
                List<TaskAssignEmployees> assignedEmployees = sess.createQuery(hql)
                        .setParameter("projectTasksId", taskAssignTaskId)
                        .list();

                // Iterate through the list and count based on task_end_date
                for (TaskAssignEmployees taemp : assignedEmployees) {
                    if (taemp.getEndDate() == null) {
                        endDateNull++;
                    } else {
                        endDateNotNull++;
                    }
                }

                // Print the results
                System.out.println("Count of employees with task_end_date as null: " + endDateNull);
                System.out.println("Count of employees with task_end_date not null: " + endDateNotNull);

                if (endDateNull == 0) {
                    ProjectTasks pt = (ProjectTasks) sess.createQuery("From ProjectTasks Where id='" + taskAssignTaskId + "'").setMaxResults(1).uniqueResult();

                    System.out.println("Task End All Emp Now...");

                    pt.setEndDate(convertStringToDate(timeStamp));
                    pt.setStatus((Status) sess.load(Status.class, 4));

                    sess.update(pt);

                }

                t.commit();

                status = 200;
                message = "Task End successfully!";
                System.out.println("Done");
            }
            sess.flush();
            sess.clear();
        } catch (Exception e) {
            status = 400;
            message = "Task not End successfully!";
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
