/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test_safe;

import Com.Tools.Security;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kbnc
 */
public class NewClass {

    public static void main(String[] args) {
        try {
            System.out.println(Security.decrypt("m43auvEsBS71u7laAsZI8w=="));
            System.out.println(Security.encrypt("Exon@1234"));

            System.out.println("");

//            Sudeera
            System.out.println(Security.decrypt("SQU28CNpsr8YP1m3Jzh0PQ=="));
            System.out.println(Security.encrypt("Sudeera@1234"));

            System.out.println("");

//            Podi Thilan
            System.out.println(Security.decrypt("LYRJ+9XQ8AO/OG5TEjuPKg=="));
            System.out.println(Security.encrypt("ThilanExon@1234"));

            System.out.println("");

//            Tharusha
            System.out.println(Security.decrypt("zUjWiDC0HB51fQfMCXEXxA=="));
            System.out.println(Security.encrypt("Tharusha@1234"));

            System.out.println("");

//            Gimhan
            System.out.println(Security.decrypt("RbjdEGYPVxdhi0sngePfMg=="));
            System.out.println(Security.encrypt("Gimhan@1234"));

            System.out.println("");

//            Kanishka
            System.out.println(Security.decrypt("G2/d2ggYEStn7yDMeXFIsQ=="));
            System.out.println(Security.encrypt("Kanishka@1234"));

            System.out.println("");

//            Ashan
            System.out.println(Security.decrypt("su5tirKCNfW/4xPyrn6UHg=="));
            System.out.println(Security.encrypt("Ashan@1234"));

            System.out.println("");

//            Isuru
            System.out.println(Security.decrypt("Un2Gx7ViMm3g0Fetmd6CVw=="));
            System.out.println(Security.encrypt("Isuru@1234"));

            System.out.println("");

//            Bihanga
            System.out.println(Security.decrypt("NaOI3NJML0a6vHhZUNDx+A=="));
            System.out.println(Security.encrypt("cwcwbwub"));

            System.out.println("");

//            Nilupul
            System.out.println(Security.decrypt("m43auvEsBS71u7laAsZI8w=="));
            System.out.println(Security.encrypt("Exon@1234"));

            System.out.println("");

//            Exon_Employee
            System.out.println(Security.decrypt("omTf7LLNuK37yQG+Xm8nfw=="));
            System.out.println(Security.encrypt("exon_employee"));

            System.out.println("");

//            Exon_Client
            System.out.println(Security.decrypt("8ZzyelTC6d8+mbdQkW5JTw=="));
            System.out.println(Security.encrypt("exon_client"));

            System.out.println("");

        } catch (GeneralSecurityException ex) {
            Logger.getLogger(NewClass.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NewClass.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
