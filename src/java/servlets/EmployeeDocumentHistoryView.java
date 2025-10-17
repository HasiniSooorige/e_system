/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.EmployeeDocumentsM;
import Model.Mapping.EmployeeDocuments;
import Model.Mapping.ProjectGopAgreement;
import Model.ProjectGopAgreementM;
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
@WebServlet(name = "EmployeeDocumentHistoryView", urlPatterns = {"/EmployeeDocumentHistoryView"})
public class EmployeeDocumentHistoryView extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("__________________Employee  Document  History  View______________________");

        String id = request.getParameter("id");
        System.out.println("Emp Id  :" + id);

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONArray jSONArray = new JSONArray();

        try {
            List<EmployeeDocumentsM> em = new ArrayList<>();

            List<EmployeeDocuments> list = sess.createQuery("From EmployeeDocuments Where employee_id='" + id + "'").list();

            if (!list.isEmpty()) {

                System.out.println("list-size" + list.size());

                for (EmployeeDocuments per : list) {

                    EmployeeDocumentsM pem = new EmployeeDocumentsM();

                    pem.setId(per.getId());
                    pem.setUrl(per.getUrl());
                    pem.setName(per.getName());
                    pem.setDocumenttypeid(per.getDocumentType().getId());
                    pem.setDocumenttypename(per.getDocumentType().getName());

                    em.add(pem);

                }
            }

            String jsonString = new Gson().toJson(em);
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
