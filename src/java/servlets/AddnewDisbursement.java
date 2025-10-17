package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.Mapping.Employee;
import Model.Mapping.PettyCashApprovalLevel;
import Model.Mapping.PettyCashTransactionHistory;
import Model.Mapping.PettyCashWallet;
import Model.Mapping.TransactionType;
import Model.Mapping.UserLogin;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Ashan Kavindu
 */
@WebServlet(name = "AddnewDisbursement", urlPatterns = {"/AddnewDisbursement"})
public class AddnewDisbursement extends HttpServlet {

//    private static int uniqueIdCounter = 1000;
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String jsonData = sb.toString();
            JSONObject jsonObject = new JSONObject(jsonData);
            String employee_ID = jsonObject.getString("emp");
            String amount = jsonObject.getString("amount");
            JSONObject responseJson = new JSONObject();

            if ("Select".equals(employee_ID)) {
                responseJson.put("status", "Please Select Employee");
            } else if (amount.isEmpty()) {
                responseJson.put("status", "Please Add amount");
            } else if (!amount.matches("^\\d+(\\.\\d{1,2})?$")) {
                responseJson.put("status", "Invalid amount");
            } else {
                Session session = NewHibernateUtil.getSessionFactory().openSession();
                Transaction tr = session.beginTransaction();

                String hql = "FROM PettyCashWallet WHERE employee_id='" + employee_ID + "'";
                PettyCashWallet pettyCashWallet = (PettyCashWallet) session.createQuery(hql).setMaxResults(1).uniqueResult();
                double getamount = Double.parseDouble(amount);
                UserLogin login = (UserLogin) req.getSession().getAttribute("user");
                PettyCashWallet balanceRec = (PettyCashWallet) session.createQuery("from PettyCashWallet u where employee_id='" + login.getId() + "'").uniqueResult();
//                System.out.println(balanceRec.getAmount() - getamount);
                if (balanceRec.getAmount() >= getamount) {
                    if (pettyCashWallet != null) {
                        getamount += pettyCashWallet.getAmount(); // Update the amount
                        pettyCashWallet.setAmount(getamount);
                        pettyCashWallet.setUpdatedDate(new Date());
                        session.update(pettyCashWallet);
                        responseJson.put("status", "Available allocations were successfully updated");
                    } else {
                        pettyCashWallet = new PettyCashWallet();
//                        pettyCashWallet.setId(uniqueIdCounter++);
                        pettyCashWallet.setAmount(getamount);
                        pettyCashWallet.setUpdatedDate(new Date());
                        pettyCashWallet.setEmployee((Employee) session.createQuery("FROM  Employee WHERE id='" + employee_ID + "'").setMaxResults(1).uniqueResult());
                        session.save(pettyCashWallet);

                        responseJson.put("status", "Added new Disbursement");
                    }
                    balanceRec.setAmount(balanceRec.getAmount() - Double.parseDouble(amount));
                    balanceRec.setUpdatedDate(new Date());
                    session.update(balanceRec);

                    tr.commit();
                    session.close();
                } else {
                    responseJson.put("status", "You cannot send Disbursement because Exon's Available Balance is insufficient");
                }
            }

            resp.getWriter().write(responseJson.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
