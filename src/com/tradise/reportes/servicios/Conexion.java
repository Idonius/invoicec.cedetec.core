 package com.tradise.reportes.servicios;
 
 import java.io.IOException;
import java.sql.Connection;
 import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.swing.JOptionPane;

import com.sun.database.ConexionBase;
 
 public class Conexion
 {
   public static Connection conectar() throws SQLException, IOException, NamingException, ClassNotFoundException
   {
     Connection con = null;
       return ConexionBase.getConexionPostgres();
   }
 }
