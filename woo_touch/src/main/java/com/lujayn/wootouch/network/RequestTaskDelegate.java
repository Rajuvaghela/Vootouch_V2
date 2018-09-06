package com.lujayn.wootouch.network;


import com.lujayn.wootouch.common.AppConstant.HttpRequestType;

public interface RequestTaskDelegate {

	void backgroundActivityComp(String response,
								HttpRequestType completedRequestType);

	void timeOut();

	void codeError(int code);

	void percentageDownloadCompleted(int percentage, Object record);
}
