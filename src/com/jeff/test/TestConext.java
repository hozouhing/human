package com.jeff.test;

import org.junit.Test;

public class TestConext {

	@Test
	public void testMvcContext() {
		String sql = "where id = {id},name={name}";
		String id = "id";
		System.out.println(sql.replaceFirst("\\{" + id + "\\}", "0"));
	}
}
