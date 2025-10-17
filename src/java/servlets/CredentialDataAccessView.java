/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.Mapping.UserCredentialIssuingManager;
import Model.Mapping.UserCredentialRole;
import Model.UserCredentialIssuingManagerM;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.annotation.WebServlet;

/**
 *
 * @author jalana
 */
@WebServlet(name = "CredentialDataAccessView", urlPatterns = {"/CredentialDataAccessView"})

public class CredentialDataAccessView extends HttpServlet {

    JSONObject objSend = new JSONObject();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        System.out.println("CREDENTIAL ACCESS VIEW------------");
        System.out.println(id);
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();

        UserCredentialIssuingManager mn = (UserCredentialIssuingManager) sess.createQuery("From UserCredentialIssuingManager Where id='" + id + "'").setMaxResults(1).uniqueResult();

        if (mn == null) {
        } else {

            System.out.println("CREDENTIAL VIEW");

            UserCredentialIssuingManagerM em = new UserCredentialIssuingManagerM();
            em.setId(mn.getId());
            em.setAssigndate(mn.getIssueDate());
            em.setCategory(mn.getUserCredentials().getUserCredentialCategory().getName());
            em.setLastchangeddate(mn.getChangedDate());
            em.setEmployeename(mn.getGeneralUserProfile().getFirstName() + " " + mn.getGeneralUserProfile().getLastName());
            em.setProject(mn.getUserCredentials().getProjects().getName());
            UserCredentialRole eer = (UserCredentialRole) sess.createQuery("From UserCredentialRole Where userCredentials='" + mn.getUserCredentials().getId() + "'").setMaxResults(1).uniqueResult();

            em.setRoll(eer.getCredentialRoles().getName());
            em.setType(mn.getUserCredentials().getUserCredentialType().getName());
            Boolean active = mn.getIsActive();
            if (active == true) {
                em.setStatus("Active");
            } else {
                em.setStatus("Inactive");
            }

            Boolean viewed = mn.getFirstTimeViewed();
            if (viewed == false) {
                em.setViewed("Not Viewed");
            } else {
                em.setViewed("1 st Time Viewed");
            }
            Boolean resign = mn.getIsResigned();
            if (resign == false) {
                em.setAssigned("Current Employee");
            } else {
                em.setAssigned("Resigned Employee");
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValueAsString(em);
            System.out.println(mapper.writeValueAsString(em));
            response.getWriter().print(mapper.writeValueAsString(em));

        }

        try {

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            sess.close();
        }

    }

}
