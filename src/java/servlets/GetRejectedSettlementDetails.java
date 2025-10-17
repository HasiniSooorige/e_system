/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.Connection.NewHibernateUtil;
import Model.Mapping.PettyCashApprovalLevel;
import Model.Mapping.PettyCashTransactionHistory;
import Model.Mapping.PettyCashTransactionHistoryDocument;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
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
@WebServlet(name = "GetRejectedSettlementDetails", urlPatterns = {"/GetRejectedSettlementDetails"})
public class GetRejectedSettlementDetails extends HttpServlet {

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
            int settlementID = jsonObject.getInt("settlementID");
            System.out.println("/////////////////" + settlementID);
            JSONObject responseJson = new JSONObject();
            if (String.valueOf(settlementID).trim().isEmpty()) {
                responseJson.put("status", "Invalid Request");
            } else {
                Session session = NewHibernateUtil.getSessionFactory().openSession();
                Transaction tr = session.beginTransaction();
                PettyCashTransactionHistory PettyCashTransactionHistory = (PettyCashTransactionHistory) session.createQuery("FROM PettyCashTransactionHistory WHERE id='" + settlementID + "'and petty_cash_approval_level_id =3").setMaxResults(1).uniqueResult();
                List<PettyCashTransactionHistoryDocument> pettyCashTransactionHistoryDocument = (List<PettyCashTransactionHistoryDocument>) session.createQuery("FROM PettyCashTransactionHistoryDocument WHERE petty_cash_transaction_history_id='" + settlementID + "'").list();
                List<JSONObject> jsonArrayData = new ArrayList<>();
                JSONObject dataObject;
                for (PettyCashTransactionHistoryDocument document : pettyCashTransactionHistoryDocument) {
                    dataObject = new JSONObject();
                    dataObject.put("img", document.getDoc());
                    dataObject.put("exDescription", document.getExpenseDescription());
                    dataObject.put("amount", document.getAmount());
                    jsonArrayData.add(dataObject);
                }
                dataObject = new JSONObject();
                dataObject.put("description", PettyCashTransactionHistory.getDescription());
                dataObject.put("MainAmount", PettyCashTransactionHistory.getAmount());
                JSONArray jsonArray = new JSONArray(jsonArrayData);
                jsonArrayData.add(dataObject);
                responseJson.put("data", jsonArray);
                tr.commit();
                session.close();
            }
            resp.getWriter().write(responseJson.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
