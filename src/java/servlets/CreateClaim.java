/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.CommonMethod.ComPath;
import Model.Connection.NewHibernateUtil;
import Model.Logic.UserLoginDAO;
import Model.Mapping.ClaimItems;
import Model.Mapping.ClaimStatus;
import Model.Mapping.ClaimType;
import Model.Mapping.Claims;
import Model.Mapping.Employee;
import Model.Mapping.UserLogin;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.annotation.MultipartConfig;
import org.json.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;

@WebServlet(name = "CreateClaim", urlPatterns = {"/CreateClaim"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10, // 10 MB
        maxFileSize = 1024 * 1024 * 1000, // 1 GB
        maxRequestSize = 1024 * 1024 * 1000)   	// 1 GB)
public class CreateClaim extends HttpServlet {

    String file_path = ComPath.getFILE_PATH() + "/project-logo/";

    private static final long serialVersionUID = 1L;
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
//ADMIN & USER

        response.setContentType("text/html");
        String action = request.getParameter("action");
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";
        boolean success = false;

        Date currentDate = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String formattedDate = dateFormat.format(currentDate);

        // Set the response content type
        response.setContentType("text/plain");

        // Initialize a PrintWriter to send a response
        PrintWriter out = response.getWriter();

        try {
            // Retrieve the FormData sent from the client
            String[] req_dates = request.getParameterValues("req_date[]");
            String[] req_amounts = request.getParameterValues("req_amount[]");
            String[] fileToUploads = request.getParameterValues("attachment[]");
            String[] claimTypes = request.getParameterValues("claimType[]");

            String textEntered = request.getParameter("textEntered");
            String totalAmount = request.getParameter("totalAmount");
            String userID = request.getSession().getAttribute("User_ID").toString();
            System.out.println("cla " + claimTypes);
            Claims claims = new Claims();

            Date parsedDate = dateFormat.parse(formattedDate);

            claims.setAddedDate(parsedDate);
            claims.setTotalAmount(Double.parseDouble(totalAmount));
            claims.setNote(textEntered);
            claims.setStatus(true);
            UserLogin user = UserLoginDAO.getByID(sess, Integer.parseInt(userID));
            System.out.println("user G" + user.getGeneralUserProfile().getId());
            System.out.println("user " + user.getId());

            Employee emp = (Employee) sess.createQuery("From Employee Where generalUserProfile.id='" + user.getGeneralUserProfile().getId() + "'").setMaxResults(1).uniqueResult();
            System.out.println("emp " + emp.getId());
            claims.setEmployee(emp);

            ClaimStatus clsts = (ClaimStatus) sess.createQuery("From ClaimStatus Where id= '1'").setMaxResults(1).uniqueResult();
            claims.setClaimStatus(clsts);

            sess.save(claims);

            for (int i = 0; i < req_dates.length; i++) {
                ClaimItems claimItems = new ClaimItems();

                String req_amount = req_amounts[i];
                String fileToUpload = fileToUploads[i];
                String req_date = req_dates[i];
                String claim_Type = claimTypes[i];

                claimItems.setAmount(Double.parseDouble(req_amount));

                claimItems.setAttachment(fileToUpload);

                Date date = dateFormat.parse(req_date);

                claimItems.setBilldDate(date);

                if ("Traveling".equals(claim_Type)) {
                    ClaimType clty = (ClaimType) sess.createQuery("From ClaimType Where id= '1'").setMaxResults(1).uniqueResult();
                    claimItems.setClaimType(clty);
                } else if ("Food and Beverage".equals(claim_Type)) {
                    ClaimType clty = (ClaimType) sess.createQuery("From ClaimType Where id= '2'").setMaxResults(1).uniqueResult();
                    claimItems.setClaimType(clty);
                } else if ("Stationary".equals(claim_Type)) {
                    ClaimType clty = (ClaimType) sess.createQuery("From ClaimType Where id= '3'").setMaxResults(1).uniqueResult();
                    claimItems.setClaimType(clty);
                }

                claimItems.setClaims(claims);
                sess.save(claimItems);

            }
            t.commit();

            status = 200;
            message = "Claim Request Successfully Saved";

            sess.flush();
            sess.clear();

        } catch (Exception e) {
            status = 400;
            message = "Claim Request Not Saved! Please Try Again.";
            e.printStackTrace();
            out.println("error");
        } finally {
            sess.close();

        }
        objSend.put("status", status);
        objSend.put("message", message);
        response.getWriter().print(objSend);
        System.out.println(objSend);
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
