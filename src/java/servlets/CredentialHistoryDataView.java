/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Com.Tools.Security;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.UserCredentialHistory;
import Model.Mapping.UserCredentialRole;
import Model.UserCeredentialHistoryM;
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
@WebServlet(name = "CredentialHistoryDataView", urlPatterns = {"/CredentialHistoryDataView"})

public class CredentialHistoryDataView extends HttpServlet {

    JSONObject objSend = new JSONObject();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        System.out.println("CREDENTIAL HISTORY");
        System.out.println(id);
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();

        try {
            UserCredentialHistory emp = (UserCredentialHistory) sess.createQuery("From UserCredentialHistory Where id='" + id + "'").setMaxResults(1).uniqueResult();

            if (emp == null) {
            } else {

                System.out.println("CREDENTIAL VIEW");

                UserCeredentialHistoryM hm = new UserCeredentialHistoryM();
                hm.setCategory(emp.getUserCredentials().getUserCredentialCategory().getName());
                hm.setId(emp.getId());
                hm.setPassword(Security.decrypt(emp.getPassword()));
                hm.setUsername(emp.getUsername());
                hm.setProject(emp.getUserCredentials().getProjects().getName());
                hm.setReason(emp.getReason());
                hm.setType(emp.getUserCredentials().getUserCredentialType().getName());
                hm.setUpdatedby(emp.getGeneralUserProfile().getFirstName() + " " + emp.getGeneralUserProfile().getLastName());
                hm.setUpdateddate(emp.getUpdatedDate());
                UserCredentialRole eer = (UserCredentialRole) sess.createQuery("From UserCredentialRole Where userCredentials='" + emp.getUserCredentials().getId() + "'").setMaxResults(1).uniqueResult();

                hm.setRoll(eer.getCredentialRoles().getName());

                System.out.println("view_cred_history");

                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValueAsString(hm);
                System.out.println(mapper.writeValueAsString(hm));
                response.getWriter().print(mapper.writeValueAsString(hm));

            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            sess.close();
        }

    }

}
