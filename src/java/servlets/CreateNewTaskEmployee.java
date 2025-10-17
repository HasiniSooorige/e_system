/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.CommonMethod.Com;
import Model.CommonMethod.Commons;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.ProjectEmployees;
import Model.Mapping.ProjectTasks;
import Model.Mapping.TaskAssignEmployees;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.json.simple.JSONObject;

/**
 *
 * @author HP
 */
@WebServlet(name = "CreateNewTaskEmployee", urlPatterns = {"/CreateNewTaskEmployee"})
public class CreateNewTaskEmployee extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("_______Create  New  Task  Employee______");

        Commons commonsInstance = new Commons();
        String admin_email = commonsInstance.ADMIN_EMAIL;
        String admin_password = commonsInstance.ADMIN_PASSWORD;

        String today = Com.getDate(new Date());

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";

        try {
            String taskId = request.getParameter("taskId");
            String taskEmpId = request.getParameter("taskEmpId");

            Criteria c4 = sess.createCriteria(ProjectEmployees.class);
            c4.add(Restrictions.eq("id", Integer.parseInt(taskEmpId)));
            ProjectEmployees pem = (ProjectEmployees) c4.uniqueResult();

            Criteria c = sess.createCriteria(ProjectTasks.class);
            c.add(Restrictions.eq("id", Integer.parseInt(taskId)));
            ProjectTasks pt = (ProjectTasks) c.uniqueResult();

            System.out.println("taskId - " + taskId + " , taskEmpId - " + taskEmpId);

            TaskAssignEmployees tae = (TaskAssignEmployees) sess.createQuery("From TaskAssignEmployees Where project_tasks_id='" + taskId + "' and project_employees_id='" + taskEmpId + "'").setMaxResults(1).uniqueResult();

            if (tae == null) {

                tae = new TaskAssignEmployees();

                tae.setProjectTasks((ProjectTasks) sess.load(ProjectTasks.class, Integer.parseInt(taskId)));
                tae.setProjectEmployees((ProjectEmployees) sess.load(ProjectEmployees.class, Integer.parseInt(taskEmpId)));
                tae.setIsActive(Boolean.TRUE);

                sess.save(tae);

                //                            email START
                Properties props = new Properties();
                props.put("mail.smtp.auth", true);
                props.put("mail.smtp.starttls.enable", true);
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");
                javax.mail.Session session = javax.mail.Session.getInstance(props,
                        new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(admin_email, admin_password);
                    }
                });
                try {
                    Message message_1 = new MimeMessage(session);
                    message_1.setFrom(new InternetAddress("Exon"));
                    message_1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(pem.getEmployee().getGeneralUserProfile().getEmail()));
                    MimeBodyPart textPart = new MimeBodyPart();
                    Multipart multipart = new MimeMultipart();
                    String link = "<a href=\"http://system.exon.lk\" style=\"color:#6666CC;\" target=\"_blank\"> <strong><em>Exon Management System</em></strong></a>";

                    String final_Text = "<div style=\"text-align:justify; \">\n"
                            + "        Dear " + pem.getEmployee().getGeneralUserProfile().getFirstName() + " " + pem.getEmployee().getGeneralUserProfile().getLastName() + ",<br>"
                            + "<p>You have been assigned a new task in the " + pem.getProjects().getName() + " project.</p>\n"
                            + "Task Name: " + pt.getName() + "<br>\n"
                            + "Assigned Date: " + today + "<br>\n"
                            + "Priority: " + pt.getProjectTaskPriority().getName() + "<br>\n"
                            + "Due Date: " + pt.getDueDate() + "<br><br>\n"
                            + "For more details to manage your tasks, please log in to your account " + link + ".<br>\n"
                            + "Thank you for your attention to this matter.<br>\n"
                            + "<p>Best Regards,</p>\n"
                            + "<p>Exon Software Solutions (Pvt) Ltd</p>\n"
                            + "</div>\n"
                            + "";
                    textPart.setText(final_Text);
                    multipart.addBodyPart(textPart);
                    message_1.setContent(final_Text, "text/html");
                    message_1.setSubject("Task Assignment: " + pt.getName() + " in " + pem.getProjects().getName());
                    Transport.send(message_1);
                    System.out.println("Task Assignment email sending: " + pem.getEmployee().getGeneralUserProfile().getFirstName());
                } catch (Exception e) {
                    message = "Somthing went wrong!";
                }
//emal END

                t.commit();

                status = 200;
                message = "Employee Succesfully Added";

            } else {
                status = 400;
                message = "Task Employee already registed !";
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

}
