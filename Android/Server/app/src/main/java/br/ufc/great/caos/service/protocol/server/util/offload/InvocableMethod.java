/*******************************************************************************
 * Copyright (c) 2015 LG Electronics. All Rights Reserved. This software is the
 * confidential and proprietary information of LG Electronics. You shall not
 * disclose such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with LG Electronics.
 *******************************************************************************/
package br.ufc.great.caos.api.offload;

import java.io.Serializable;

/**
 * Class related to the Invocable Method and its parameters. Basically, this
 * method and its parameters will be serialized and sent to the Cloud.
 */
public class InvocableMethod implements Serializable {

	private static final long serialVersionUID = -5028632709347192824L;

	private String mMethodName;
	private String mClassName;
	private Object[] mParams;
	private String mAppVersion;
	private String mPackageName;

	public InvocableMethod(String packageName, String appVersion, String name, String declaredClass, Object[] params) {
		this.setAppVersion(appVersion);
		this.mMethodName = name;
		this.mParams = params;
		this.mClassName = declaredClass;
		this.mPackageName = packageName;
	}

	public String getMethodId() {
		String methodId = mClassName + "." + mAppVersion + "." + mMethodName;
		if (mParams != null) {
			for (Object object : mParams) {
				methodId += "." + object.getClass().getSimpleName();
			}
		}
		return methodId;
	}

	public String getMethodName() {
		return mMethodName;
	}

	public void setMethodName(String methodName) {
		this.mMethodName = methodName;
	}

	public Object[] getParam() {
		return mParams;
	}

	public void setParam(Object[] params) {
		this.mParams = params;
	}

	public String getClassName() {
		return mClassName;
	}

	public void setClassName(String className) {
		this.mClassName = className;
	}

	public String getAppVersion() {
		return mAppVersion;
	}

	public void setAppVersion(String appVersion) {
		this.mAppVersion = appVersion;
	}

	public String getPackageName() {
		return mPackageName;
	}

	public void setPackageName(String mPackageName) {
		this.mPackageName = mPackageName;
	}

}
