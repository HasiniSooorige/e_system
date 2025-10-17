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
import Model.Mapping.SystemInterface;
import Model.Mapping.UserLogin;
import Model.Mapping.UserRole;
import Model.Mapping.UserRoleHasSystemInterface;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author sachintha
 */
@WebServlet(name = "AddInterfacesToUserRoles", urlPatterns = {"/AddInterfacesToUserRoles"})
public class AddInterfacesToUserRoles extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getSession().getAttribute("GUP_ID") != null) {
            PrintWriter out = resp.getWriter();
            Session sess = NewHibernateUtil.getSessionFactory().openSession();
            Transaction tr = sess.beginTransaction();
            JSONObject objSend = new JSONObject();
            boolean success = false;
            String message = "";
            try {
                String gup = req.getSession().getAttribute("GUP_ID").toString();
                UserLogin ul = UserLoginDAO.getByID(sess, Integer.parseInt(gup));
                if (ul.getUserRole().getId() == 1 || ul.getUserRole().getId() == 2) {
                    
                    String jsn_interfaces = req.getParameter("jsnobj_interface").trim();
                    JSONParser parser_interfaces = new JSONParser();
                    Object obj_interfaces = parser_interfaces.parse(jsn_interfaces);
                    JSONObject job_interfaces = (JSONObject) obj_interfaces;
                    JSONArray jsnarr_interfaces = (JSONArray) job_interfaces.get("jsn");

                    String jsn_role = req.getParameter("jsnobj_role").trim();
                    JSONParser parser_role = new JSONParser();
                    Object obj_role = parser_role.parse(jsn_role);
                    JSONObject job_role = (JSONObject) obj_role;
                    JSONArray jsnarr_role = (JSONArray) job_role.get("jsn");

                    for (Iterator it1 = jsnarr_interfaces.iterator(); it1.hasNext();) { //read interfaces
                        JSONObject json_object1 = (JSONObject) it1.next();
                        int interface_id = Integer.parseInt(json_object1.get("id").toString().trim());
                        System.out.println("interface_id: " + interface_id);
                        SystemInterface si = SystemInterfaceDAO.getByID(sess, interface_id);

                        for (Iterator it2 = jsnarr_role.iterator(); it2.hasNext();) { //read userRoles
                            JSONObject json_object2 = (JSONObject) it2.next();
                            int user_role_id = Integer.parseInt(json_object2.get("id").toString().trim());
                            System.out.println("user_role_id: " + user_role_id);

                            UserRole ur = UserRoleDAO.getByID(sess, user_role_id);

                            UserRoleHasSystemInterface urhsi = (UserRoleHasSystemInterface) sess.createQuery("from UserRoleHasSystemInterface where systemInterface.siId='" + si.getSiId() + "' AND userRole.id='" + ur.getId() + "'").uniqueResult();

                            if (urhsi == null) {
                                urhsi = new UserRoleHasSystemInterface();
                                urhsi.setSystemInterface(si);
                                urhsi.setUserRole(ur);
                                sess.save(urhsi);
                                success = true;
                                message = "Successfully Assigned!";
                            } else {
                                message = "Already Assign " + si.getDisplayName() + " interface  to " + ur.getName() + " User Role";
                                break;
                            }
                        }
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
                message = e.getMessage();
            }
            objSend.put("success", success);
            objSend.put("message", message);

            System.out.println(objSend.toString());

            resp.setContentType("text/json;charset=UTF-8");
            try {
                out.print(objSend);
            } finally {
                out.close();
            }

            tr.commit();
            sess.clear();
            sess.close();

        } else {
            resp.sendRedirect("../index.jsp");
        }
    }

}
