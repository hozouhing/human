package com.jeff.mvc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ReqDateFormat {

	// pattern
	String value() default "yyyy-mm-dd hh:mm:ss";
}
