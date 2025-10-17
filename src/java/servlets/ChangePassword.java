/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Com.Tools.Security;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.GeneralUserProfile;
import Model.Mapping.UserLogin;
import java.io.IOException;
import static java.lang.System.out;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.json.simple.JSONArray;

/**
 *
 * @author kbnc
 */
public class ChangePassword extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();

        boolean success = false;
        String message = "";
        Integer idURoll = null;
        String link = "";
        JSONArray js = new JSONArray();
        Map m = new HashMap();
        String origin = request.getHeader("Origin");

        String textpassword_current = request.getParameter("currentpassword");
        String textchpasswprd_new = request.getParameter("chpassword");
        System.out.println("pass__t");

        try {

            String nic = (String) request.getSession(false).getAttribute("nic");

            GeneralUserProfile gup = (GeneralUserProfile) sess.createQuery("From GeneralUserProfile Where nic='" + nic + "'").setMaxResults(1).uniqueResult();

//            UserLogin login = (UserLogin) sess.createQuery("From UserLogin Where generalUserProfile='" + gup + "'").setMaxResults(1).uniqueResult();
            Criteria c = sess.createCriteria(UserLogin.class);
            c.add(Restrictions.eq("generalUserProfile", gup));
            UserLogin login = (UserLogin) c.uniqueResult();

            System.out.println("login_fetch_user");
            System.out.println(login.getUsername());

            if (textpassword_current.equals(Security.decrypt(login.getPassword()))) {
                System.out.println("OK pass");

                login.setPassword(Security.encrypt(textchpasswprd_new));
                login.setIsActive(true);

                sess.update(login);
                t.commit();

                message = "password change successfully";

                if (login.getUserRole().getName().equals("Super Admin")) {
                    idURoll = login.getUserRole().getId();
                    link = "page-mng_dash_bord.jsp";
                } else if (login.getUserRole().getName().equals("Manager")) {
                    idURoll = login.getUserRole().getId();
                    link = "page-mng_dash_bord.jsp";

                } else {
                    idURoll = login.getUserRole().getId();
                    link = "page-emp_dash_bord.jsp";

                }

                success = true;

            } else {
                System.out.println("Please enter Correct password");
                message = "Please enter Correct password";
                success = false;

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sess.close();
        }

        m.put("success", success);
        m.put("message", message);
        m.put("idURoll", idURoll);
        m.put("link", link);
        js.add(m);
        out.print(js.toJSONString());
        response.getWriter().write(js.toJSONString());

    }

}
