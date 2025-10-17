/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Connection.NewHibernateUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import Model.Mapping.UserCredentials;
import javax.servlet.annotation.WebServlet;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;

@WebServlet(name = "DeactivateCredential", urlPatterns = {"/DeactivateCredential"})

public class DeactivateCredential extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, NullPointerException {

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        int status = 200;
        String message = "";
        JSONObject objSend = new JSONObject();

        String activestatus = request.getParameter("status");
        String id = request.getParameter("id");

        Boolean uaction_check;

        if (activestatus.equals("true")) {
            uaction_check = false;
            System.out.println("0");
        } else {
            uaction_check = true;
            System.out.println("1");
        }

        UserCredentials mn = (UserCredentials) sess.createQuery("From UserCredentials Where id='" + id + "'").setMaxResults(1).uniqueResult();

        if (mn == null) {

            status = 404; // Not Found
            message = "User Credential not found";
            System.out.println("User Credential not found");

        } else {
            mn.setIsActive(uaction_check);
            sess.update(mn);

            if (uaction_check == false) {
                status = 200;
                message = "Credential Deactivated Successfully.";
                System.out.println("Credential Deactivate");
                objSend.put("status", status);
                objSend.put("message", message);
                response.getWriter().write(objSend.toString());
            } else {
                status = 300;
                message = "Credential Activated Successfully.";
                System.out.println("Credential Activated");
                objSend.put("status", status);
                objSend.put("message", message);
                response.getWriter().write(objSend.toString());
            }

            t.commit();
        }

        sess.flush();
        sess.clear();

    }

}
