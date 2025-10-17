/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Admin.Tasks;

import Model.Connection.NewHibernateUtil;
import Model.Logic.SystemInterfaceDAO;
import Model.Logic.UserRoleDAO;
import Model.Mapping.SystemInterface;
import Model.Mapping.UserRole;
import Model.Mapping.UserRoleHasSystemInterface;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;

/**
 *
 * @author sachintha
 */
@WebServlet(name = "LoadUserRoleToInterface", urlPatterns = {"/LoadUserRoleToInterface"})
public class LoadUserRoleToInterface extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getSession().getAttribute("GUP_ID") != null) {
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        int userrole = Integer.parseInt(req.getParameter("userrole"));
        UserRole ur = UserRoleDAO.getByID(sess, userrole);
        List<UserRoleHasSystemInterface> ur_list = sess.createQuery("from UserRoleHasSystemInterface where userRole.id='" + ur.getId() + "'").list();
        if (!ur_list.isEmpty()) {
            Map<Integer, String> hm = new HashMap<Integer, String>();
            for (UserRoleHasSystemInterface urhsi : ur_list) {
                hm.put(urhsi.getId(), urhsi.getSystemInterface().getDisplayName());
            }
            String json = new Gson().toJson(hm);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(json);
        } else {
            resp.getWriter().write("null");
        }
        sess.flush();
        sess.clear();
        sess.close();
        } else {
            resp.sendRedirect("../index.jsp");
        }

    }

}
