/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.CommonMethod.Com;
import Model.Connection.NewHibernateUtil;
import Model.EmployeeM;
import Model.Logic.UserLoginDAO;
import Model.Mapping.Employee;
import Model.Mapping.GeneralUserProfile;
import Model.Mapping.UniversalPayrollManager;
import Model.Mapping.UserLogin;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import javax.servlet.annotation.WebServlet;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author personal
 */
@WebServlet(name = "AddEmployeesToSalarySheet", urlPatterns = {"/AddEmployeesToSalarySheet"})
public class AddEmployeesToSalarySheet extends HttpServlet {

    JSONObject objSend = new JSONObject();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        if (req.getSession().getAttribute("GUP_ID") != null) {

            JSONObject objSend = new JSONObject();
            int status = 200;
            String message = "";
            String duplicates = "Already Addedd Selectd Month Salery Sheet ! ";
            boolean is_duplicated = false;
            Session sess = NewHibernateUtil.getSessionFactory().openSession();
            String month = req.getParameter("month");
            String year = req.getParameter("year");
            GeneralUserProfile gup = (GeneralUserProfile) sess.load(GeneralUserProfile.class, Integer.parseInt(req.getSession().getAttribute("GUP_ID").toString()));
            String gup_id = req.getSession().getAttribute("GUP_ID").toString();
            UserLogin ul = UserLoginDAO.getByID(sess, Integer.parseInt(gup_id));
            if (ul.getUserRole().getId() == 1 || ul.getUserRole().getId() == 2) {
                try {
                    String selected_month = "";

                    selected_month = year + "-" + month + "-01";
                    String jsn = req.getParameter("jsnobj").trim();
                    JSONParser parser = new JSONParser();
                    Object obj = parser.parse(jsn);
                    JSONObject job = (JSONObject) obj;
                    JSONArray jsnarr = (JSONArray) job.get("jsn");

//                    System.out.println("Date " + Com.getFormattedDateString(selected_month));
                    for (Iterator it = jsnarr.iterator(); it.hasNext();) {
                        is_duplicated = false;
                        JSONObject json_object = (JSONObject) it.next();
                        int empid = Integer.parseInt(json_object.get("empid").toString().trim());
                        UniversalPayrollManager payrollManager = (UniversalPayrollManager) sess.createQuery("from UniversalPayrollManager "
                                + "where employee.id='" + empid + "'AND salaryDate='" + selected_month + "'").uniqueResult();
                        if (payrollManager == null) {
                            payrollManager = new UniversalPayrollManager();
                            payrollManager.setAttendance(0.0);
                            payrollManager.setBasicSalary(0.0);
                            payrollManager.setBr(0.0);
                            payrollManager.setEmployee((Employee) sess.load(Employee.class, empid));
                            payrollManager.setEpf12(0.0);
                            payrollManager.setEpf8(0.0);
                            payrollManager.setEtf3(0.0);
                            payrollManager.setTax(0.0);

                            payrollManager.setGeneralUserProfileByGeneralUserProfileGupIdAddedBy(gup);
                            payrollManager.setGeneralUserProfileByGeneralUserProfileGupIdUpdatedBy(gup);
                            payrollManager.setGrossSalary(0.0);
                            payrollManager.setHouseRental(0.0);
                            payrollManager.setIncentive(0.0);
                            payrollManager.setLastUpdateTime(new Date());
                            payrollManager.setNoPay(0.0);
                            payrollManager.setOtherDeduction(0.0);
                            payrollManager.setPerformance(0.0);
                            payrollManager.setRecordAddedDate(new Date());
                            payrollManager.setSalaryAdvanced(0.0);
                            payrollManager.setSalaryDate(Com.getFormattedDate(selected_month));
                            payrollManager.setTotalSalary(0.0);
                            payrollManager.setTravelling(0.0);
                            sess.save(payrollManager);
                            message = "Successfully Added!";
                        } else {
                            is_duplicated = true;
                            duplicates += " , " + payrollManager.getEmployee().getGeneralUserProfile().getNic();
                        }
                    }
                    if (is_duplicated) {
                        message = message + duplicates;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    status = 500;
                    message = "ERROR";
                }
            }
            sess.beginTransaction().commit();

            sess.close();

            objSend.put("status", status);
            objSend.put("message", message);
            objSend.put("duplicates", duplicates);
            response.setContentType("text/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            try {
                out.print(objSend);
            } finally {
                out.close();
            }

        } else {
            response.sendRedirect("login/admin.jsp");
        }
    }

}
