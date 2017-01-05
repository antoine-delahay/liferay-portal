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

package com.liferay.jenkins.results.parser;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Kevin Yen
 */
public class BatchBuild extends BaseBuild {

	@Override
	public String getAppServer() {
		return getEnvironment("app.server");
	}

	public String getBatchName() {
		String batchName = getParameterValue("JOB_VARIANT");

		if ((batchName == null) || batchName.isEmpty()) {
			batchName = getParameterValue("JENKINS_JOB_VARIANT");
		}

		return batchName;
	}

	@Override
	public String getBrowser() {
		return getEnvironment("browser");
	}

	@Override
	public String getDatabase() {
		return getEnvironment("database");
	}

	@Override
	public String getJDK() {
		return getEnvironment("java.jdk");
	}

	@Override
	public String getOperatingSystem() {
		return getEnvironment("operating.system");
	}

	@Override
	public List<TestResult> getTestResults(String testStatus) {
		String status = getStatus();

		if (!status.equals("completed")) {
			return null;
		}

		List<TestResult> testResults = new ArrayList<>();

		JSONObject testReportJSONObject = getTestReportJSONObject();

		JSONArray childReportsJSONArray = testReportJSONObject.getJSONArray(
			"childReports");

		for (int i = 0; i < childReportsJSONArray.length(); i++) {
			JSONObject childReportJSONObject =
				childReportsJSONArray.getJSONObject(i);

			JSONObject childJSONObject = childReportJSONObject.getJSONObject(
				"child");

			String axisBuildURL = childJSONObject.getString("url");

			Matcher axisBuildURLMatcher = AxisBuild.buildURLPattern.matcher(
				axisBuildURL);

			axisBuildURLMatcher.find();

			String axisVariable = axisBuildURLMatcher.group("axisVariable");

			JSONObject resultJSONObject = childReportJSONObject.getJSONObject(
				"result");

			JSONArray suitesJSONArray = resultJSONObject.getJSONArray("suites");

			testResults.addAll(
				TestResult.getTestResults(
					getAxisBuild(axisVariable), suitesJSONArray, testStatus));
		}

		return testResults;
	}

	protected BatchBuild(String url) {
		this(url, null);
	}

	protected BatchBuild(String url, TopLevelBuild topLevelBuild) {
		super(url, topLevelBuild);
	}

	@Override
	protected List<String> findDownstreamBuildsInConsoleText() {
		return Collections.emptyList();
	}

	protected AxisBuild getAxisBuild(String axisVariable) {
		for (Build downstreamBuild : getDownstreamBuilds(null)) {
			AxisBuild downstreamAxisBuild = (AxisBuild)downstreamBuild;

			if (axisVariable.equals(downstreamAxisBuild.getAxisVariable())) {
				return downstreamAxisBuild;
			}
		}

		return null;
	}

	protected String getBatchComponent(
		String batchName, String environmentOption) {

		int x = batchName.indexOf(environmentOption);

		int y = batchName.indexOf("-", x);

		if (y == -1) {
			y = batchName.length();
		}

		return batchName.substring(x, y);
	}

	protected String getEnvironment(String environmentType) {
		Properties buildProperties = null;

		try {
			buildProperties = JenkinsResultsParserUtil.getBuildProperties();
		}
		catch (IOException ioe) {
			throw new RuntimeException("Unable to get build properties.", ioe);
		}

		List<String> environmentOptions = new ArrayList<>(
			Arrays.asList(
				StringUtils.split(
					buildProperties.getProperty(environmentType + ".types"),
					",")));

		String batchName = getBatchName();

		for (String environmentOption : environmentOptions) {
			if (batchName.contains(environmentOption)) {
				String batchComponent = getBatchComponent(
					batchName, environmentOption);

				return buildProperties.getProperty(
					"env.option." + environmentType + "." + batchComponent);
			}
		}

		String name = buildProperties.getProperty(environmentType + ".type");

		String environmentVersion = (String)buildProperties.get(
			environmentType + "." + name + ".version");

		Matcher matcher = majorVersionPattern.matcher(
			buildProperties.getProperty(
				environmentType + "." + name + ".version"));

		String environmentMajorVersion;

		if (matcher.matches()) {
			environmentMajorVersion = matcher.group(1);
		}
		else {
			environmentMajorVersion = environmentVersion;
		}

		if (environmentType.equals("java.jdk")) {
			return buildProperties.getProperty(
				"env.option." + environmentType + "." + name + "." +
					environmentMajorVersion.replace(".", ""));
		}
		else {
			return buildProperties.getProperty(
				"env.option." + environmentType + "." + name +
					environmentMajorVersion.replace(".", ""));
		}
	}

	protected final Pattern majorVersionPattern = Pattern.compile(
		"((\\d+)\\.?(\\d+?)).*");

}