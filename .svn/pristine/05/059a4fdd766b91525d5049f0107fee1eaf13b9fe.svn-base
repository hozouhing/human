package com.jeff.test;

import javax.servlet.http.HttpServletRequest;

import com.jeff.DI.DI;
import com.jeff.mvc.MVC;
import com.jeff.orm.BaseDao;

@MVC
public class UserMvc {

	@DI("userDao")
	public BaseDao<User> userDao;

	@MVC("addUser")
	public User addUser(User user) {
		userDao.add(user);
		return user;
	}

	@MVC("getUser")
	public User getUser() {
		User user = userDao.getObject("select * from `user` where id = 1");
		return user;
	}

	@MVC("testForward")
	public String testForward(HttpServletRequest request) {
		System.out.println(request.getContextPath());
		return "/test1.jsp";
	}

	@MVC("testRedirect")
	public String testRedirect(HttpServletRequest request) {
		String page = "redirect:/test1.jsp";
		return page;
	}

	// ------------------------------------------------------------------------------------------------------------------
	public BaseDao<User> getUserDao() {
		return userDao;
	}

	public void setUserDao(BaseDao<User> userDao) {
		this.userDao = userDao;
	}
}
