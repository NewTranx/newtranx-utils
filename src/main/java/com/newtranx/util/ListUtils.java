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

package com.newtranx.util;

import java.util.List;
import java.util.function.Predicate;

public final class ListUtils {

	private ListUtils() {
	}

	public static <T> void removeBefore(List<T> source, Predicate<T> predicate) {
		int cutPoint = indexOf(source, predicate);
		source.subList(0, cutPoint).clear();
	}

	public static <T> void removeAfter(List<T> source, Predicate<T> predicate) {
		int cutPoint = lastIndexOf(source, predicate);
		source.subList(cutPoint, source.size()).clear();
	}

	public static <T> int indexOf(List<T> source, Predicate<T> predicate) {
		int found = -1;
		int i = 0;
		for (T x : source) {
			if (predicate.test(x)) {
				found = i;
				break;
			}
			i++;
		}
		return found;
	}

	public static <T> int lastIndexOf(List<T> source, Predicate<T> predicate) {
		int found = -1;
		int i = 0;
		for (T x : source) {
			if (predicate.test(x)) {
				found = i;
			}
			i++;
		}
		return found;
	}

}
