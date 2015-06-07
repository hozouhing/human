package com.jeff.mvc;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.jeff.orm.OrmContext;

public class MyServletContext implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	}

	@Override
	public void contextInitialized(ServletContextEvent context) {
		MvcContext mvcContext = null;
		OrmContext ormContext = null;
		try {
			ormContext = OrmContext.getInstance();
			mvcContext = MvcContext.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		context.getServletContext().setAttribute("mvcContext", mvcContext);
		context.getServletContext().setAttribute("ormContext", ormContext);
	}

}
