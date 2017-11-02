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

package com.liferay.vulcan.test.json;

import static com.liferay.vulcan.test.json.JsonMatchers.aJsonBoolean;
import static com.liferay.vulcan.test.json.JsonMatchers.aJsonLong;
import static com.liferay.vulcan.test.json.JsonMatchers.aJsonString;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.google.gson.JsonObject;

import org.hamcrest.Description;
import org.hamcrest.StringDescription;

import org.junit.Test;

/**
 * @author Alejandro Hernández
 */
public class ConditionsTest {

	@Test
	public void testInvokingDescribesToUpdatesDescription() {
		Conditions.Builder builder = new Conditions.Builder();

		Conditions conditions = builder.where(
			"vulcan", is(aJsonString(equalTo("Live long and prosper")))
		).where(
			"geek", is(aJsonBoolean(true))
		).build();

		Description description = new StringDescription();

		conditions.describeTo(description);

		String expected =
			"{\n  vulcan: is a string element with a value that is \"Live " +
				"long and prosper\"\n  geek: is a boolean element with a " +
					"value that is <true>\n}";

		assertThat(description.toString(), is(equalTo(expected)));
	}

	@Test
	public void testInvokingMatchesElementInStrictModeUpdatedDescription() {
		Conditions.Builder builder = new Conditions.Builder();

		Conditions conditions = builder.where(
			"vulcan", is(aJsonString(equalTo("Live long and prosper")))
		).where(
			"geek", is(aJsonBoolean(true))
		).withStrictModeActivated(
		).build();

		Description description = new StringDescription();

		JsonObject jsonObject = new JsonObject();

		jsonObject.addProperty("vulcan", "Live long and prosper");
		jsonObject.addProperty("geek", true);
		jsonObject.addProperty("number", 42);
		jsonObject.addProperty("other", "vulcan");

		boolean matchesElement = conditions.matches(jsonObject);
		conditions.describeMismatch(jsonObject, description);

		String expected =
			"with more fields than validated. Extra keys: number, other";

		assertThat(matchesElement, is(false));
		assertThat(description.toString(), is(equalTo(expected)));
	}

	@Test
	public void testInvokingMatchesElementUpdatedDescription() {
		Conditions.Builder builder = new Conditions.Builder();

		Conditions conditions = builder.where(
			"vulcan", is(aJsonString(equalTo("Live long and prosper")))
		).where(
			"geek", is(aJsonBoolean(true))
		).where(
			"number", is(aJsonLong(equalTo(42L)))
		).build();

		Description description = new StringDescription();

		JsonObject jsonObject = new JsonObject();

		jsonObject.addProperty("vulcan", "Live long and");
		jsonObject.addProperty("geek", 23);
		jsonObject.addProperty("number", 42);

		boolean matchesElement = conditions.matches(jsonObject);
		conditions.describeMismatch(jsonObject, description);

		String expected =
			"{\n  vulcan: was a string element with a value that was \"Live " +
				"long and\"\n  geek: was not a boolean element, but a number " +
					"element\n  ...\n}";

		assertThat(matchesElement, is(false));
		assertThat(description.toString(), is(equalTo(expected)));
	}

}