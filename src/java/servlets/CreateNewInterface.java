/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

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
 * @author Personal
 */
@WebServlet(name = "CreateNewInterface", urlPatterns = {"/CreateNewInterface"})
public class CreateNewInterface extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (req.getSession().getAttribute("GUP_ID") != null) {
            String action = req.getParameter("action");
            Session sess = NewHibernateUtil.getSessionFactory().openSession();
            Transaction tx = sess.beginTransaction();
            if (action.equals("createInterface")) {

                System.out.println("CreateNewInterface ");
                int in_menuId = Integer.parseInt(req.getParameter("inmenu"));
                int in_smenuId = Integer.parseInt(req.getParameter("insmenu"));
                String in_name = req.getParameter("inname");
                String in_dsname = req.getParameter("indsname");
                String in_url = req.getParameter("inurl");
                String in_icon = req.getParameter("inicon");
                boolean result = createInterface(sess, in_menuId, in_smenuId, in_name, in_dsname, in_url, in_icon);
                if (result) {
                    tx.commit();
                    resp.getWriter().write("1");
                } else {
                    resp.getWriter().write("2");

                }
            } else if (action.equals("removeInterface")) {
                int si_id = Integer.parseInt(req.getParameter("id"));
                SystemInterface si = SystemInterfaceDAO.getByID(sess, si_id);
                sess.delete(si);
                tx.commit();
            }
            sess.flush();
            sess.clear();
            sess.close();
        } else {
            resp.sendRedirect("../index.jsp");
        }
    }

    private synchronized boolean createInterface(Session sess, int in_menuId, int in_smenuId, String in_name, String in_dsname, String in_url, String in_icon) {
        boolean result = false;
        InterfaceMenu in_menu = InterfaceMenuDAO.getByID(sess, in_menuId);
        InterfaceSubMenu in_smenu = InterfaceSubMenuDAO.getByID(sess, in_smenuId);
        SystemInterface si = (SystemInterface) sess.createQuery("from SystemInterface where url='" + in_url + "'").uniqueResult();
        if (si == null) {
            si = new SystemInterface();
            si.setDisplayName(in_dsname);
            si.setInterfaceMenu(in_menu);
            si.setInterfaceName(in_name);
            si.setInterfaceSubMenu(in_smenu);
            si.setUrl(in_url);
            si.setIcon(in_icon);
            sess.save(si);
            result = true;
        } else {
            result = false;
        }

        return result;
    }

}
