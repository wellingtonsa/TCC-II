/*******************************************************************************
 * Copyright (c) 2015 LG Electronics. All Rights Reserved. This software is the
 * confidential and proprietary information of LG Electronics. You shall not
 * disclose such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with LG Electronics.
 *******************************************************************************/
package br.ufc.great.caos.service.protocol.core.offload;

import java.io.Serializable;

public class InvocableMethod implements Serializable {

	private static final long serialVersionUID = -5028632709347192824L;

	private String mAppName;
	private String mMethodName;
	private String mClassName;
	private Object[] mParams;
	private String mPackageName;

	public InvocableMethod(String packageName, String name, String declaredClass, String appName, Object[] params) {
		this.mMethodName = name;
		this.mParams = params;
		this.mClassName = declaredClass;
		this.mAppName = appName;
		this.mPackageName = packageName;
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

	public String getAppName() {
		return mAppName;
	}

	public void setAppName(String appName) {
		this.mAppName = appName;
	}


	public String getPackageName() {
		return mPackageName;
	}

	public void setPackageName(String mPackageName) {
		this.mPackageName = mPackageName;
	}

}
