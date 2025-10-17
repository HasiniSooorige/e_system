/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.CommonMethod.ComPath;
import Model.CommonMethod.Commons;
import Model.CommonMethod.Mail;
import Model.Connection.NewHibernateUtil;

import Model.Mapping.Employee;
import Model.Mapping.HelpTicket;
import Model.Mapping.HelpTicketRespond;
import Model.Mapping.HelpTicketRespondType;
import filteration.Filteration;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONArray;

/**
 *
 * @author Sudeera Perera
 */
@WebServlet(name = "AddTicketReply", urlPatterns = {"/AddTicketReply"})
public class AddTicketReply extends HttpServlet {

    String file_path = ComPath.getFILE_PATH() + "tickets/";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        JSONArray js = new JSONArray();
        Map m = new HashMap();
        String message = "";
        boolean success = false;
        PrintWriter out = response.getWriter();
        if ((request.getSession().getAttribute("C_GUP") != null) || (request.getSession().getAttribute("Emp_GUP") != null)) {
            Transaction t = sess.beginTransaction();
            StringBuilder sb = new StringBuilder();
            String save_as = "";
//            for (Part part : request.getParts()) {
//                String submittedFileName = part.getSubmittedFileName();
//                if (submittedFileName != null && !submittedFileName.isEmpty()) {
//                    save_as = System.currentTimeMillis() + "-" + Filteration.getFilteredFilename(submittedFileName);
//
//                    // String fileName = System.currentTimeMillis() + submittedFileName.substring(submittedFileName.lastIndexOf("."));
//                    String savePath = file_path + save_as;
//                    Files.copy(part.getInputStream(), Paths.get(savePath));
//                    sb.append(savePath).append(";");
//                }
//            }
            HelpTicketRespond htr = new HelpTicketRespond();
            String ticketId = filteration.Filteration.getFilteredUsername(request.getParameter("ticketId"));
            String replyMessage = filteration.Filteration.getFilteredUsername(request.getParameter("replyMessage"));
            String rating = filteration.Filteration.getFilteredUsername(request.getParameter("rating"));
            String responseId = filteration.Filteration.getFilteredUsername(request.getParameter("responseId"));

            Double ratingval = Double.parseDouble(rating);

            if (replyMessage.equals("") || replyMessage == null) {
                String errorStatus = "Error uploading the ticket. Reply Mesage Cannot be null! Please try again.";
                out.print(errorStatus);
            } else {

                HelpTicket ht = (HelpTicket) sess.createQuery("FROM HelpTicket WHERE id='" + ticketId + "'").uniqueResult();
                if (ht != null) {
                    if (ht.getHelpTicketStatus().getId() != 4) {
                        HelpTicketRespondType htrt = (HelpTicketRespondType) sess.createQuery("FROM HelpTicketRespondType Where id=2").uniqueResult();
                        HelpTicketRespond htrp = (HelpTicketRespond) sess.createQuery("FROM HelpTicketRespond WHERE id='" + responseId + "'").uniqueResult();
                        if (request.getSession().getAttribute("Emp_GUP") != null) {
                            String gup = request.getSession().getAttribute("Emp_GUP").toString();
                            System.out.println("gup " + request.getSession().getAttribute("Emp_GUP"));
                            Employee emp = (Employee) sess.createQuery("From Employee where generalUserProfile.id='" + Integer.parseInt(gup) + "'").setMaxResults(1).uniqueResult();
//                            htr.setEmployee(emp); //hasini
                            try {
                                htr.setRespondDate(new Date());
                                htr.setComment(replyMessage);
                                htr.setHelpTicketRespondType(htrt);
                                htr.setHelpTicket(ht);
                                htr.setHelpTicketRespond(htrp);
                                htr.setRating(ratingval);
                                htr.setDoc(save_as);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                sess.save(htr);
                                t.commit();
                                Commons cm = new Commons();
                                Mail email = new Mail();
                                Map<String, String> hashMap = new HashMap<>();
                                hashMap.put("ticketid", ht.getTicketId());
                                hashMap.put("message", replyMessage);
                                hashMap.put("link", "http://localhost:8080/exon-system/main-pages/help-ticket/admin/ticket-page.jsp");
                                email.Send(cm.ADMIN_EMAIL, "RESPONSE_ALERT", hashMap);

                                System.out.println("Ticket successfully uploaded.");
                                response.getWriter().write("Ticket successfully uploaded");
                                success = true;

                            } catch (Exception e) {
                                success = false;
                                message = "Error uploading the ticket. Please try again.";
                                e.printStackTrace();
                            }
                        }

                    } else {
                        success = false;
                        message = "Closed Ticket!";
                    }
                } else {
                    success = false;
                    message = "Something went wrong!";
                }
            }

        } else {
            response.sendRedirect("../index.jsp");
        }
        m.put("success", success);
        m.put("message", message);
        js.add(m);
        out.write(js.toJSONString());
        out.close();
        sess.close();
    }
}
