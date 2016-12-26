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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * http://stackoverflow.com/questions/26162407/is-there-an-equivalent-of-scalas-
 * either-in-java-8
 * 
 * @author Holger
 *
 * @param <L>
 * @param <R>
 */
public final class Either<L, R> {
	public static <L, R> Either<L, R> left(L value) {
		return new Either<>(Optional.of(value), Optional.empty());
	}

	public static <L, R> Either<L, R> right(R value) {
		return new Either<>(Optional.empty(), Optional.of(value));
	}

	public final Optional<L> left;
	public final Optional<R> right;

	private Either(Optional<L> l, Optional<R> r) {
		left = l;
		right = r;
	}

	public <T> T map(Function<? super L, T> lFunc, Function<? super R, T> rFunc) {
		return left.map(lFunc).orElseGet(() -> right.map(rFunc).get());
	}

	public <T> Either<T, R> mapLeft(Function<? super L, ? extends T> lFunc) {
		return new Either<>(left.map(lFunc), right);
	}

	public <T> Either<L, T> mapRight(Function<? super R, ? extends T> rFunc) {
		return new Either<>(left, right.map(rFunc));
	}

	public void apply(Consumer<? super L> lFunc, Consumer<? super R> rFunc) {
		left.ifPresent(lFunc);
		right.ifPresent(rFunc);
	}

	public boolean isLeft() {
		return left.isPresent();
	}

	public boolean isRight() {
		return right.isPresent();
	}

}