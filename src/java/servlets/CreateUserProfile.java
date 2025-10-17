package Servlets;

import Com.Tools.Security;
import Model.CommonMethod.Com;
import Model.Mapping.Country;
import Model.Mapping.GeneralUserProfile;
import Model.Connection.NewHibernateUtil;
import Model.Mapping.Designation;
import Model.Mapping.Employee;
import Model.Mapping.Gender;
import Model.Mapping.GeneralOrganizationProfile;
import Model.Mapping.UserLogin;
import Model.Mapping.UserRole;
import Model.Mapping.WorkHistory;
import Utilities.PassGen;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;
import Model.CommonMethod.Commons;

@WebServlet(name = "CreateUserProfile", urlPatterns = {"/CreateUserProfile"})

public class CreateUserProfile extends HttpServlet {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Create User Profile ...");
        if (!(req.getSession().getAttribute("GUP_ID") == null)) {

            Commons commonsInstance = new Commons();
            String admin_email = commonsInstance.ADMIN_EMAIL;
            String admin_password = commonsInstance.ADMIN_PASSWORD;

            Session sess = NewHibernateUtil.getSessionFactory().openSession();
            Transaction t = sess.beginTransaction();
            JSONObject objSend = new JSONObject();
            int status = 200;
            String message = "";

            try {

                String epf_no = req.getParameter("epf_no");
                String organization_by = req.getParameter("organization_by");
                Integer OrgId = Integer.parseInt(organization_by);
                String designation = req.getParameter("designation");
                String nic = req.getParameter("nic");
                String gender = req.getParameter("gender");
                String first_name = req.getParameter("first_name");
                String last_name = req.getParameter("last_name");
                String dob = req.getParameter("dob");
                String address1 = req.getParameter("address1");
                String address2 = req.getParameter("address2");
                String address3 = req.getParameter("address3");
                String mobile_no = req.getParameter("mobile_no");
                String home_no = req.getParameter("home_no");
                String email = req.getParameter("email");
                String country_id = req.getParameter("country_id");
                String emergency_no = req.getParameter("emergencyContact");

                System.out.println(epf_no + "," + OrgId + "," + designation + "," + nic + "," + gender + "," + first_name + "," + last_name + "," + dob + "," + email + "," + mobile_no + "," + home_no + "," + emergency_no + "," + address1 + "," + address2 + "," + address3 + "," + country_id);

                GeneralUserProfile gup = (GeneralUserProfile) sess.createQuery("From GeneralUserProfile Where nic='" + nic + "' or email='" + email + "'").setMaxResults(1).uniqueResult();
                if (gup == null) {

                    Employee emp = null;
                    if ("".equals(epf_no)) {
                        System.out.println("Epf No Null User!");
                    } else {
                        emp = (Employee) sess.createQuery("From Employee Where epfNo='" + epf_no + "'").setMaxResults(1).uniqueResult();
                    }
                    if (emp == null) {

                        UserLogin userlogin = (UserLogin) sess.createQuery("From UserLogin Where username='" + nic + "'").setMaxResults(1).uniqueResult();
                        if (userlogin == null) {

                            gup = new GeneralUserProfile();
                            gup.setNic(nic);
                            gup.setFirstName(first_name);
                            gup.setLastName(last_name);
                            gup.setAddress1(address1);
                            gup.setAddress2(address2);
                            gup.setAddress3(address3);
                            gup.setMobileNo(mobile_no);
                            gup.setHomeNo(home_no);
                            gup.setEmail(email);
                            gup.setProfileCreatedDate(convertStringToDate(timeStamp));
                            gup.setCountry((Country) sess.load(Country.class, Integer.parseInt(country_id)));
                            gup.setGender((Gender) sess.load(Gender.class, Integer.parseInt(gender)));
                            gup.setEmergencyContactNo(emergency_no);
                            gup.setDob(Com.getFormattedDate(dob));

                            String password = new PassGen().generate(PassGen.getValid("[a-z]", Character.MAX_VALUE), 8);

                            userlogin = new UserLogin();
                            userlogin.setUsername(nic);
                            userlogin.setPassword(Security.encrypt(password));
                            userlogin.setMaxLoginAttemp(3);
                            userlogin.setCountAttempt(0);
                            userlogin.setIsActive(true);
                            userlogin.setGeneralUserProfile(gup);
                            userlogin.setUserRole((UserRole) sess.load(UserRole.class, 3));

                            emp = new Employee();
                            emp.setRegisteredDate(convertStringToDate(timeStamp));
                            emp.setEpfNo(epf_no);
                            emp.setGeneralUserProfile(gup);
                            emp.setGeneralOrganizationProfile((GeneralOrganizationProfile) sess.load(GeneralOrganizationProfile.class, OrgId));
                            emp.setDesignation((Designation) sess.load(Designation.class, Integer.parseInt(designation)));
                            emp.setIsActive(true);

                            WorkHistory history = new WorkHistory();
                            history.setDateFrom(convertStringToDate(timeStamp));
                            history.setDateTo(convertStringToDate(timeStamp));
                            history.setDesignationByDesignationFrom((Designation) sess.load(Designation.class, Integer.parseInt(designation)));
                            history.setDesignationByDesignationTo((Designation) sess.load(Designation.class, Integer.parseInt(designation)));
                            history.setEmployee(emp);

                            sess.save(gup);
                            sess.save(userlogin);
                            sess.save(emp);
                            sess.save(history);

                            t.commit();

//                            email START
                            Properties props = new Properties();
                            props.put("mail.smtp.auth", true);
                            props.put("mail.smtp.starttls.enable", true);
                            props.put("mail.smtp.host", "smtp.gmail.com");
                            props.put("mail.smtp.port", "587");
                            javax.mail.Session session = javax.mail.Session.getInstance(props,
                                    new javax.mail.Authenticator() {
                                @Override
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication(admin_email, admin_password);
                                }
                            });
                            try {
                                Message message_1 = new MimeMessage(session);
                                message_1.setFrom(new InternetAddress("Exon"));
                                message_1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                                MimeBodyPart textPart = new MimeBodyPart();
                                Multipart multipart = new MimeMultipart();
                                String link = "<a href=\"http://system.exon.lk\" style=\"color:#6666CC;\" target=\"_blank\"> <strong><em>Exon Management System</em></strong></a>";

                                String final_Text = "<div style=\"text-align:justify; \">\n"
                                        + "        Dear " + first_name + " " + last_name + ",<br>"
                                        + "<p>We are thrilled to welcome you to the Exon Software Solutions (Pvt) Ltd team! Your journey with us marks the beginning of an exciting chapter, and we are eager to have you on board.</p>\n"
                                        + "<p>As part of our commitment to providing a seamless onboarding experience, we are pleased to provide you with your login credentials to access our system. Please  find your login details below:</p>\n"
                                        + "Username&nbsp;:&nbsp;&nbsp;" + nic + "<br>\n"
                                        + "Password&nbsp;&nbsp;:&nbsp;&nbsp;" + password + "<br><br>\n"
                                        + "Visit our login page&nbsp;:&nbsp;&nbsp;" + link + "<br>\n"
                                        + "<p>If you encounter any issues while logging in or have any questions about your onboarding process, feel free to reach out to our HR team.</p>\n"
                                        + "<p>We look forward to achieving great milestones together and wish you a successful and rewarding career with us.</p>\n"
                                        + "<p>Best Regards,</p>\n"
                                        + "<p>Exon Software Solutions (Pvt) Ltd</p>\n"
                                        + "</div>\n"
                                        + "";
                                textPart.setText(final_Text);
                                multipart.addBodyPart(textPart);
                                message_1.setContent(final_Text, "text/html");
                                message_1.setSubject("Welcome to Exon Software Solutions - Your Login Credentials");
                                Transport.send(message_1);
                                System.out.println("emailsending");
                            } catch (Exception e) {
                                message = "Somthing went wrong!";
                            }
//emal END

                            status = 200;
                            message = "Data Successfully Saved";

                            sess.flush();
                            sess.clear();

                        } else {

                            status = 400;
                            System.out.println("UserName Already Used");
                            message = "UserName Already Used";

                        }

                    } else {
                        status = 400;
                        System.out.println("EPF NO Already Used");
                        message = "EPF NO Already Used";
                    }

                } else {

                    gup.setNic(nic);
                    gup.setFirstName(first_name);
                    gup.setLastName(last_name);
                    gup.setAddress1(address1);
                    gup.setAddress2(address2);
                    gup.setAddress3(address3);
                    gup.setMobileNo(mobile_no);
                    gup.setHomeNo(home_no);
                    gup.setEmail(email);
                    gup.setProfileCreatedDate(convertStringToDate(timeStamp));
                    gup.setCountry((Country) sess.load(Country.class, Integer.parseInt(country_id)));
                    gup.setGender((Gender) sess.load(Gender.class, Integer.parseInt(gender)));
                    gup.setEmergencyContactNo(emergency_no);
                    gup.setDob(Com.getFormattedDate(dob));

                    sess.update(gup);

                    Integer gupId = gup.getId();

                    Employee empGupTest = (Employee) sess.createQuery("From Employee Where general_user_profile_id='" + gupId + "'").setMaxResults(1).uniqueResult();

                    if (empGupTest == null) {
                        Employee emp = null;
                        if ("".equals(epf_no)) {
                            System.out.println("Epf No Null User!");
                        } else {
                            emp = (Employee) sess.createQuery("From Employee Where epfNo='" + epf_no + "'").setMaxResults(1).uniqueResult();
                        }
                        if (emp == null) {

                            UserLogin userlogin = (UserLogin) sess.createQuery("From UserLogin Where username='" + nic + "'").setMaxResults(1).uniqueResult();
                            if (userlogin == null) {

                                String password = new PassGen().generate(PassGen.getValid("[a-z]", Character.MAX_VALUE), 8);

                                userlogin = new UserLogin();
                                userlogin.setUsername(nic);
                                userlogin.setPassword(Security.encrypt(password));
                                userlogin.setMaxLoginAttemp(3);
                                userlogin.setCountAttempt(0);
                                userlogin.setIsActive(true);
                                userlogin.setGeneralUserProfile(gup);
                                userlogin.setUserRole((UserRole) sess.load(UserRole.class, 3));

                                emp = new Employee();
                                emp.setRegisteredDate(convertStringToDate(timeStamp));
                                emp.setEpfNo(epf_no);
                                emp.setGeneralUserProfile(gup);
                                emp.setGeneralOrganizationProfile((GeneralOrganizationProfile) sess.load(GeneralOrganizationProfile.class, OrgId));
                                emp.setDesignation((Designation) sess.load(Designation.class, Integer.parseInt(designation)));
                                emp.setIsActive(true);

                                WorkHistory history = new WorkHistory();
                                history.setDateFrom(convertStringToDate(timeStamp));
                                history.setDateTo(convertStringToDate(timeStamp));
                                history.setDesignationByDesignationFrom((Designation) sess.load(Designation.class, Integer.parseInt(designation)));
                                history.setDesignationByDesignationTo((Designation) sess.load(Designation.class, Integer.parseInt(designation)));
                                history.setEmployee(emp);

                                sess.save(userlogin);
                                sess.save(emp);
                                sess.save(history);

//                            email START
                                Properties props = new Properties();
                                props.put("mail.smtp.auth", true);
                                props.put("mail.smtp.starttls.enable", true);
                                props.put("mail.smtp.host", "smtp.gmail.com");
                                props.put("mail.smtp.port", "587");
                                javax.mail.Session session = javax.mail.Session.getInstance(props,
                                        new javax.mail.Authenticator() {
                                    @Override
                                    protected PasswordAuthentication getPasswordAuthentication() {
                                        return new PasswordAuthentication(admin_email, admin_password);
                                    }
                                });
                                try {
                                    Message message_1 = new MimeMessage(session);
                                    message_1.setFrom(new InternetAddress("Exon"));
                                    message_1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                                    MimeBodyPart textPart = new MimeBodyPart();
                                    Multipart multipart = new MimeMultipart();
                                    String link = "<a href=\"http://system.exon.lk\" style=\"color:#6666CC;\" target=\"_blank\"> <strong><em>Exon Management System</em></strong></a>";

                                    String final_Text = "<div style=\"text-align:justify; \">\n"
                                            + "        Dear " + first_name + " " + last_name + ",<br>"
                                            + "<p>We are thrilled to welcome you to the Exon Software Solutions (Pvt) Ltd team! Your journey with us marks the beginning of an exciting chapter, and we are eager to have you on board.</p>\n"
                                            + "<p>As part of our commitment to providing a seamless onboarding experience, we are pleased to provide you with your login credentials to access our system. Please  find your login details below:</p>\n"
                                            + "Username&nbsp;:&nbsp;&nbsp;" + nic + "<br>\n"
                                            + "Password&nbsp;&nbsp;:&nbsp;&nbsp;" + password + "<br><br>\n"
                                            + "Visit our login page&nbsp;:&nbsp;&nbsp;" + link + "<br>\n"
                                            + "<p>If you encounter any issues while logging in or have any questions about your onboarding process, feel free to reach out to our HR team.</p>\n"
                                            + "<p>We look forward to achieving great milestones together and wish you a successful and rewarding career with us.</p>\n"
                                            + "<p>Best Regards,</p>\n"
                                            + "<p>Exon Software Solutions (Pvt) Ltd</p>\n"
                                            + "</div>\n"
                                            + "";
                                    textPart.setText(final_Text);
                                    multipart.addBodyPart(textPart);
                                    message_1.setContent(final_Text, "text/html");
                                    message_1.setSubject("Welcome to Exon Software Solutions - Your Login Credentials");
                                    Transport.send(message_1);
                                    System.out.println("emailsending");
                                } catch (Exception e) {
                                    message = "Somthing went wrong!";
                                }
//emal END

                                status = 200;
                                message = "Data Successfully Saved";

                            } else {

                                status = 400;
                                System.out.println("UserName Already Used");
                                message = "UserName Already Used";

                            }

                        } else {
                            status = 400;
                            System.out.println("EPF NO Already Used");
                            message = "EPF NO Already Used";
                        }

                    } else {
                        System.out.println("Employee Table, User Login Table & Work History Table already Exist!");
                    }

                    t.commit();
                    status = 200;
                    message = "Data Successfully Saved";
                    sess.flush();
                    sess.clear();

                }
                objSend.put("status", status);
                objSend.put("message", message);
                resp.getWriter().print(objSend);
                System.out.println(objSend);
            } catch (Exception e) {
                status = 500;
                message = "Error Occurred";
                e.printStackTrace();
                objSend.put("status", status);
                objSend.put("message", message);
                resp.getWriter().print(objSend);
                System.out.println(objSend);
            } finally {
                sess.close();

            }
        } else {
            resp.sendRedirect("../index.jsp");
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
