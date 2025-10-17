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

/**
 *
 * @author jalana
 */
public class DocumentDataLoad extends HttpServlet {

    JSONObject objSend = new JSONObject();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        System.out.println("id");
        System.out.println(id);
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
    List<EmployeeDocumentsM> uds = new ArrayList<>();
      List<EmployeeDocuments> list =        sess.createQuery("From EmployeeDocuments Where employee='" + id + "'").list();

       
      int naId =0;
       int baId =0;
        int eduId =0;
       
      if (!list.isEmpty()) {
                  
                System.out.println("list-size");
                System.out.println(list.size());
                for (EmployeeDocuments gup : list) {
//                    gupp=gup;
                    EmployeeDocumentsM ud = new EmployeeDocumentsM();
                    
                    ud.setId(gup.getId());
                    ud.setEmployeeid(gup.getEmployee().getId());
                    ud.setDocumenttypeid(gup.getDocumentType().getId());
                    if(gup.getDocumentType().getId()==1){
                        naId =naId +1;
                      
                        
                    }
                     if(gup.getDocumentType().getId()==2){
                        baId =baId +1;
                         
                    }
                      if(gup.getDocumentType().getId()==3){
                        eduId =eduId +1;
                         
                    }
            ud.setUrl(gup.getUrl());
                ud.setDocSizeN(naId);
                 ud.setDocSizeB(baId);
                  ud.setDocSizeE(eduId);
//                    gupp=gup;
                    uds.add(ud);
                    
                }
               
        } else{
              System.out.println("list-size-empty");
         }
             
   

               String jsonString = new Gson().toJson(uds);
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
