/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.GeneralOrganizationProfileM;
import Model.Mapping.GeneralOrganizationProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@WebServlet(name = "ClientDataView", urlPatterns = {"/ClientDataView"})
public class ClientDataView extends HttpServlet {

        JSONObject objSend = new JSONObject();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("____________Client  Data  View___________");
        String id = request.getParameter("id");
        System.out.println("id  :" + id);
        
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        
         GeneralOrganizationProfile gop = (GeneralOrganizationProfile) sess.createQuery("From GeneralOrganizationProfile Where id='" + id + "'").setMaxResults(1).uniqueResult();

        GeneralOrganizationProfileM gm = new GeneralOrganizationProfileM();
        
        if (gop == null) {
            System.out.println("Client Id Not Ok");
        } else {

            System.out.println("Client View Id Ok");

            gm.setId(gop.getId());
            gm.setName(gop.getName());
            gm.setAddress1(gop.getAddress1());
            gm.setAddress2(gop.getAddress2());
            gm.setAddress3(gop.getAddress3());
            gm.setEmail(gop.getEmail());
            gm.setContactNo(gop.getContactNo());
            gm.setFaxNo(gop.getFaxNo());
            gm.setCreatedDate(gop.getCreatedDate());
            gm.setCountry(gop.getCountry().getName());
            gm.setLogo(gop.getLogo());

            System.out.println("view - client");

            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValueAsString(gm);
            System.out.println(mapper.writeValueAsString(gm));
            response.getWriter().print(mapper.writeValueAsString(gm));

        }

        try {

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            sess.close();
        }

    }

}
