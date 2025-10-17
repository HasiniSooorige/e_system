/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.Mapping.GupGopManager;
import Model.Mapping.ProjectEmployees;
import java.io.IOException;
import java.io.PrintWriter;
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
@WebServlet(name = "ClientTeamListStatusUpdate", urlPatterns = {"/ClientTeamListStatusUpdate"})
public class ClientTeamListStatusUpdate extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("_____________Client Team List Status Update_______________");
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";
        Boolean status_active;

        try {
            String OrgId = request.getParameter("OrgId");
            String EmpId = request.getParameter("EmpId");
            String ActiveStatus = request.getParameter("ActiveStatus");

            System.out.println(OrgId + " - " + EmpId + " - " + ActiveStatus);

            if ("true".equals(ActiveStatus)) {
                status_active = false;
            } else {
                status_active = true;
            }

            GupGopManager ggm = (GupGopManager) sess.createQuery("From GupGopManager Where general_user_profile_id='" + EmpId + "' and general_organization_profile_id='" + OrgId + "'").setMaxResults(1).uniqueResult();

            System.out.println(ggm.getId());
            ggm.setIsActive(status_active);

            sess.update(ggm);

            t.commit();

            if ("true".equals(ActiveStatus)) {
                status = 200;
                message = "Employee Remove Successful";
            } else {
                status = 200;
                message = "Employee Add Successful";
            }

        } catch (Exception e) {
            status = 400;
            message = "Data Not Saved";
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
