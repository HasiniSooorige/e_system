/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.Logic.UserLoginDAO;
import Model.Mapping.Attendance;
import Model.Mapping.Employee;
import Model.Mapping.GeneralUserProfile;
import Model.Mapping.LeaveApprovalStatus;
import Model.Mapping.LeaveRequest;
import Model.Mapping.LeaveType;
import Model.Mapping.UserLogin;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;
import org.json.simple.JSONObject;

@WebServlet(name = "LeaveApprovalHandle", urlPatterns = {"/LeaveApprovalHandle"})

public class LeaveApprovalHandle extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getSession().getAttribute("User_ID") != null) {
            System.out.println("yes");
        }else{
            System.out.println("no");
        
        }
        int login = Integer.parseInt(request.getSession().getAttribute("User_ID").toString());
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";

        LocalDate currentDate = LocalDate.now();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String dateString = currentDate.toString();

        Date parDate = null;

        try {
            parDate = dateFormat.parse(dateString);
        } catch (ParseException ex) {

        }

        String id = request.getParameter("id");
        String approveStatus = request.getParameter("approve");
        UserLogin user = UserLoginDAO.getByID(sess, login);
        Employee empId = (Employee) sess.createQuery("From Employee Where generalUserProfile.id='" + user.getGeneralUserProfile().getId() + "'").setMaxResults(1).uniqueResult();

        LeaveRequest userExist = (LeaveRequest) sess.createQuery("From LeaveRequest Where id='" + id + "'").setMaxResults(1).uniqueResult();

        if (approveStatus.equals("Approved")) {

            userExist.setEmployeeByEmployeeIdApprovedBy(empId);

            userExist.setApprovedDate(parDate);
            Criteria c13 = sess.createCriteria(LeaveApprovalStatus.class);
            c13.add(Restrictions.eq("name", "Approved"));
            LeaveApprovalStatus leaveApp = (LeaveApprovalStatus) c13.uniqueResult();
            userExist.setLeaveApprovalStatus(leaveApp);
            sess.update(userExist);

            sess.save(userExist);
            t.commit();
            status = 201;

            objSend.put("status", status);
            objSend.put("message", message);
            response.getWriter().print(objSend);

        } else if (approveStatus.equals("Decline")) {

            userExist.setEmployeeByEmployeeIdApprovedBy(empId);

            userExist.setApprovedDate(parDate);
            Criteria c13 = sess.createCriteria(LeaveApprovalStatus.class);
            c13.add(Restrictions.eq("name", "Decline"));
            LeaveApprovalStatus leaveApp = (LeaveApprovalStatus) c13.uniqueResult();
            userExist.setLeaveApprovalStatus(leaveApp);
            sess.update(userExist);

            sess.save(userExist);
            t.commit();
            status = 201;

            objSend.put("status", status);
            objSend.put("message", message);
            response.getWriter().print(objSend);
        } else {
            status = 202;

            objSend.put("status", status);
            objSend.put("message", message);
            response.getWriter().print(objSend);
        }

        sess.close();
    }

}
