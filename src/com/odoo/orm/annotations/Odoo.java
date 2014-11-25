/*
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * 
 */
package com.odoo.orm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Interface Odoo.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Odoo {

	/**
	 * The Interface Functional.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Functional {

		/**
		 * Method.
		 * 
		 * @return the string
		 */
		String method() default "";

		/**
		 * If true, system create column for this functional field and store
		 * value (on create and update) given by this function
		 * 
		 * @return true, if successful
		 */
		boolean store() default false;

		/**
		 * Depends.
		 * 
		 * @return the string[]
		 */
		String[] depends() default {};

		/**
		 * Check row id.
		 * 
		 * @return true, if successful
		 */
		boolean checkRowId() default true;
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface api {

		@Retention(RetentionPolicy.RUNTIME)
		@Target({ ElementType.FIELD, ElementType.METHOD })
		public @interface v7 {
			String[] versions() default {};

			String[] exclude() default {};
		}

		@Retention(RetentionPolicy.RUNTIME)
		@Target({ ElementType.FIELD, ElementType.METHOD })
		public @interface v8 {
			String[] versions() default {};

			String[] exclude() default {};
		}

		@Retention(RetentionPolicy.RUNTIME)
		@Target({ ElementType.FIELD, ElementType.METHOD })
		public @interface v9alpha {
			String[] versions() default {};

			String[] exclude() default {};
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD, ElementType.METHOD })
	public @interface onChange {
		String method();

		/**
		 * Background process If true, method block executed in background
		 * thread. default false
		 * 
		 * @return
		 */
		boolean bg_process() default false;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD, ElementType.METHOD })
	public @interface hasDomainFilter {
		boolean checkDomainRuntime() default true;
	}

}
