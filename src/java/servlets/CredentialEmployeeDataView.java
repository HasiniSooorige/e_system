/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Com.Tools.Security;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.UserCredentialIssuingManager;
import Model.Mapping.UserCredentialRole;
import Model.Mapping.UserCredentialViewRequest;
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
@WebServlet(name = "CredentialEmployeeDataView", urlPatterns = {"/CredentialEmployeeDataView"})

public class CredentialEmployeeDataView extends HttpServlet {

    JSONObject objSend = new JSONObject();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        System.out.println("CREDENTIAL EMPLOYEE DATA VIEW------------");
        System.out.println(id);
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();

        try {
            UserCredentialIssuingManager mn = (UserCredentialIssuingManager) sess.createQuery("From UserCredentialIssuingManager Where id='" + id + "'").setMaxResults(1).uniqueResult();

            UserCredentialIssuingManagerM em = new UserCredentialIssuingManagerM();

            if (mn.getFirstTimeViewed() == false && mn.getIsChanged() == false) {

                System.out.println("First View...");

                em.setType(mn.getUserCredentials().getUserCredentialType().getName());
                UserCredentialRole eer = (UserCredentialRole) sess.createQuery("From UserCredentialRole Where userCredentials='" + mn.getUserCredentials().getId() + "'").setMaxResults(1).uniqueResult();

                em.setRoll(eer.getCredentialRoles().getName());
                em.setAssigndate(mn.getIssueDate());
                em.setCategory(mn.getUserCredentials().getUserCredentialCategory().getName());
                em.setProject(mn.getUserCredentials().getProjects().getName());
                em.setUsername(mn.getUserCredentials().getUsername());
                em.setPassword(Security.decrypt(mn.getUserCredentials().getPassword()));
                em.setNote(mn.getUserCredentials().getNote());

                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValueAsString(em);
                System.out.println(mapper.writeValueAsString(em));
                response.getWriter().print(mapper.writeValueAsString(em));

            } else if (mn.getFirstTimeViewed() == true && mn.getIsChanged() == true) {

                System.out.println("Viewed... & Requested...");

                UserCredentialViewRequest ucvr = (UserCredentialViewRequest) sess.createQuery("From UserCredentialViewRequest Where user_credential_issuing_manager_id='" + id + "'").setMaxResults(1).uniqueResult();

                Integer viewStatus = null;

                if (ucvr.getIsActive() == true) {
                    viewStatus = 1;
                } else if (ucvr.getIsActive() == false) {
                    viewStatus = 0;
                }
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValueAsString(viewStatus);
                System.out.println(mapper.writeValueAsString(viewStatus));
                response.getWriter().print(mapper.writeValueAsString(viewStatus));

            } else if (mn.getFirstTimeViewed() == true && mn.getIsChanged() == false) {

                System.out.println("Viewed... & Not requested...");

                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValueAsString(null);
                System.out.println(mapper.writeValueAsString(null));
                response.getWriter().print(mapper.writeValueAsString(null));

            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            sess.close();
        }

    }

}
