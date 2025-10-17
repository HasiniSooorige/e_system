///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package Servlets;
//
//import Model.Connection.NewHibernateUtil;
//import Model.Mapping.Employee;
//import java.io.IOException;
//import java.io.PrintWriter;
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import org.hibernate.Session;
//import org.hibernate.Transaction;
//
///**
// *
// * @author Developer
// */
//@WebServlet(name = "EmployeeIsActiveStatus", urlPatterns = {"/EmployeeIsActiveStatus"})
//public class EmployeeIsActiveStatus extends HttpServlet {
//
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//
//        Session sess = NewHibernateUtil.getSessionFactory().openSession();
//        Transaction t = sess.beginTransaction();
//        String isactiveStatus = request.getParameter("valuechecked");
//        String nic = request.getParameter("nic");
//
//        Employee emp = (Employee) sess.createQuery("From Employee Where epfNo='" + nic + "'").setMaxResults(1).uniqueResult();
//    }
//
//}
