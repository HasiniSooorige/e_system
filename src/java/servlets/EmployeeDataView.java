package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.EmployeeM;
import Model.Mapping.Employee;
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

@WebServlet(name = "EmployeeDataView", urlPatterns = {"/EmployeeDataView"})

public class EmployeeDataView extends HttpServlet {

    JSONObject objSend = new JSONObject();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        System.out.println("id");
        System.out.println(id);
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();

        Employee emp = (Employee) sess.createQuery("From Employee Where id='" + id + "'").setMaxResults(1).uniqueResult();

        if (emp == null) {
        } else {

            System.out.println("Employee_view_epf_ok");

            System.out.println("emp.getRegisteredDate");
            System.out.println(emp.getRegisteredDate());

            System.out.println(emp.getEpfNo());
            System.out.println(emp.getGeneralUserProfile().getEmail());
            EmployeeM em = new EmployeeM();
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
            em.setGender(emp.getGeneralUserProfile().getGender().getName());
            em.setResignationDate(emp.getResignationDate());
            em.setEmergencyContactNo(emp.getGeneralUserProfile().getEmergencyContactNo());
            em.setDateofbirth(emp.getGeneralUserProfile().getDob());

            sess.flush();
            sess.clear();

            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValueAsString(em);
            System.out.println(mapper.writeValueAsString(em));
            response.getWriter().print(mapper.writeValueAsString(em));

        }

        sess.close();

    }

}
