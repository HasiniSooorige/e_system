/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.Mapping.Attendance;
import Model.Mapping.GeneralUserProfile;
import java.io.IOException;
import java.sql.Time;
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
 * @author Suren Fernando
 */
@WebServlet(name = "AttendanceMark", urlPatterns = {"/AttendanceMark"})
public class AttendanceMark extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";

        // Create a SimpleDateFormat object to format the date and time
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss"); // Adjust the time format as needed
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust the date format as needed

        // Get the current date and time
        Date currentTime = new Date();

        // Format the current date and time as a string
        String formattedTime = timeFormat.format(currentTime);

        String nic = request.getParameter("nic");
        String date = request.getParameter("date");

        Date parsedTime;
        Date parsedDate;
        try {
            parsedDate = dateFormat.parse(date);
            parsedTime = timeFormat.parse(formattedTime);

        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        Time timeObject = new Time(parsedTime.getTime());

        GeneralUserProfile userId = (GeneralUserProfile) sess.createQuery("From GeneralUserProfile Where nic='" + nic + "'").setMaxResults(1).uniqueResult();

       
         Attendance userExist = (Attendance) sess.createQuery("From Attendance Where general_user_profile_id='" + userId.getId() + "' AND date='" + date + "'").setMaxResults(1).uniqueResult();

        if (userExist == null) {
            Attendance atend = new Attendance();
            atend.setGeneralUserProfile(userId);
            atend.setDate(parsedDate);
            atend.setInTime(timeObject);
            atend.setIsMarked(true);
            sess.save(atend);
            t.commit();
            status = 200;

            objSend.put("status", status);
            objSend.put("message", message);
            response.getWriter().print(objSend);
        }

        if (userExist != null) {

            if (userExist.getIsMarked()) {
                userExist.setOutTime(timeObject);
                userExist.setIsMarked(false);
                sess.update(userExist);
                t.commit();

                status = 300;
                objSend.put("status", status);
                objSend.put("message", message);
                response.getWriter().print(objSend);
            } else if (!userExist.getIsMarked()) {
                userExist.setIsMarked(false);
                sess.update(userExist);
                t.commit();
                
                status = 301;
                objSend.put("status", status);
                objSend.put("message", message);
                response.getWriter().print(objSend);
//                response.sendRedirect("projects/projectList.jsp");
            }else{
                System.out.println("error in marking");
            }
        }
        sess.close();
    }

}
