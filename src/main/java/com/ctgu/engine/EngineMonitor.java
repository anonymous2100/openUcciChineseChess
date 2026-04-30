package com.ctgu.engine;

public interface EngineMonitor
{
	void onError(String str);

	void onResponse(String str);
}
