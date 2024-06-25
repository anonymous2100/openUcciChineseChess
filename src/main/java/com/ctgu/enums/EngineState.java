package com.ctgu.enums;

public enum EngineState
{

	Gone, Initialized, Ready, Thinking, Exiting;

	public boolean isRunning()
	{
		return this == Thinking || this == Ready;
	}

	public boolean isStopped()
	{
		return this == Gone || this == Exiting;
	}

	public boolean isReady()
	{
		return this == Ready;
	}

	public boolean isThinking()
	{
		return this == Thinking;
	}
}
