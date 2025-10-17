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

public class LeaveApprovalHandle_1 extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();

        String gup_id = request.getSession().getAttribute("GUP_ID").toString();
        UserLogin ul = UserLoginDAO.getByID(sess, Integer.parseInt(gup_id));
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";
        if (ul.getUserRole().getId() == 1 || ul.getUserRole().getId() == 2) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String id = request.getParameter("id");

            String approveStatus = request.getParameter("approve");
            String approveBy = request.getParameter("approveby");

            System.out.println(id + approveStatus + approveBy);

            GeneralUserProfile userId = (GeneralUserProfile) sess.createQuery("From GeneralUserProfile Where nic='" + approveBy + "'").setMaxResults(1).uniqueResult();

            Employee empId = (Employee) sess.createQuery("From Employee Where id='" + userId.getId() + "'").setMaxResults(1).uniqueResult();

            LeaveRequest userExist = (LeaveRequest) sess.createQuery("From LeaveRequest Where id='" + id + "'").setMaxResults(1).uniqueResult();

            if (approveStatus.equals("Approved")) {

                userExist.setEmployeeByEmployeeIdApprovedBy(empId);

                userExist.setApprovedDate(new Date());
                Criteria c13 = sess.createCriteria(LeaveApprovalStatus.class);
                c13.add(Restrictions.eq("name", "Approved"));
                LeaveApprovalStatus leaveApp = (LeaveApprovalStatus) c13.uniqueResult();
                userExist.setLeaveApprovalStatus(leaveApp);
                sess.update(userExist);

                status = 201;

                objSend.put("status", status);
                objSend.put("message", message);
                response.getWriter().print(objSend);

            } else if (approveStatus.equals("Decline")) {

                userExist.setEmployeeByEmployeeIdApprovedBy(empId);

                userExist.setApprovedDate(new Date());
                Criteria c13 = sess.createCriteria(LeaveApprovalStatus.class);
                c13.add(Restrictions.eq("name", "Decline"));
                LeaveApprovalStatus leaveApp = (LeaveApprovalStatus) c13.uniqueResult();
                userExist.setLeaveApprovalStatus(leaveApp);
                sess.update(userExist);

                status = 201;

            } else {
                status = 202;
            }
            sess.save(userExist);
            t.commit();
            objSend.put("status", status);
            objSend.put("message", message);
            response.getWriter().print(objSend);

            sess.close();
        }
    }
}
