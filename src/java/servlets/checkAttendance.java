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

@WebServlet(name = "checkAttendance", urlPatterns = {"/checkAttendance"})
public class checkAttendance extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status;
        String message = "";
        String InTime = "";
        String OutTime = "";

        String nic = request.getParameter("nic");
        String date = request.getParameter("date");
        System.out.println("nic "+nic);
        GeneralUserProfile userId = (GeneralUserProfile) sess.createQuery("From GeneralUserProfile Where nic='" + nic + "'").setMaxResults(1).uniqueResult();

        Attendance userExist = (Attendance) sess.createQuery("From Attendance Where general_user_profile_id='" + userId.getId() + "' AND date='" + date + "'").setMaxResults(1).uniqueResult();
        
        
        if (userExist == null) {
           
            status = 200;
            objSend.put("status", status);
            objSend.put("message", message);
            response.getWriter().print(objSend);

        } else if ("1".equals(userExist.getIsMarked())) {
          
            status = 300;
            objSend.put("status", status);
            objSend.put("message", message);
            InTime = userExist.getInTime().toString();
            objSend.put(InTime, InTime);
            objSend.put(userExist.getInTime(), OutTime);
            response.getWriter().print(objSend);

        } else if ("0".equals(userExist.getIsMarked())) {
          
            status = 301;
            objSend.put("status", status);
            objSend.put("message", message);
            objSend.put(userExist.getInTime(), InTime);
            objSend.put(userExist.getInTime(), OutTime);
            response.getWriter().print(objSend);
        } else if ("2".equals(userExist.getIsMarked())) {
          
            status = 302;
            objSend.put("status", status);
            objSend.put("message", message);
            objSend.put(userExist.getInTime(), InTime);
            objSend.put(userExist.getInTime(), OutTime);
            response.getWriter().print(objSend);
        } else {
            System.out.println("Error1");
        }
        t.commit();
        sess.close();
    }
}
