/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Admin.Tasks;

import Model.Connection.NewHibernateUtil;
import Model.Logic.InterfaceMenuDAO;
import Model.Logic.UserLoginDAO;
import Model.Logic.UserRoleDAO;
import Model.Mapping.InterfaceMenu;
import Model.Mapping.InterfaceSubMenu;
import Model.Mapping.SystemInterface;
import Model.Mapping.UserLogin;
import Model.Mapping.UserRole;
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
@WebServlet(name = "CreateNewMenu", urlPatterns = {"/CreateNewMenu"})
public class CreateNewMenu extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getSession().getAttribute("GUP_ID") != null) {
            Session sess = NewHibernateUtil.getSessionFactory().openSession();
            Transaction tx = sess.beginTransaction();

            String gup = req.getSession().getAttribute("GUP_ID").toString();
            UserLogin ul = UserLoginDAO.getByID(sess, Integer.parseInt(gup));
            if (ul.getUserRole().getId() == 1 || ul.getUserRole().getId() == 2) {
                String menu_name = req.getParameter("menu_name");
                String menu_icon = req.getParameter("menu_icon");
                boolean result = createMenu(sess, menu_name, menu_icon);
                if (result) {
                    tx.commit();
                    resp.getWriter().write("1");
                } else {
                    resp.getWriter().write("2");

                }
                sess.flush();
                sess.clear();
                sess.close();

            } else {
                System.out.println("login as admin");
                resp.sendRedirect("../index.jsp");
            }
        } else {
            System.out.println("login as admin");
            resp.sendRedirect("../index.jsp");
        }

    }

    private synchronized boolean createMenu(Session sess, String menu_name, String menu_icon) {
        boolean result = false;
        try {
            InterfaceMenu im = (InterfaceMenu) sess.createQuery("from InterfaceMenu where menuName='" + menu_name + "'").uniqueResult();
            if (im != null) {
                im.setIcon(menu_icon);
                sess.update(im);
            } else {
                im = new InterfaceMenu();
                im.setMenuName(menu_name);
                im.setIcon(menu_icon);
                sess.save(im);
            }
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
