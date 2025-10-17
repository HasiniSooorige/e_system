/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.CommonMethod.ComPath;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author HP
 */
@WebServlet(name = "HelpTicketDocumentOpen", urlPatterns = {"/HelpTicketDocumentOpen"})
public class HelpTicketDocumentOpen extends HttpServlet {

    private static final String root = ComPath.getFILE_PATH();
    private static final String FILE_DIRECTORY = root + "/HelpTickets";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fileName = request.getParameter("fileName");

        String filePath = FILE_DIRECTORY + File.separator + fileName;

        File file = new File(filePath);

        System.out.println(fileName);
        System.out.println(filePath);
        System.out.println(file);

        if (file.exists()) {
            InputStream in = new FileInputStream(file);
            OutputStream out = response.getOutputStream();

            response.setContentType(getContentType(fileName));
            response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");

            IOUtils.copy(in, out);
            in.close();
            out.close();
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            return "application/msword";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else {
            return "application/octet-stream";
        }

    }

}
