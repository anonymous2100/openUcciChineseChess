package com.ctgu.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReflectUtil
{
	private static final Logger logger = Logger.getLogger(ReflectUtil.class.getName());
	public static List<Field> getAllFields(Object object)
	{
		Class clazz = object.getClass();
		Field[] fields = clazz.getDeclaredFields();
		List<Field> fieldList = new ArrayList<>();
		for (Field field : fields)
		{
			fieldList.add(field);
		}
		return fieldList;
	}

	public static String getFieldValueByFieldName(Object object, String fieldName)
	{
		try
		{
			Field field = object.getClass().getDeclaredField(fieldName);
			// 设置对象的访问权限，保证对private的属性的访问
			field.setAccessible(true);
			String value = String.valueOf(field.get(object));
			return value;
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "Reflect getFieldValue failed for field: " + fieldName, e);
			return null;
		}
	}

	public Object getFieldValueByName(Object object, String fieldName)
	{
		try
		{
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			String getter = "get" + firstLetter + fieldName.substring(1);
			Method method = object.getClass().getMethod(getter, new Class[] {});
			Object value = method.invoke(object, new Object[] {});
			return value;
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "Reflect getFieldValueByName failed for: " + fieldName, e);
			return null;
		}
	}

	public static void setFieldValueByFieldName(Object object, String fieldName, String fieldValue)
	{
		try
		{
			// 获取obj类的字节文件对象
			Class clazz = object.getClass();
			// 获取该类的成员变量
			Field f = clazz.getDeclaredField(fieldName);
			// 取消语言访问检查
			f.setAccessible(true);
			// 给变量赋值
			if (fieldValue.equals("true"))
			{
				f.set(object, Boolean.TRUE);
			}
			else if (fieldValue.equals("false"))
			{
				f.set(object, Boolean.FALSE);
			}
			else if (Integer.parseInt(fieldValue) >= 0)
			{
				f.set(object, Integer.parseInt(fieldValue));
			}
			else
			{
				f.set(object, fieldValue);
			}
		}
		catch (Exception e)
		{
 			logger.log(Level.WARNING, "Reflect setFieldValue failed for field: " + fieldName, e);
		}
	}
}
