package com.jeff.orm;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class OrmFilter implements Filter {

	OrmContext ormContext;

	@Override
	public void destroy() {
		ormContext.release();
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain chain) throws IOException, ServletException {
		//doFilter(arg0, arg1, arg2);
		ormContext = OrmContext.getInstance();
		ormContext.push();
		chain.doFilter(arg0, arg1);
		ormContext.pop();
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}

}
