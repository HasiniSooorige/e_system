/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.Mapping.UserCredentialRole;
import Model.Mapping.UserCredentials;
import Model.UserCredentialsM;
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
@WebServlet(name = "CredentialDataView", urlPatterns = {"/CredentialDataView"})

public class CredentialDataView extends HttpServlet {

    JSONObject objSend = new JSONObject();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        System.out.println("id------------");
        System.out.println(id);
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();

        try {
            UserCredentials emp = (UserCredentials) sess.createQuery("From UserCredentials Where id='" + id + "'").setMaxResults(1).uniqueResult();
            UserCredentialRole eer = (UserCredentialRole) sess.createQuery("From UserCredentialRole Where userCredentials='" + id + "'").setMaxResults(1).uniqueResult();

            if (emp == null) {
            } else {

                System.out.println("CREDENTIAL VIEW");

                //  System.out.println("emp.getRegisteredDate");
                System.out.println(emp.getUserCredentialType().getName());

                UserCredentialsM em = new UserCredentialsM();
                em.setType(emp.getUserCredentialType().getName());
                em.setRoll(eer.getCredentialRoles().getName());
                Boolean active = emp.getIsActive();
                if (active == false) {
                    em.setStatus("Inactive");
                } else {
                    em.setStatus("Active");
                }

                em.setId(emp.getId());
                em.setCreateddate(emp.getEnteredDate());
                em.setPassword(emp.getPassword());
                em.setCreateddate(emp.getEnteredDate());
                em.setCategory(emp.getUserCredentialCategory().getName());
                em.setEmployeename(emp.getGeneralUserProfile().getFirstName() + " " + emp.getGeneralUserProfile().getLastName());
                em.setProject(emp.getProjects().getName());
                em.setUsername(emp.getUsername());
                em.setNote(emp.getNote());

                System.out.println("view_cred");

                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValueAsString(em);
                System.out.println(mapper.writeValueAsString(em));
                response.getWriter().print(mapper.writeValueAsString(em));

            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            sess.close();
        }

    }

}
