package com.ctgu.event;

public class EventMessage
{
	private int type;
	private Object obj;

	public EventMessage(int type, Object obj)
	{
		this.type = type;
		this.obj = obj;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public Object getObj()
	{
		return obj;
	}

	public void setObj(Object obj)
	{
		this.obj = obj;
	}

	@Override
	public String toString()
	{
		return "EventMessage [type=" + type + ", obj=" + obj + "]";
	}
}
