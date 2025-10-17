/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.EmployeeM;
import Model.Mapping.Employee;
import Model.Mapping.GeneralUserProfile;
import Model.Mapping.UserLogin;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.json.simple.JSONObject;

/**
 *
 * @author kbnc
 */
public class EditEmployeeDataG extends HttpServlet {

    JSONObject objSend = new JSONObject();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        String epfno=request.getParameter("epfno");
//        System.out.println("epfno");
//        System.out.println(epfno);
Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();

        System.out.println("EditEmployeeDataG");
//request.getSession(false).getAttribute("user")== null
            
             String nic=(String) request.getSession(false).getAttribute("nic");
            
            System.out.println(nic);
            
            


        
        
        
        GeneralUserProfile gup = (GeneralUserProfile) sess.createQuery("From GeneralUserProfile Where nic='" + nic + "'").setMaxResults(1).uniqueResult();
        
        Criteria c=sess.createCriteria(Employee.class);
        c.add(Restrictions.eq("generalUserProfile", gup));
        Employee employee=(Employee) c.uniqueResult();
        
        
        
        
        Employee emp = (Employee) sess.createQuery("From Employee Where epfNo='" + employee.getEpfNo() + "'").setMaxResults(1).uniqueResult();
        
        if(emp ==null){
        }else{
            
            System.out.println("Employee_epf_ok");
            
            
            System.out.println("emp.getRegisteredDate");
            System.out.println(emp.getRegisteredDate());
            
            System.out.println(emp.getEpfNo());
            System.out.println(emp.getGeneralUserProfile().getEmail());
            EmployeeM em=new EmployeeM();
            em.setEpfno(emp.getEpfNo());
            em.setRegistereddate(emp.getRegisteredDate());
            
            em.setGeneralorganizationname(emp.getGeneralOrganizationProfile().getName());
            em.setDesignation(emp.getDesignation().getName());
            em.setNic(emp.getGeneralUserProfile().getNic());
            em.setFirstname(emp.getGeneralUserProfile().getFirstName());
            em.setLastname(emp.getGeneralUserProfile().getLastName());
            em.setEmail(emp.getGeneralUserProfile().getEmail());
            em.setMobileno(emp.getGeneralUserProfile().getMobileNo());
            em.setAddress1(emp.getGeneralUserProfile().getAddress1());
            em.setAddress2(emp.getGeneralUserProfile().getAddress2());
            em.setAddress3(emp.getGeneralUserProfile().getAddress3());
            em.setHomeno(emp.getGeneralUserProfile().getHomeNo());
            em.setCoutry(emp.getGeneralUserProfile().getCountry().getName());
            
//            objSend.put(em, "edit_employee");
            System.out.println("edit_emp");
            
            
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValueAsString(em);
            
            System.out.println(mapper.writeValueAsString(em));
            response.getWriter().print(mapper.writeValueAsString(em));
            
            
        
        }
        
        try {
            
            
        } catch (Exception e) {
            e.printStackTrace();
            
        }finally {
            sess.close();
        }
        
        
        
        
        
        
        
        
    }

   
   

  
}
