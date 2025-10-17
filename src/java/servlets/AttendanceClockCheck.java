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
@WebServlet(name = "AttendanceClockCheck", urlPatterns = {"/AttendanceClockCheck"})
public class AttendanceClockCheck extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status;
        String message = "";
        String message2 = "";

        String nic = request.getParameter("nic");
        String date = request.getParameter("date");
        

        GeneralUserProfile userId = (GeneralUserProfile) sess.createQuery("From GeneralUserProfile Where nic='" + nic + "'").setMaxResults(1).uniqueResult();
        Attendance userExist = (Attendance) sess.createQuery("From Attendance Where general_user_profile_id='" + userId.getId() + "' AND date='" + date + "'").setMaxResults(1).uniqueResult();
        System.out.println(userExist.getIsMarked());

        if (userExist == null) {

            status = 200;
            objSend.put("status", status);
            objSend.put("message", message);
            response.getWriter().print(objSend);

        } else if (userExist.getIsMarked()) {
            status = 201;
            message = userExist.getInTime().toString();
            objSend.put("status", status);
            objSend.put("message", message);
            response.getWriter().print(objSend);

        } else if (!userExist.getIsMarked()) {
            status = 202;
            message = userExist.getInTime().toString();
            message2 = userExist.getOutTime().toString();
            
            objSend.put("status", status);
            objSend.put("message", message);
            objSend.put("message2", message2);
            response.getWriter().print(objSend);
        } else if ("2".equals(userExist.getIsMarked())) {
            status = 203;
            message = userExist.getInTime().toString();
            message2 = userExist.getOutTime().toString();
            
            objSend.put("status", status);
            objSend.put("message", message);
            objSend.put("message2", message2);
            response.getWriter().print(objSend);
        } else {
            System.out.println("Erro1r");
        }
        t.commit();
        sess.close();
    }

}
