package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.Logic.UserLoginDAO;
import Model.Mapping.ClaimApproval;
import Model.Mapping.ClaimApprovalType;
import Model.Mapping.ClaimStatus;
import Model.Mapping.Claims;
import Model.Mapping.Employee;
import Model.Mapping.UserLogin;
import java.io.IOException;
import java.io.PrintWriter;
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
@WebServlet(name = "FinanceStatus", urlPatterns = {"/FinanceStatus"})

public class FinanceStatus extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (request.getSession().getAttribute("GUP_ID") != null) {
            int id = Integer.parseInt(request.getParameter("id"));
            System.out.println("id " + id);
            String val = request.getParameter("val");
            String approveAdmin = request.getParameter("approveAdmin");

            int claimId = Integer.parseInt(request.getParameter("claimId"));

            Session sess = NewHibernateUtil.getSessionFactory().openSession();
            Transaction t = sess.beginTransaction();
            JSONObject objSend = new JSONObject();
            int status = 0;
            String message = "";

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = dateFormat.format(new Date());

            String gup_id = request.getSession().getAttribute("GUP_ID").toString();
            UserLogin ul = UserLoginDAO.getByID(sess, Integer.parseInt(gup_id));
            if (ul.getUserRole().getId() == 1 || ul.getUserRole().getId() == 2) {
                if (val == null) {
                    // Employee not found, handle the error
                    status = 404; // Not Found
                    message = "Claim not found";
                    System.out.println("Claim not found");

                } else {

                    ClaimApproval claimApprove = new ClaimApproval();
                    Claims claim = (Claims) sess.createQuery("From Claims Where id ='" + claimId + "'").setMaxResults(1).uniqueResult();
                    Employee emp = (Employee) sess.createQuery("From Employee Where id='" + id + "'").setMaxResults(1).uniqueResult();
                    claimApprove.setApprovedDate(new Date());
                    claimApprove.setClaims(claim);
                    claimApprove.setEmployee(emp);
                    if ("Human".equals(approveAdmin)) {

                        if ("Approve".equals(val)) {

                            ClaimApprovalType sts = (ClaimApprovalType) sess.createQuery("From ClaimApprovalType Where id ='1'").setMaxResults(1).uniqueResult();
                            claimApprove.setClaimApprovalType(sts);
                            claim.setStatus(true);

                            ClaimStatus clsts = (ClaimStatus) sess.createQuery("From ClaimStatus Where id= '2'").setMaxResults(1).uniqueResult();
                            claim.setClaimStatus(clsts);

                            sess.save(claimApprove);
                            status = 200;
                            System.out.println("s " + status);

                        } else if ("Dissapprove".equals(val)) {
                            ClaimApprovalType sts = (ClaimApprovalType) sess.createQuery("From ClaimApprovalType Where id ='2'").setMaxResults(1).uniqueResult();
                            claimApprove.setClaimApprovalType(sts);

                            Claims stsSet = (Claims) sess.createQuery("From Claims Where id='" + claimId + "'").setMaxResults(1).uniqueResult();

                            stsSet.setStatus(false);

                            ClaimStatus clsts = (ClaimStatus) sess.createQuery("From ClaimStatus Where id= '2'").setMaxResults(1).uniqueResult();
                            claim.setClaimStatus(clsts);

                            sess.save(claimApprove);
                            status = 200;
                        } else {
                            status = 204;
                        }

                    } else if ("Finance".equals(approveAdmin)) {

                        if ("Approve".equals(val)) {

                            ClaimApprovalType sts = (ClaimApprovalType) sess.createQuery("From ClaimApprovalType Where id ='1'").setMaxResults(1).uniqueResult();
                            claimApprove.setClaimApprovalType(sts);

                            Claims stsSet = (Claims) sess.createQuery("From Claims Where id='" + claimId + "'").setMaxResults(1).uniqueResult();

                            stsSet.setStatus(true);

                            ClaimStatus clsts = (ClaimStatus) sess.createQuery("From ClaimStatus Where id= '3'").setMaxResults(1).uniqueResult();
                            claim.setClaimStatus(clsts);

                            sess.save(claimApprove);
                            status = 200;

                        } else if ("Dissapprove".equals(val)) {
                            ClaimApprovalType sts = (ClaimApprovalType) sess.createQuery("From ClaimApprovalType Where id ='2'").setMaxResults(1).uniqueResult();
                            claimApprove.setClaimApprovalType(sts);

                            Claims stsSet = (Claims) sess.createQuery("From Claims Where id='" + claimId + "'").setMaxResults(1).uniqueResult();

                            stsSet.setStatus(false);

                            ClaimStatus clsts = (ClaimStatus) sess.createQuery("From ClaimStatus Where id= '3'").setMaxResults(1).uniqueResult();
                            claim.setClaimStatus(clsts);

                            sess.save(claimApprove);
                            status = 200;
                        } else {

                            status = 204;
                        }
                    }
                    t.commit();
                    sess.flush();
                    sess.close();
                    objSend.put("status", status);
                    objSend.put("message", message);
                    response.setContentType("text/json;charset=UTF-8");
                    PrintWriter out = response.getWriter();
                    try {
                        out.print(objSend);
                    } finally {
                        out.close();
                    }

                }
            }

        } else {
            response.sendRedirect("../index.jsp");
        }
    }
}
