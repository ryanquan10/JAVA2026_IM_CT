
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

package org.tio.jfinal.template.stat;

/**
 * 对 Text 节点进行压缩
 * 
 * 1：为追求性能极致，只压缩 Text 节点，所以压缩结果会存在一部分空白字符
 * 2：每次读取一行，按行进行压缩
 * 3：第一行左侧空白不压缩
 * 4：最后一行右侧空白不压缩（注意：最后一行以字符 '\n' 结尾时不算最后一行）
 * 5：第一行、最后一行以外的其它行左右侧都压缩
 * 6：文本之内的空白不压缩，例如字符串 "abc  def" 中的 "abc" 与 "def" 之间的空格不压缩
 * 7：压缩分隔符默认配置为 '\n'，还可配置为 ' '。如果模板中含有 javascript 脚本，需配置为 '\n'
 * 8：可通过 Engine.setCompressor(Compressor) 来定制自己的实现类
 *    可使用第三方的压缩框架来定制，例如使用 google 的压缩框架:
 *      压缩 html: com.googlecode.htmlcompressor:htmlcompressor
 *      压缩 javascript: com.google.javascript:closure-compiler
 */
public class Compressor {
	
	protected char separator = '\n';
	
	public Compressor() {}
	
	public Compressor(char separator) {
		if (separator > ' ') {
			throw new IllegalArgumentException("The parameter separator must be a separator character");
		}
		this.separator = separator;
	}
	
	public StringBuilder compress(StringBuilder content) {
		int len = content.length();
		StringBuilder ret = new StringBuilder(len);
		
		char ch;
		boolean hasLineFeed;
		int begin = 0;
		int forward = 0;
		
		while (forward < len) {
			// 扫描空白字符
			hasLineFeed = false;
			while (forward < len) {
				ch = content.charAt(forward);
				if (ch <= ' ') {			// 包含换行字符在内的空白字符
					if (ch == '\n') {		// 包含换行字符
						hasLineFeed = true;
					}
					forward++;
				} else {					// 非空白字符
					break ;
				}
			}
			
			// 压缩空白字符
			if (begin != forward) {
				if (hasLineFeed) {
					ret.append(separator);
				} else {
					ret.append(' ');
				}
			}
			
			// 复制非空白字符
			while (forward < len) {
				ch = content.charAt(forward);
				if (ch > ' ') {
					ret.append(ch);
					forward++;
				} else {
					break ;
				}
			}
			
			begin = forward;
		}
		
		return ret;
	}
}




