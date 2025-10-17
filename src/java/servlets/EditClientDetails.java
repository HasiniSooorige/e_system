/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.Mapping.GeneralOrganizationProfile;
import Model.Mapping.GeneralUserProfile;
import Model.Mapping.GupGopManager;
import Model.Mapping.ProjectEmployees;
import Model.Mapping.UserLogin;
import Model.Mapping.UserRole;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
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
@WebServlet(name = "EditClientDetails", urlPatterns = {"/EditClientDetails"})
public class EditClientDetails extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("________________Edit  Client  Details_________________");

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";
        int userId = 0;

        try {
            String editClientId = request.getParameter("editClientId");
            String editCliAdd1 = request.getParameter("editCliAdd1");
            String editCliAdd2 = request.getParameter("editCliAdd2");
            String editCliAdd3 = request.getParameter("editCliAdd3");
            String editCliEmail = request.getParameter("editCliEmail");
            String editCliContact = request.getParameter("editCliContact");
            String editCliFaxNo = request.getParameter("editCliFaxNo");

            System.out.println(editClientId + " - " + editCliAdd1 + " - " + editCliAdd2
                    + " - " + editCliAdd3 + " - " + editCliEmail + " - " + editCliContact + " - " + editCliFaxNo);

            GeneralOrganizationProfile gop = (GeneralOrganizationProfile) sess.createQuery("From GeneralOrganizationProfile Where id='" + editClientId + "'").setMaxResults(1).uniqueResult();
            
             List<GupGopManager> list = sess.createQuery("From GupGopManager Where general_organization_profile_id='" + editClientId + "'").list();
             
            if (gop != null) {

                gop.setAddress1(editCliAdd1);
                gop.setAddress2(editCliAdd2);
                gop.setAddress3(editCliAdd3);
                gop.setEmail(editCliEmail);
                gop.setContactNo(editCliContact);
                gop.setFaxNo(editCliFaxNo);

                sess.update(gop);
                
                 if (!list.isEmpty()) {
                     
                     System.out.println("edit-client-emp-list-size  :  " + list.size());
                     
                     for (GupGopManager per : list) {
                         System.out.println(per.getGeneralUserProfile().getId());
                         
                         userId = per.getGeneralUserProfile().getId();
                         
                         GeneralUserProfile gup = (GeneralUserProfile) sess.createQuery("From GeneralUserProfile Where id='" + userId + "'").setMaxResults(1).uniqueResult();
                         
                         gup.setAddress1(editCliAdd1);
                         gup.setAddress2(editCliAdd2);
                         gup.setAddress3(editCliAdd3);
                         
                         sess.update(gup);
                     }
                 }

                status = 200;
                message = "Updated Successfully!";
                System.out.println("Done");
                
            } else {

                status = 400;
                message = "Client Not Found!!!";
                System.out.println("Client Not Found!!!");

            }

            t.commit();

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
