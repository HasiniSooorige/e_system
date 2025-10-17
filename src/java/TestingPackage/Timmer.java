/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestingPackage;

import Model.Connection.NewHibernateUtil;
import Model.Mapping.Country;
import java.sql.ResultSet;
import java.util.Timer;
import java.util.TimerTask;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.hibernate.Session;

/**
 *
 * @author sachintha
 */
public class Timmer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                   System.out.println("Exon System - Calling Timmer...");
                    Session sess = NewHibernateUtil.getSessionFactory().openSession();
                    Country at = (Country) sess.createQuery("from Country where id=1").setMaxResults(1).uniqueResult();
                    sess.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        timer.scheduleAtFixedRate(task, 0, 1000*60*60);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
