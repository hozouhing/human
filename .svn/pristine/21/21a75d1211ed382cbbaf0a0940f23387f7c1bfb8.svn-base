package com.jeff.test;

import javax.servlet.http.HttpServletRequest;

import com.jeff.DI.DI;
import com.jeff.mvc.MVC;
import com.jeff.orm.OrmContext;

@MVC
public class MVC1 {
	@DI("resource1")
	public Resource1 resource1;

	@MVC("test1")
	public void test1(User user, String arg) {
		resource1.test();
	}

	@MVC("test3")
	public User test3(HttpServletRequest request, User user, String arg) {
		System.out.println(OrmContext.getCurrentConnection()==null);
		return user;
	}

	public Resource1 getResource1() {
		return resource1;
	}

	public void setResource1(Resource1 resource2) {
		this.resource1 = resource2;
	}

}
