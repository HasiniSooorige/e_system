/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.Mapping.NotificationManager;
import Model.NotificationManageryM;
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
@WebServlet(name = "CredentialNotificationDataView", urlPatterns = {"/CredentialNotificationDataView"})

public class CredentialNotificationDataView extends HttpServlet {

    JSONObject objSend = new JSONObject();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        System.out.println("CREDENTIAL NOTIFICATION");
        System.out.println(id);
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();

        try {
            NotificationManager emp = (NotificationManager) sess.createQuery("From NotificationManager Where id='" + id + "'").setMaxResults(1).uniqueResult();

            if (emp == null) {
            } else {

                System.out.println("CREDENTIAL NOTIFICATION VIEW");

                NotificationManageryM hm = new NotificationManageryM();
                hm.setId(emp.getId());
                hm.setDescription(emp.getDescription());
                hm.setIsviewed(emp.getIsViewed());
                hm.setNotificationtypeid(emp.getNotificationType().getName());
                hm.setNotifydate(emp.getNotifyDate());
                hm.setReferance(emp.getReference());
                hm.setViewddate(emp.getViewedDate());

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
