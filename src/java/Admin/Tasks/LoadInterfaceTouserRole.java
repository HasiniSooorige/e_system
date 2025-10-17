/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Admin.Tasks;

import Model.Connection.NewHibernateUtil;
import Model.Logic.SystemInterfaceDAO;
import Model.Mapping.SystemInterface;
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
@WebServlet(name = "LoadInterfaceTouserRole", urlPatterns = {"/LoadInterfaceTouserRole"})
public class LoadInterfaceTouserRole extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getSession().getAttribute("GUP_ID") != null) {
        System.out.println("LoadInterfaceTouserRole");
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        int interface_id = Integer.parseInt(req.getParameter("interface_id"));
        SystemInterface si = SystemInterfaceDAO.getByID(sess, interface_id);
        List<UserRoleHasSystemInterface> ur_list = sess.createQuery("from UserRoleHasSystemInterface where system_interface_si_id='" + si.getSiId() + "'").list();
        if (!ur_list.isEmpty()) {
            Map<Integer, String> hm = new HashMap<Integer, String>();
            for (UserRoleHasSystemInterface ur : ur_list) {
                hm.put(ur.getId(), ur.getUserRole().getName());
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
