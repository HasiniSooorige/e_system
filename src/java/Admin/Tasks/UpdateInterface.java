/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Admin.Tasks;

import Model.Connection.NewHibernateUtil;
import Model.Logic.InterfaceMenuDAO;
import Model.Logic.InterfaceSubMenuDAO;
import Model.Logic.SystemInterfaceDAO;
import Model.Mapping.InterfaceMenu;
import Model.Mapping.InterfaceSubMenu;
import Model.Mapping.SystemInterface;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author sachintha
 */
@WebServlet(name = "UpdateInterface", urlPatterns = {"/UpdateInterface"})
public class UpdateInterface extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getSession().getAttribute("GUP_ID") != null) {
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction tx = sess.beginTransaction();
        int si_id = Integer.parseInt(req.getParameter("id"));
        int in_menuId = Integer.parseInt(req.getParameter("inmenu"));
        int in_smenuId = Integer.parseInt(req.getParameter("insmenu"));
        String in_name = req.getParameter("inname");
        String in_dsname = req.getParameter("indsname");
        String in_url = req.getParameter("inurl");
        String in_icon = req.getParameter("inicon");

        boolean result = updateInterface(sess, si_id, in_menuId, in_smenuId, in_name, in_dsname, in_url, in_icon);
        if (result) {
            tx.commit();
            resp.getWriter().write("1");
        }
        } else {
            resp.sendRedirect("login/admin.jsp");
        }
    }

    private boolean updateInterface(Session sess, int si_id, int in_menuId, int in_smenuId, String in_name, String in_dsname, String in_url, String in_icon) {
        SystemInterface si = SystemInterfaceDAO.getByID(sess, si_id);
        InterfaceMenu in_menu = InterfaceMenuDAO.getByID(sess, in_menuId);
        InterfaceSubMenu in_smenu = InterfaceSubMenuDAO.getByID(sess, in_smenuId);

        si.setDisplayName(in_dsname);
        si.setIcon(in_icon);
        si.setInterfaceMenu(in_menu);
        si.setInterfaceName(in_name);
        si.setInterfaceSubMenu(in_smenu);
//        si.setStatus(1);
        si.setUrl(in_url);
        sess.update(si);
        return true;
    }

}
