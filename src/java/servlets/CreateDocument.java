/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Model.CommonMethod.ComPath;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.DocumentType;
import Model.Mapping.Employee;
import Model.Mapping.EmployeeDocuments;
import Model.Mapping.GeneralUserProfile;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
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
 * @author kbnc
 */
@WebServlet(name = "CreateDocument", urlPatterns = {"/CreateDocument"})
public class CreateDocument extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Session sess = NewHibernateUtil.getSessionFactory().openSession();
        Transaction t = sess.beginTransaction();
        JSONObject objSend = new JSONObject();
        int stat = 200;
        String message = "";
  int idd = 0;
//        try {
//            
////            resp.getWriter().write("1");
//            
//            
////            
//            Session s = NewHibernateUtil.getSessionFactory().openSession();
//            Transaction t = s.beginTransaction();
//            EmployeeDocuments p = new EmployeeDocuments();
//            
//            DiskFileItemFactory dfi = new DiskFileItemFactory();
//            ServletFileUpload sfu = new ServletFileUpload(dfi);
//            List<FileItem> items = sfu.parseRequest(req);
//            for (FileItem i : items) {
//               
//                if (i.isFormField()) {
//                    
//                
//                }else  {
//                      
//                        
//                        String project_path=req.getServletContext().getRealPath("/");
//                        String folder_path=project_path+"Product/";
//                        System.out.println(project_path);
//                        String file_name=System.currentTimeMillis()+".png";
//                        String path=folder_path+file_name;
//                        File f=new File(path);
//                        i.write(f);
//                        p.setUrl(path);
//                    }
//            }
//            s.save(p);
//            t.commit();
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        if (isMultipart) {
            // Create a factory for disk-based file items  
            FileItemFactory factory = new DiskFileItemFactory();
            // Create a new file upload handler  
            ServletFileUpload upload = new ServletFileUpload(factory);
            try {
                // Parse the request  
                EmployeeDocuments documents = new EmployeeDocuments();
                List<EmployeeDocuments> documentslist = new ArrayList<>();
              
                List items = upload.parseRequest(request);
                Iterator iterator = items.iterator();
                while (iterator.hasNext()) {
                    FileItem item = (FileItem) iterator.next();

                    if (item.isFormField()) {
                        String itemName = System.currentTimeMillis() + "-" + item.getFieldName();

                        if (itemName.equals("id")) {
                            idd = Integer.valueOf(item.getString().replaceAll("\\D+", ""));

                            System.out.println("nic");
                            System.out.println(idd);

                        }

                    }

                    if (!item.isFormField()) {

//                             if(item.getFieldName().equals("nic_doc")){
//                                 System.out.println("file1_fff");
//                                System.out.println(item.getString());
//                                out.println("file1_fff");
//                                out.println(item.getString());
//                            }
                        String fileName = item.getName();
                        String doc_id = item.getFieldName();

                        System.out.println(doc_id);

                        String root = ComPath.getFILE_PATH();
                        File path = new File(root + "/product");
                        if (!path.exists()) {
                            boolean status = path.mkdirs();
                        }
                        File uploadedFile = new File(path + "/" + fileName);
                        System.out.println(uploadedFile.getAbsolutePath());

                        documents.setUrl(uploadedFile.getAbsolutePath());

                        try {

                            Session s = NewHibernateUtil.getSessionFactory().openSession();
                            Criteria c = s.createCriteria(DocumentType.class);
                            List<DocumentType> cat_list = c.list();
                            // String option_tags="";
                            for (DocumentType doc_type : cat_list) {
                                //    option_tags+="<option>"+doc_type.getName()+"</option>";
                                //    System.out.println(option_tags);
                                String docName = doc_type.getName();
                                if (docName.equals(doc_id)) {
                                    //option_tags+="<option>"+doc_type.getId()+"</option>";
                                    DocumentType doccs = (DocumentType) sess.load(DocumentType.class, doc_type.getId());
                                    documents.setDocumentType(doccs);
                                    System.out.println("doc_type_lD");
                                    System.out.println(doccs);
                                }

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Employee c = (Employee) sess.load(Employee.class, idd);
                        documents.setEmployee(c);

                        documentslist.add(documents);

                        System.out.println("list___");

//                            System.out.println(documentslist.size());
//                            
                        if (fileName != "") {

                            item.write(uploadedFile);

                            sess.save(documentslist.get(documentslist.size() - 1));

                            System.out.println(documentslist.size());
                            System.out.println("forloop");

                            sess.clear();

                        } else {
                            //      out.println("file not found");
                        }
                        //   out.println("<h1>File Uploaded Successfully....:-)</h1>");
                    } else {

                    }

                }

                for (int i = 0; i < documentslist.size(); i++) {

//                        }
                }
                t.commit();
                sess.close();

//                    for(String employeeDocuments : documentslist) {
//                                
//                                System.out.println("list_list_aaa_1");
//                                System.out.println(employeeDocuments.toString());
//                                documents.setUrl(employeeDocuments);
//                                 
//                                 sess.save(employeeDocuments);
//                    
//                                                    
//                                                    
//                                
//                            }
//                        
//                        t.commit();
            } catch (FileUploadException e) {
                out.println(e);
            } catch (Exception e) {
                out.println(e);
            }
        } else {
            out.println("Not Multipart");
        }
        objSend.put("status", stat);
        objSend.put("message", message);

        response.sendRedirect("admin_task/employee-document-upload.jsp?id="+idd+"&message=" + "success");
        System.out.println(objSend);

    }
}