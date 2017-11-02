/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.vulcan.test.internal.json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.liferay.vulcan.result.Try;

import java.util.Objects;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * A {@link Matcher} that can be used to check if a string is a correct json
 * object.
 *
 * @author Alejandro Hernández
 * @review
 */
public class IsJsonObjectString extends TypeSafeDiagnosingMatcher<String> {

	public IsJsonObjectString(Matcher<JsonObject> jsonObjectMatcher) {
		_jsonObjectMatcher = jsonObjectMatcher;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText(
			"a json object "
		).appendDescriptionOf(
			_jsonObjectMatcher
		);
	}

	@Override
	protected boolean matchesSafely(String item, Description description) {
		Try<JsonObject> jsonObjectTry = Try.fromFallible(
			() -> new Gson().fromJson(item, JsonObject.class));

		return jsonObjectTry.filter(
			Objects::nonNull
		).fold(
			__ -> {
				description.appendText("wasn't a json object");

				return false;
			},
			jsonObject -> {
				if (_jsonObjectMatcher.matches(jsonObject)) {
					return true;
				}
				else {
					description.appendText("was a json object ");

					_jsonObjectMatcher.describeMismatch(
						jsonObject, description);

					return false;
				}
			}
		);
	}

	private final Matcher<JsonObject> _jsonObjectMatcher;

}