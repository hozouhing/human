package com.jeff.test;

import java.util.List;

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

	@MVC("testBaseDao1")
	public User testBaseDao(User u) {
		User user;
		// user = (User) userDao.exeSql4obj("test1", u, User.class);
		user = userDao.exeSql4obj("test1", u);
		return user;
	}

	@MVC("testBaseDao2")
	public List<User> testBaseDao2(User u) {
		List<User> users;
		// user = (User) userDao.exeSql4obj("test1", u, User.class);
		users = userDao.exeSql4list("test1", u);
		return users;
	}

	// ------------------------------------------------------------------------------------------------------------------
	public BaseDao<User> getUserDao() {
		return userDao;
	}

	public void setUserDao(BaseDao<User> userDao) {
		this.userDao = userDao;
	}
}
