/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.EmployeeDocumentsM;
import Model.EmployeeM;
import Model.Mapping.Employee;
import Model.Mapping.EmployeeDocuments;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;


public class DocumentDataCheck extends HttpServlet {

    JSONObject objSend = new JSONObject();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        System.out.println("doc data check");
        System.out.println(id);
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        List<EmployeeDocumentsM> uds = new ArrayList<>();
        List<EmployeeDocuments> list = sess.createQuery("From EmployeeDocuments Where employee='" + id + "'").list();

        boolean naIdCheck = false;
        boolean baIdCheck = false;
        boolean eduIdCheck = false;
        int widthX = 0;
        int widthY = 0;
        int widthZ = 0;
        if (!list.isEmpty()) {

            System.out.println("list-size");
            System.out.println(list.size());
            for (EmployeeDocuments gup : list) {
//                    gupp=gup;
                EmployeeDocumentsM ud = new EmployeeDocumentsM();

                ud.setDocumenttypeid(gup.getDocumentType().getId());
                if (gup.getDocumentType().getId() == 1) {
                    naIdCheck = true;

                }
                if (gup.getDocumentType().getId() == 2) {
                    baIdCheck = true;

                }
                if (gup.getDocumentType().getId() == 3) {
                    eduIdCheck = true;

                }

            }
            if (naIdCheck) {
                widthX = 30;
            } else {
                widthX = 0;
            }
            if (baIdCheck) {
                widthY = 30;
            } else {
                widthY = 0;
            }
            if (eduIdCheck) {
                widthZ = 30;
            } else {
                widthZ = 0;
            }

        } else {
            System.out.println("list-size-empty");
        }

        widthX = widthX + widthY + widthZ;
        if(widthX==90){
            widthX =100;
        }
   System.out.println(widthX);
        String jsonString = new Gson().toJson(widthX);
        
        System.out.println("jsonString");
        System.out.println(jsonString);
        response.getWriter().print(jsonString);

        try {

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            sess.close();
        }

    }

}