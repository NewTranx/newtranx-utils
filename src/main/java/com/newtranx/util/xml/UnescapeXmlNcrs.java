/*
 * Copyright 2016 NewTranx Co. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.newtranx.util.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnescapeXmlNcrs {

	private static final Pattern P = Pattern.compile("&#([xX]?[0-9a-fA-F]+);");

	public static String unescapeNcrs(String input) {
		Matcher m = P.matcher(input);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String str = m.group(1);
			int code;
			if (str.charAt(0) == 'x' || str.charAt(0) == 'X') {
				code = Integer.parseInt(str.substring(1), 16);
			} else {
				code = Integer.parseInt(str);
			}
			m.appendReplacement(sb, Character.toString((char) code));
		}
		m.appendTail(sb);
		return sb.toString();
	}

}
