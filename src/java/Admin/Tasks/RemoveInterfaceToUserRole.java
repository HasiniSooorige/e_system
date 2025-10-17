/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Admin.Tasks;

import Model.Connection.NewHibernateUtil;
import Model.Logic.UserLoginDAO;
import Model.Mapping.UserLogin;
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
 * @author sachintha
 */
@WebServlet(name = "RemoveInterfaceToUserRole", urlPatterns = {"/RemoveInterfaceToUserRole"})
public class RemoveInterfaceToUserRole extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getSession().getAttribute("GUP_ID") != null) {

            Session sess = NewHibernateUtil.getSessionFactory().openSession();
            Transaction tx = sess.beginTransaction();
            String gup = req.getSession().getAttribute("GUP_ID").toString();
            UserLogin ul = UserLoginDAO.getByID(sess, Integer.parseInt(gup));
            if (ul.getUserRole().getId() == 1 || ul.getUserRole().getId() == 2) {
                int id = Integer.parseInt(req.getParameter("id"));
                UserRoleHasSystemInterface ur = (UserRoleHasSystemInterface) sess.createQuery("from UserRoleHasSystemInterface where id='" + id + "'").uniqueResult();
                sess.delete(ur);
                tx.commit();
            }

        } else {
            resp.sendRedirect("../index.jsp");
        }
    }

}
