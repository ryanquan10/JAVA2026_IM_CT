
/**
 * Copyright (c) 2011-2019, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tio.jfinal.template.stat.ast;

import org.tio.jfinal.template.Env;
import org.tio.jfinal.template.TemplateException;
import org.tio.jfinal.template.expr.ast.Expr;
import org.tio.jfinal.template.expr.ast.ExprList;
import org.tio.jfinal.template.io.Writer;
import org.tio.jfinal.template.stat.Location;
import org.tio.jfinal.template.stat.ParseException;
import org.tio.jfinal.template.stat.Scope;

/**
 * Output 输出指令
 * 
 * 用法：
 * 1：#(value)
 * 2：#(x = 1, y = 2, x + y)
 * 3：#(seoTitle ?? 'JFinal 极速开发社区')
 */
public class Output extends Stat {
	
	protected Expr expr;
	
	public Output(ExprList exprList, Location location) {
		if (exprList.length() == 0) {
			throw new ParseException("The expression of output directive like #(expression) can not be blank", location);
		}
		this.expr = exprList.getActualExpr();
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		try {
			Object value = expr.eval(scope);
			
			if (value instanceof String) {
				String str = (String)value;
				writer.write(str, 0, str.length());
			} else if (value instanceof Number) {
				Class<?> c = value.getClass();
				if (c == Integer.class) {
					writer.write((Integer)value);
				} else if (c == Long.class) {
					writer.write((Long)value);
				} else if (c == Double.class) {
					writer.write((Double)value);
				} else if (c == Float.class) {
					writer.write((Float)value);
				} else if (c == Short.class) {
					writer.write((Short)value);
				} else {
					writer.write(value.toString());
				}
			} else if (value != null) {
				writer.write(value.toString());
			}
		} catch(TemplateException | ParseException e) {
			throw e;
		} catch(Exception e) {
			throw new TemplateException(e.getMessage(), location, e);
		}
	}
}




