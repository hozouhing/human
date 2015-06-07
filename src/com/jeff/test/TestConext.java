package com.jeff.test;

import org.junit.Test;

import com.jeff.DI.DIContext;

public class TestConext {

	@Test
	public void testMvcContext() {
		try {
			DIContext diContext = DIContext.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
