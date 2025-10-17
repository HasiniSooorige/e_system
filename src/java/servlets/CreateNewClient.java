package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.Mapping.GeneralOrganizationProfile;
import Model.Mapping.GeneralUserProfile;
import Model.Mapping.GupGopManager;
import Model.Mapping.UserLogin;
import Model.Mapping.UserRole;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
@WebServlet(name = "CreateNewClient", urlPatterns = {"/CreateNewClient"})
public class CreateNewClient extends HttpServlet {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("_____________Create  New  Client_______________");
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int status = 200;
        String message = "";

        try {
            String clientId = request.getParameter("clientId");
            String clientFirstName = request.getParameter("clientFirstName");
            String clientLastName = request.getParameter("clientLastName");
            String clientNic = request.getParameter("clientNic");
            String clientemail = request.getParameter("clientemail");
            String companycontactNo = request.getParameter("companycontactNo");
            String clientMobileNo = request.getParameter("clientMobileNo");

            GeneralOrganizationProfile gop = (GeneralOrganizationProfile) sess.createQuery("From GeneralOrganizationProfile Where id='" + clientId + "'").setMaxResults(1).uniqueResult();

            GeneralUserProfile gup = (GeneralUserProfile) sess.createQuery("From GeneralUserProfile Where nic='" + clientNic + "' OR email='" + clientemail + "'").setMaxResults(1).uniqueResult();

            if (gup == null) {

                gup = new GeneralUserProfile();

                gup.setNic(clientNic);
                gup.setFirstName(clientFirstName);
                gup.setLastName(clientLastName);
                gup.setAddress1(gop.getAddress1());
                gup.setAddress2(gop.getAddress2());
                gup.setAddress3(gop.getAddress3());
                gup.setMobileNo(clientMobileNo);
                gup.setHomeNo(companycontactNo);
                gup.setEmail(clientemail);
                gup.setProfileCreatedDate(convertStringToDate(timeStamp));
                gup.setCountry(gop.getCountry());

                sess.save(gup);
                System.out.println("G U P Saved.");

                UserLogin ul = new UserLogin();

                ul.setUsername(clientNic);
                ul.setPassword("00000");
                ul.setMaxLoginAttemp(3);
                ul.setCountAttempt(0);
                ul.setIsActive(true);
                ul.setGeneralUserProfile(gup);
                ul.setUserRole((UserRole) sess.load(UserRole.class, 3));

                sess.save(ul);
                System.out.println("USER LOGIN Saved");

                GupGopManager gup_gop = new GupGopManager();

                gup_gop.setGeneralUserProfile(gup);
                gup_gop.setGeneralOrganizationProfile(gop);
                gup_gop.setIsActive(true);

                sess.save(gup_gop);
                System.out.println("GOP - GUP Manager Saved");

                t.commit();

                status = 200;
                message = "Data Succesfully Saved.";
                System.out.println("Done");

            } else {
                status = 400;
                message = "User already registed.";
            }

        } catch (Exception e) {
            status = 500;
            message = "Error Occurred";
            e.printStackTrace();
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
