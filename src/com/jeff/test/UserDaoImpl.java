package com.jeff.test;

import com.jeff.DI.Resource;
import com.jeff.orm.BaseDaoJDBCImpl;

@Resource("userDao")
public class UserDaoImpl extends BaseDaoJDBCImpl<User> implements UserDao {

	private final String test1_sql = "select * from `user` where `username` = {username}";

	public String getTest1_sql() {
		return test1_sql;
	}

}
