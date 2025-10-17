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
import Model.Mapping.PettyCashWallet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONObject;
import org.json.simple.JSONArray;

/**
 *
 * @author Ashan Kavindu
 */
@WebServlet(name = "loadViewAllAttachmentsModel", urlPatterns = {"/loadViewAllAttachmentsModel"})
public class loadViewAllAttachmentsModel extends HttpServlet {

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
            int id = jsonObject.getInt("id");
            JSONObject responseJson = new JSONObject();
            Session session = NewHibernateUtil.getSessionFactory().openSession();
            Transaction tr = session.beginTransaction();
            List<PettyCashTransactionHistoryDocument> documents = (List<PettyCashTransactionHistoryDocument>) session.createQuery("from PettyCashTransactionHistoryDocument where petty_cash_transaction_history_id='" + id + "'").list();
            JSONArray documentsArray = new JSONArray();
            double totAmount = 0;
//            JSONObject documentInfoObject = new JSONObject();
            int i = 0;
            for (PettyCashTransactionHistoryDocument document : documents) {
                i += 1;
//                totAmount += document.getAmount();
                JSONObject documentObject = new JSONObject();
                documentObject.put("amount", document.getAmount());
                documentObject.put("doc", document.getDoc());
                documentObject.put("totAmount", document.getAmount());
                documentObject.put("expense_description", document.getExpenseDescription());
                documentObject.put("numberOfAttachments", documents.size());
                documentsArray.add(documentObject);
            }
//            documentInfoObject.put("totAmount", totAmount);

//            documentsArray.add(documentInfoObject);
            responseJson.put("documents", documentsArray);
            tr.commit();
            session.close();
            resp.getWriter().write(responseJson.toString());
            System.out.println(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
