package com.jeff.test;

import com.jeff.DI.Resource;
import com.jeff.orm.BaseDaoJDBCImpl;

@Resource("userDao")
public class UserDaoImpl extends BaseDaoJDBCImpl<User> {

}
