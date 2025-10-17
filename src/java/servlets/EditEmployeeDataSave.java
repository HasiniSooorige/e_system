/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Mapping.Employee;
import Model.Connection.NewHibernateUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import Model.Mapping.Designation;
import Model.Mapping.GeneralUserProfile;
import Model.Mapping.WorkHistory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.annotation.WebServlet;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;

@WebServlet(name = "EditEmployeeDataSave", urlPatterns = {"/EditEmployeeDataSave"})
public class EditEmployeeDataSave extends HttpServlet {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, NullPointerException {
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        int status = 200;
        String message = "";
        JSONObject objSend = new JSONObject();

        System.out.println("______________Edit  Employee  Data  Save______________________");
        try {

            String designation = req.getParameter("designation");
            String epf_no = req.getParameter("epf_no");
            String nic = req.getParameter("nic");
            String first_name = req.getParameter("first_name");
            String last_name = req.getParameter("last_name");
            String address1 = req.getParameter("address1");
            String address2 = req.getParameter("address2");
            String address3 = req.getParameter("address3");
            String mobile_no = req.getParameter("mobile_no");
            String home_no = req.getParameter("home_no");
            String emergency_contact = req.getParameter("emergency_contact");
            String email = req.getParameter("email");

            System.out.println(designation + " , " + epf_no + " , " + nic + " , " + first_name + " , " + last_name + " , " + address1 + " , " + address2 + " , " + address3 + " , " + mobile_no + " , " + home_no + " , " + email + " , " + emergency_contact);

            GeneralUserProfile gup = (GeneralUserProfile) sess.createQuery("From GeneralUserProfile Where nic='" + nic + "'").setMaxResults(1).uniqueResult();
            Integer gupId = gup.getId();

            if (gup == null) {
                status = 400;
                message = "Employee Not Found";
                System.out.println("GUP Not Found");
            } else {

                Employee emp = (Employee) sess.createQuery("From Employee Where general_user_profile_id='" + gupId + "'").setMaxResults(1).uniqueResult();

                if (emp == null) {
                    status = 400;
                    message = "Employee Not Found";
                    System.out.println("Emp Not Found");
                } else {

                    gup.setFirstName(first_name);
                    gup.setLastName(last_name);
                    gup.setAddress1(address1);
                    gup.setAddress2(address2);
                    gup.setAddress3(address3);
                    gup.setMobileNo(mobile_no);
                    gup.setHomeNo(home_no);
                    gup.setEmail(email);
                    gup.setEmergencyContactNo(emergency_contact);

                    sess.update(gup);

                    if (emp.getDesignation().getId().equals(Integer.parseInt(designation))) {

                        System.out.println("Designation Same");

                    } else {
                        emp.setDesignation((Designation) sess.load(Designation.class, Integer.parseInt(designation)));
                        sess.update(emp);

                        WorkHistory history = new WorkHistory();
                        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
                        history.setDateFrom(convertStringToDate(timeStamp));
                        history.setDateTo(convertStringToDate(timeStamp));
                        history.setDesignationByDesignationFrom((Designation) sess.load(Designation.class, Integer.parseInt(designation)));
                        history.setDesignationByDesignationTo((Designation) sess.load(Designation.class, Integer.parseInt(designation)));
                        history.setEmployee(emp);
                        sess.save(history);
                    }

                }

                status = 200;
                message = "Employee Details Updated Successfully!";

            }

            t.commit();

            sess.flush();
            sess.clear();

        } catch (Exception e) {
            status = 500;
            message = "Error Occurred";
            e.printStackTrace();
        } finally {
            sess.close();
        }
        objSend.put("status", status);
        objSend.put("message", message);
        resp.getWriter().print(objSend);

    }

    public static Date convertStringToDate(String date) {
        if (date != null) {
            try {
                return FORMATTER.parse(date);
            } catch (ParseException e) {
                // nothing we can do if the input is invalid
                throw new RuntimeException(e);
            }
        }
        return null;
    }

}
