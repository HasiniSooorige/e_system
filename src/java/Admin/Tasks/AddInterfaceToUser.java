/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Admin.Tasks;

import Model.Connection.NewHibernateUtil;
import Model.Logic.SystemInterfaceDAO;
import Model.Logic.UserLoginDAO;
import Model.Logic.UserRoleDAO;
import Model.Mapping.LoginSession;
import Model.Mapping.SystemInterface;
import Model.Mapping.UserLogin;
import Model.Mapping.UserLoginHasSystemInterface;
import Model.Mapping.UserRole;
import Model.Mapping.UserRoleHasSystemInterface;
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
@WebServlet(name = "AddInterfaceToUser", urlPatterns = {"/AddInterfaceToUser"})
public class AddInterfaceToUser extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("AddInterfaceToUser");
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction tx = sess.beginTransaction();
        String action = req.getParameter("action");
        int interface_id = Integer.parseInt(req.getParameter("interface_id"));
        int userrole_id = 0;
        int user_id = 0;
        UserLogin ul = null;
        UserRole ur = null;

        boolean result = false;
        if (action.equals("addInterfaceToUserRole")) {
            userrole_id = Integer.parseInt(req.getParameter("userrole_id"));
            ur = UserRoleDAO.getByID(sess, userrole_id);
            result = addInterfaceToUserRole(sess, interface_id, ur);
        } else if (action.equals("addInterfaceToUser")) {
            user_id = Integer.parseInt(req.getParameter("user_id"));
            ul = UserLoginDAO.getByID(sess, user_id);
            result = addInterfaceToUser(sess, interface_id, ul);
        } else if (action.equals("addInterfaceToUserRoleNew")) {
        }

        if (result) {
            tx.commit();
            resp.getWriter().write("1");
        } else {
            resp.getWriter().write("2");
        }

        sess.flush();
        sess.clear();
        sess.close();
    }

    private boolean addInterfaceToUserRole(Session sess, int interface_id, UserRole ur) {
        boolean result = false;
        SystemInterface si = SystemInterfaceDAO.getByID(sess, interface_id);
        UserRoleHasSystemInterface urhsi = (UserRoleHasSystemInterface) sess.createQuery("from UserRoleHasSystemInterface where systemInterface.siId='" + si.getSiId() + "'").uniqueResult();
        if (urhsi == null) {
            urhsi = new UserRoleHasSystemInterface();
            urhsi.setSystemInterface(si);
            urhsi.setUserRole(ur);
            sess.save(urhsi);
            result = true;
        }
        return result;
    }

    private boolean addInterfaceToUser(Session sess, int interface_id, UserLogin ul) {
        SystemInterface si = SystemInterfaceDAO.getByID(sess, interface_id);
        UserLoginHasSystemInterface ulhsi = new UserLoginHasSystemInterface();
        ulhsi.setSystemInterface(si);
        ulhsi.setUserLogin(ul);
        sess.save(ulhsi);
        return true;
    }

}
