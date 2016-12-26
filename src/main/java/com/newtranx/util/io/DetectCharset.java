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

package com.newtranx.util.io;

import java.io.IOException;
import java.io.InputStream;

import org.mozilla.universalchardet.UniversalDetector;

public class DetectCharset {

	public static String detectCharset(InputStream fis) throws IOException {
		byte[] buf = new byte[4096];
		// (1)
		UniversalDetector detector = new UniversalDetector(null);
		// (2)
		int nread;
		while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
			detector.handleData(buf, 0, nread);
		}
		// (3)
		detector.dataEnd();
		// (4)
		String encoding = detector.getDetectedCharset();
		// (5)
		detector.reset();
		return encoding;
	}

}
