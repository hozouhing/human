package com.jeff.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.jeff.DI.DI;
import com.jeff.mvc.MVC;
import com.jeff.mvc.MvcContext;

@MVC
public class UserMvc {

	@DI("userDao")
	public UserDao userDao;

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

	@MVC("testDateIn")
	public User testDateIn(Date date) {
		User user = new User();
		date = new Date();
		user.setDate(date);
		return user;
	}

	@MVC("testDateIn2")
	public Date testDateIn2() {
		try {
			MvcContext.getInstance().setUsingJsonDateFormat(
					new SimpleDateFormat("yyyy-MM-dd"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Date date = new Date();
		return date;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

}
