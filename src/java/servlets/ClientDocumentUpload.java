/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.CommonMethod.ComPath;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.Country;
import Model.Mapping.GeneralOrganizationProfile;
import Model.Mapping.OrganizationType;
import Model.Mapping.ProjectGopAgreement;
import Model.Mapping.Projects;
import static Servlets.CreateClientDetails.convertStringToDate;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.json.simple.JSONObject;

/**
 *
 * @author HP
 */
@WebServlet(name = "ClientDocumentUpload", urlPatterns = {"/ClientDocumentUpload"})
public class ClientDocumentUpload extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("____________Client  Document  Upload_____________");

        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int stat = 200;
        String message = "";
        String projectId = "";
        int ProId = 0;

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if (isMultipart) {
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);

            try {
                GeneralOrganizationProfile documents = new GeneralOrganizationProfile();
                List<GeneralOrganizationProfile> documentslist = new ArrayList<>();

                List items = upload.parseRequest(request);
                Iterator iterator = items.iterator();
                while (iterator.hasNext()) {
                    FileItem item = (FileItem) iterator.next();

                    if (item.isFormField()) {
                        String itemName = item.getFieldName();
                        if (itemName.equals("rowId")) {
                            projectId = item.getString();
                            System.out.println("project Id String Type : " + projectId);
                            ProId = Integer.parseInt(projectId);
                            System.out.println("Pro Id Int Type  :  " + ProId);
                        }
                    }

                    if (!item.isFormField()) {

                        String fileName = item.getName();
                        String doc_id = item.getFieldName();

                        System.out.println(doc_id);

                        String root = ComPath.getFILE_PATH();
                        File path = new File(root + "/ClientAgreements");
                        if (!path.exists()) {
                            boolean status = path.mkdirs();
                        }
                        File uploadedFile = new File(path + "/" + fileName);

                        System.out.println(uploadedFile.getAbsolutePath());

                        documentslist.add(documents);

                        System.out.println("list___");

                        if (fileName != "") {
                            item.write(uploadedFile);
                            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

                            Projects proDoc = (Projects) sess.createQuery("From Projects Where id='" + ProId + "'").setMaxResults(1).uniqueResult();

                            ProjectGopAgreement pga = new ProjectGopAgreement();

                            pga.setProjects(proDoc);
                            pga.setGeneralOrganizationProfile(proDoc.getGeneralOrganizationProfile());
                            pga.setDocument(fileName);
                            pga.setUploadedDate(convertStringToDate(timeStamp));

                            sess.save(pga);

                            t.commit();

                            stat = 200;
                            message = "Client Agreement Documents Successfully Uploaded !";

                            objSend.put("status", stat);
                            objSend.put("ClientSuccess", message);
                            System.out.println(objSend);
                            response.sendRedirect("projects/project-list.jsp?ClientSuccess=" + "success");
                        } else {
                            stat = 400;
                            message = "No file selected for upload. Please choose a file to upload before proceeding !";

                            objSend.put("status", stat);
                            objSend.put("DocUploadError", message);
                            response.sendRedirect("projects/project-list.jsp?DocUploadError=" + "error");
                        }

                    } else {
                    }

                }

                sess.close();

            } catch (FileUploadException e) {
                stat = 400;
                message = "Fill All Fields !";

                objSend.put("status", stat);
                objSend.put("ClientError", message);
                response.sendRedirect("projects/project-list.jsp?ClientError=" + "error");
                out.println(e);
            } catch (Exception e) {
                out.println(e);
            }
        } else {
            out.println("Not Multipart");
        }

    }

    public static Date convertStringToDate(String date) {
        if (date != null) {
            try {
                return FORMATTER.parse(date);
            } catch (ParseException e) {
                // nothing we can do if the input is invalid
                throw new RuntimeException(e);
            }
        }
        return null;
    }

}
