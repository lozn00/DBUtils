package com.example.mydbutils;

import java.lang.reflect.Field;

import com.example.mydbutils.anotation.Column;
import com.example.mydbutils.anotation.ID;
import com.example.mydbutils.anotation.Table;
//import com.example.mydbutils.domain.New;

/**
 * 
 * @author luozheng
 * 
 * 
 *         找id字段 
 *         判断是不是id字段
 *          根据字段找列名 
 *          根据类找表明字符串 
 *          获取 int值 获取字符串值 
 *          ，字符串值
 *         需要通过String.valueOf()进行操作
 * 
 *
 */
public class ReflectUtils {
	/**
	 * 返回类的所有字段 所有私有都会被修改为共有
	 * 
	 * @param klass
	 * @return
	 */
	public static Field[] getFields(Class<?> klass) {
//		Field[] fields = klass.getFields();//这只能获取共有的方法
		Field[] fields = klass.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
		}
		return fields;
	}

	/**
	 * 返回 字段为id的，并修改为共有
	 * 
	 * @param klass
	 * @return
	 */
	public static Field getIDField(Class<?> klass) {
		Field[] fields = klass.getDeclaredFields();
		for (Field field : fields) {
			if (isIDField(field))
				return field;
		}
		return null;
	}
	/**
	 * 判断字段是否是int类型
	 * @param field
	 * @return
	 */
	public static boolean isIntType(Field field) {
		return field.getType().equals(int.class) || field.getType().equals(Integer.class);
	}
	/**
	 * 判断是否是id字段
	 * @param field
	 * @return
	 */
	public static boolean isIDField(Field field) {
		if(field.isAnnotationPresent(ID.class)){
			field.setAccessible(true);
			return true;
		}
		return false;
		//不能通过这个来判断了，只能通过注解判断是否是id
		// if(field.getName().equalsIgnoreCase("id") ||
		// field.getName().equalsIgnoreCase("_id")){
		// {
		// field.setAccessible(true);
		// return true;
		// }
	}

	/**
	 * 传递我什么对象我返回什么实例
	 * 
	 * @param kClass
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static <E> E getInstance(Class<E> kClass){
		try {
			return kClass.newInstance();
		} catch (Exception e) {
			mFailListener.onFail(e);
			e.printStackTrace();
		}
		// return null;
		return null;
	}
	/**
	 * 设置值
	 * 
	 * @param object
	 *            如果是 对象 请传递实例对象
	 * @param field
	 *            字段
	 * @param value
	 *            要设置的值
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static void setValue(Object object, Field field, Object value) {
		try {
			field.set(object, value);
		} catch (Exception e){
			mFailListener.onFail(e);
			e.printStackTrace();
		}
	}
	/**
	 * 获取int字段值
	 * @param object 静态可以传递为null
	 * @param field
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static int getIntValue(Object object,Field field) {
		try {
			return field.getInt(object);
		} catch (Exception e) {
			mFailListener.onFail(e);
			e.printStackTrace();
		}
		return -1;
	}
	/**
	 * 获取字符串字段值
	 * @param object
	 * @param field
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static String getStringValue(Object object,Field field){
		return String.valueOf(getValue(object, field));
	}
	/**
	 * 
	 * @param object  如果是静态 可传递为null
	 * @param field 字段名
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static Object  getValue(Object object,Field field) {
		try {
			return  field.get(object);
		} catch (Exception e) {
			mFailListener.onFail(e);
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 根据字节码对象返回表名字符串
	 * @param klass
	 * @return
	 */
	public static String getTableNameByClass(Class<?> klass){
		if(klass.isAnnotationPresent(Table.class)){
			return klass.getAnnotation(Table.class).value();//后面的value我想就是参数的泛型返回 注解的接口然后就可以链式输出value();
		}
		return  klass.getSimpleName();
	}
	public static String getColumnNameByField(Field field){
		if(field.isAnnotationPresent(Column.class))
		{
			return field.getAnnotation(Column.class).value();
		}
		return field.getName();
	}
	
	 public interface OnFailListener{
		 void onFail(Throwable e);
	}
	 static OnFailListener mFailListener=new OnFailListener() {
		
		@Override
		public void onFail(Throwable e) {
		}
	};
	/**
	 * 设置反射的异常监听事件
	 * @param onFailListener
	 */
	 public void setOnFailListener(OnFailListener onFailListener){
		 ReflectUtils.mFailListener=onFailListener;
	 }
//	public static<T> T  getValue(T T,Field field) throws IllegalAccessException, IllegalArgumentException{
//		return (T) field.get(T);
//	}
	{
//		try {
//		} catch (IllegalAccessException e) {
//			
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			
//			e.printStackTrace();
//		}
	}
}
