package com.cimait.invoicec.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public final class DBDataSource {
	
	private static DBDataSource datasource; 
	private SessionFactory factory;
	private ServiceRegistry registry;
	
	private DBDataSource() {
		 try {
	            Configuration configuration = new Configuration() ;
	            configuration.configure();
	            //HH4.3
	            //registry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
	            registry = new ServiceRegistryBuilder().applySettings(
	                    configuration.getProperties()).buildServiceRegistry();
	            factory = configuration.buildSessionFactory(registry);
	        } catch (Throwable e) {
	            System.err.println(e.getMessage());
	            throw new ExceptionInInitializerError(e);
	        }
	}
	
	public static DBDataSource getInstance() {
		if (datasource == null) {
			datasource = new DBDataSource();
			return datasource;
		} else return datasource;
	}
	
	public SessionFactory  getFactory() throws SQLException {
		return this.factory;
	}
	
	public Connection getConnection() {
		System.out.println("llamada a connection null");
		return null;
	}
	
}
