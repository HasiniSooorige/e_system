/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.GeneralOrganizationProfileM;
import Model.Mapping.GupGopManager;
import Model.Mapping.ProjectEmployees;
import Model.ProjectEmpRoleM;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONArray;

/**
 *
 * @author HP
 */
@WebServlet(name = "ClientTeamListView", urlPatterns = {"/ClientTeamListView"})
public class ClientTeamListView extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("________Client Team List View_______________");

        String id = request.getParameter("id");
        System.out.println("Client id  :" + id);

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONArray jSONArray = new JSONArray();
        
        int is_active=1;

        try {
            List<GeneralOrganizationProfileM> gm = new ArrayList<>();

            List<GupGopManager> list = sess.createQuery("From GupGopManager Where general_organization_profile_id='" + id + "' and is_active='" + is_active + "'").list();

            String content = "";

            if (!list.isEmpty()) {

                System.out.println("list-size" + list.size());

                for (GupGopManager per : list) {

                    GeneralOrganizationProfileM pem = new GeneralOrganizationProfileM();
                    
                    pem.setGupFirstName(per.getGeneralUserProfile().getFirstName());
                    pem.setGupLastName(per.getGeneralUserProfile().getLastName());
                    pem.setGupNic(per.getGeneralUserProfile().getNic());
                    pem.setGupEmail(per.getGeneralUserProfile().getEmail());
                    pem.setGupMobileNo(per.getGeneralUserProfile().getMobileNo());

                    gm.add(pem);

                }
            }

            String jsonString = new Gson().toJson(gm);
            System.out.println("jsonString");
            System.out.println(jsonString);
            response.getWriter().print(jsonString);

        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            sess.close();
        }

    }

}
