/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.CommonMethod.Commons;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.ProjectEmployees;
import Model.Mapping.ProjectTaskPriority;
import Model.Mapping.ProjectTasks;
import Model.Mapping.Projects;
import Model.Mapping.Status;
import Model.Mapping.TaskAssignEmployees;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
@WebServlet(name = "CreateNewTask", urlPatterns = {"/CreateNewTask"})
public class CreateNewTask extends HttpServlet {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("__________Create New Task______________");

        Commons commonsInstance = new Commons();
        String admin_email = commonsInstance.ADMIN_EMAIL;
        String admin_password = commonsInstance.ADMIN_PASSWORD;

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";
        int status_id = 1;

        try {
            String projectId = request.getParameter("projectId");
            String taskName = request.getParameter("taskName");
            String taskPriorityValue = request.getParameter("taskPriorityValue");
            String taskNote = request.getParameter("taskNote");

            String taskDueDate = request.getParameter("taskDueDate");
            SimpleDateFormat dueDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date duedate = dueDateFormat.parse(taskDueDate);
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String proDueDate = outputDateFormat.format(duedate);

            String taskAssignDate = request.getParameter("taskAssignDate");
            SimpleDateFormat assignDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date assigndate = assignDateFormat.parse(taskAssignDate);
            SimpleDateFormat taDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String proAssignDate = taDateFormat.format(assigndate);

            System.out.println(projectId + "," + taskName + "," + taskPriorityValue + "," + taskNote + "," + proAssignDate + "," + proDueDate);

            String checkedValues = request.getParameter("checkedValues");

            ProjectTasks pt = (ProjectTasks) sess.createQuery("From ProjectTasks Where name='" + taskName + "' and projects_id='" + projectId + "'").setMaxResults(1).uniqueResult();

            Criteria c = sess.createCriteria(Projects.class);
            c.add(Restrictions.eq("id", Integer.parseInt(projectId)));
            Projects p = (Projects) c.uniqueResult();

            Criteria c1 = sess.createCriteria(ProjectTaskPriority.class);
            c1.add(Restrictions.eq("id", Integer.parseInt(taskPriorityValue)));
            ProjectTaskPriority ptp = (ProjectTaskPriority) c1.uniqueResult();

            if (pt == null) {

                pt = new ProjectTasks();
                pt.setName(taskName);
                pt.setNote(taskNote);
                pt.setAssignedDate(convertStringToDate(proAssignDate));
                pt.setDueDate(convertStringToDate(proDueDate));
                pt.setIsActive(Boolean.TRUE);
                pt.setProjectTaskPriority((ProjectTaskPriority) sess.load(ProjectTaskPriority.class, Integer.parseInt(taskPriorityValue)));
                pt.setStatus((Status) sess.load(Status.class, status_id));
                pt.setProjects(p);

                sess.save(pt);

                String[] valuesArray = checkedValues.split(",");
                int[] intArray = new int[valuesArray.length];
                for (int i = 0; i < valuesArray.length; i++) {
                    intArray[i] = Integer.parseInt(valuesArray[i]);
                    System.out.println(intArray[i]);

                    Criteria c4 = sess.createCriteria(ProjectEmployees.class);
                    c4.add(Restrictions.eq("id", intArray[i]));
                    ProjectEmployees pem = (ProjectEmployees) c4.uniqueResult();

                    TaskAssignEmployees tae = new TaskAssignEmployees();

                    tae.setProjectTasks(pt);
                    tae.setProjectEmployees(pem);
                    tae.setIsActive(true);
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
                                + "<p>You have been assigned a new task in the " + p.getName() + " project.</p>\n"
                                + "Task Name: " + taskName + "<br>\n"
                                + "Assigned Date: " + taskAssignDate + "<br>\n"
                                + "Priority: " + ptp.getName() + "<br>\n"
                                + "Due Date: " + taskDueDate + "<br><br>\n"
                                + "For more details to manage your tasks, please log in to your account " + link + ".<br>\n"
                                + "Thank you for your attention to this matter.<br>\n"
                                + "<p>Best Regards,</p>\n"
                                + "<p>Exon Software Solutions (Pvt) Ltd</p>\n"
                                + "</div>\n"
                                + "";
                        textPart.setText(final_Text);
                        multipart.addBodyPart(textPart);
                        message_1.setContent(final_Text, "text/html");
                        message_1.setSubject("Task Assignment: " + taskName + " in " + p.getName());
                        Transport.send(message_1);
                        System.out.println("Task Assignment email sending: " + pem.getEmployee().getGeneralUserProfile().getFirstName());
                    } catch (Exception e) {
                        message = "Somthing went wrong!";
                    }
//emal END

                }
                t.commit();

                status = 200;
                message = "Task Assign Successful !";
            } else {
                status = 400;
                message = "Task name already exists! Task name must be unique.";
            }

        } catch (Exception e) {
            status = 400;
            message = "Task Assign Not Success !";
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
