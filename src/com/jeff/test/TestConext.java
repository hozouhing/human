package com.jeff.test;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class TestConext {

	@Test
	public void testMvcContext() {
		Set<String> t = new HashSet<String>();
		t.add("test");
		String t2 = "test";
		System.out.println(t.contains(t2));
	}
}
