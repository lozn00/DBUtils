package com.example.mydbutils;


import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import android.util.Log;

import com.example.mydbutils.anotation.Column;
import com.example.mydbutils.anotation.ID;
import com.example.mydbutils.anotation.Table;

//import com.example.mydbutils.domain.New;

/**
 * @author luozheng
 *         <p>
 *         <p>
 *         找id字段
 *         判断是不是id字段
 *         根据字段找列名
 *         根据类找表明字符串
 *         获取 int值 获取字符串值
 *         ，字符串值
 *         需要通过String.valueOf()进行操作
2016-1-9 15:19:34 update http://www.cnblogs.com/avenwu/p/4193000.html
 */
public class ReflectUtils {
    private static final String TAG = "ReflectUtils";

    /**
     * 返回类的所有字段 所有私有都会被修改为共有
     *
     * @param klass
     * @return
     */
    public static Field[] getFields(Class<?> klass) {
//		Field[] fields = klass.getFields();//这只能获取共有的方法
        Field[] fields = klass.getFields();
        for (Field field : fields) {
            field.setAccessible(true);
        }
        return fields;
    }

    /**
     * 通过给定的字符串字段从字节码类中获取出字段对象
     * @param klass
     * @param fieldStr
     * @return
     */
    public static Field getField(Class<?> klass, String fieldStr) {
//		Field[] fields = klass.getFields();//这只能获取共有的方法
        try {
            return klass.getField(fieldStr);
        } catch (NoSuchFieldException e) {
            mFailListener.onFail(e);
            e.printStackTrace();
            return null;
        }

    }
    /*
    public static String getFieldStrByMethodName(Class<?> klass, String field) {
//		Field[] fields = klass.getFields();//这只能获取共有的方法

        try {
            Method declaredMethod = klass.getDeclaredMethod(field, klass);
            String name = declaredMethod.getName();
            if (name.startsWith("set") || name.startsWith("get")) {
                return name.substring("set".length());
            }
            return name;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }


    }*/

    /**
     * 返回 字段为id的，
     *
     * @param klass
     * @return
     */
    public static Field getIDField(Class<?> klass) {
        Field[] fields = klass.getFields();//getDeclaredFields只能获取子类的所有方法。
//        Field[] fields = klass.getDeclaredFields();//getDeclaredFields只能获取子类的所有方法。
        ArrayList<Field> listBak=new ArrayList<>();//备份的id字段如果找不到申明的就用listBak
        for (Field field : fields) {
            if (isIDField(field))
            {
                return field;
            }else if("_id".equals(field.getName())|| "id".equals(field.getName())){
                if(listBak.size()>0 && listBak.get(0).equals("id")){//找到的是_id
                    listBak.add(0,field);//_id优先级比id高
                }else if(listBak.size()==0){//可能是id也可能是_id
                    listBak.add(field);
                }
             }

        }
      Field field =(listBak.size()==0?null:listBak.get(0));
        if(field==null){
            new RuntimeException("警告,没有找到id,请申明id或_id或使用注解申明其它字符的作为id字段");
        }
        return field;
    }

    /**
     * 判断字段是否是int类型
     *
     * @param field
     * @return
     */
    public static boolean isIntType(Field field) {
        return field.getType().equals(int.class) || field.getType().equals(Integer.class);
    }

    /**
     * 判断字段是否是Long类型
     *
     * @param field
     * @return
     */
    public static boolean isLongType(Field field) {
        return field.getType().equals(long.class) || field.getType().equals(Long.class);
    }

    /**
     * 判断字段是否是Long类型
     *
     * @param field
     * @return
     */
    public static boolean isFloatType(Field field) {
        return field.getType().equals(float.class) || field.getType().equals(Float.class);
    }

    /**
     * 判断字段是否是double类型
     *
     * @param field
     * @return
     */
    public static boolean isDoubleType(Field field) {
        return field.getType().equals(double.class) || field.getType().equals(Double.class);
    }

    /**
     * 判断字段是否是short类型
     *
     * @param field
     * @return
     */
    public static boolean isShortType(Field field) {
        return field.getType().equals(short.class) || field.getType().equals(Short.class);
    }
    /**
     * 判断字段是否是beoolean类型
     *
     * @param field
     * @return
     */
    public static boolean isBooleanType(Field field) {
        return field.getType().equals(boolean.class) || field.getType().equals(Boolean.class);
    }
    /**
     * 判断字段是否是string类型
     *
     * @param field
     * @return
     */
    public static boolean isStringType(Field field) {
        return field.getType().equals(String.class);
    }
    public static boolean isStringArrayType(Field field) {
        return field.getType().equals(String[].class);
    }
    public static boolean isBytesType(Field field) {
        return Byte[].class.equals(field);
    }
    public static boolean isByteType(Field field) {
        return byte.class.equals(field)|| Byte.class.equals(field);
    }

    /**
     * 判断是否是id字段 通过注解
     *
     * @param field
     * @return
     */
    public static boolean isIDField(Field field) {
        Log.i(TAG,"field:"+field);
        if (field!=null && field.isAnnotationPresent(ID.class)) {
//            field.setAccessible(true);
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
    public void ModifyField(Field field){
//将字段的访问权限设为true：即去除private修饰符的影响
        field.setAccessible(true);

/*去除final修饰符的影响，将字段设为可修改的*/
        Field modifiersField = null;
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
                modifiersField.set(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }
    public static boolean isFinal(Object o){
        try{
            o=null;
        }catch(Exception ex){
            return true;
        }
        return false;
    }
    /**
     * 传递我什么对象我返回什么实例
     *
     * @param kClass
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static <E> E getInstance(Class<E> kClass) {
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
     * @param object 如果是 对象 请传递实例对象
     * @param field  字段
     * @param value  要设置的值
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static void setValue(Object object, Field field, Object value) {
        try {
            field.set(object, value);
        } catch (Exception e) {
            mFailListener.onFail(e);
            e.printStackTrace();
        }
    }

    /**
     * 获取int字段值
     *
     * @param object 静态可以传递为null
     * @param field
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static int getIntValue(Object object, Field field) {
        try {
            return field.getInt(object);
        } catch (Exception e) {
            mFailListener.onFail(e);
            e.printStackTrace();
        }
        return -1;
    }

    public static double getDoubleValue(Object object, Field field) {
        try {
            return field.getDouble(object);
        } catch (Exception e) {
            mFailListener.onFail(e);
            e.printStackTrace();
        }
        return -1;
    }

    public static float getFloatValue(Object object, Field field) {
        try {
            return field.getFloat(object);
        } catch (Exception e) {
            mFailListener.onFail(e);
            e.printStackTrace();
        }
        return -1;
    }

    public static short getShortValue(Object object, Field field) {
        try {
            return field.getShort(object);
        } catch (Exception e) {
            mFailListener.onFail(e);
            e.printStackTrace();
        }
        return -1;
    }
    public static byte getByteValue(Object object, Field field) {
        try {
            return field.getByte(object);
        } catch (Exception e) {
            mFailListener.onFail(e);
            e.printStackTrace();
        }
        return -1;
    }
    public static boolean isArrayType(Object object){
        return object instanceof  Object[] || object.getClass().isArray();

    }
    //http://docs.oracle.com/javase/7/docs/api/java/lang/reflect/Array.html

    /**
     * 正确不正确等待验证
     * @param object
     * @return
     */
    public static byte[] getBytesValue(Object object) {
        try {
            int length = Array.getLength(object);
            StringBuilder stringBuilder=new StringBuilder();

            for (int i = 0; i < length; i++) {
                byte aByte = Array.getByte(object, i);
                stringBuilder.append(aByte);
            }
            stringBuilder.toString().getBytes();


        } catch (Exception e) {
            mFailListener.onFail(e);
            e.printStackTrace();
        }
        return null;
    }
    public static boolean getBooleanValue(Object object, Field field) {
        try {
            return field.getBoolean(object);
        } catch (Exception e) {
            mFailListener.onFail(e);
            e.printStackTrace();
        }
        return false;
    }
    public static  long getLongValue(Object object, Field field) {

        try {
            return  field.getLong(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            mFailListener.onFail(e);
        }
        return -1;
    }

    /**
     * 获取字符串字段值
     *
     * @param object
     * @param field
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static String getStringValue(Object object, Field field) {
        return String.valueOf(getValue(object, field));
    }

    /**
     * @param object 如果是静态 可传递为null
     * @param field  字段名
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static Object getValue(Object object, Field field) {
        try {
            return field.get(object);
        } catch (Exception e) {
            mFailListener.onFail(e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据字节码对象返回表名字符串
     *
     * @param klass
     * @return
     */
    public static String getTableNameByClass(Class<?> klass) {
        if (klass.isAnnotationPresent(Table.class)) {
            return klass.getAnnotation(Table.class).value();//后面的value我想就是参数的泛型返回 注解的接口然后就可以链式输出value();
        }
        return klass.getSimpleName();
    }

    public static String getColumnNameByField(Field field) {
        if(field==null){
            new RuntimeException("字段不能为空");
            return null;
        }
        try{
        if ( field.isAnnotationPresent(Column.class)) {
            return field.getAnnotation(Column.class).value();
        }

        }catch ( Exception e){
            mFailListener.onFail(e);
            e.toString();

            Log.e(TAG,field.getName()+"E."+e.toString());
        }
        return field.getName();
    }




    public interface OnFailListener {
        void onFail(Throwable e);
    }

    static OnFailListener mFailListener = new OnFailListener() {

        @Override
        public void onFail(Throwable e) {
            e.printStackTrace();
            Log.e(TAG,"ERR:"+e.toString());
        }
    };

    /**
     * 设置反射的异常监听事件
     *
     * @param onFailListener
     */
    public void setOnFailListener(OnFailListener onFailListener) {
        ReflectUtils.mFailListener = onFailListener;
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

    /**
     * 拼接某属性get 方法
     * @param fldname
     * @return
     */
    public static String getGetMethodNameByField(String fldname){
        if(null == fldname || "".equals(fldname)){
            mFailListener.onFail(new RuntimeException("无法拼接get方法因为字段为空"));
            return null;
        }
        String pro = "get"+fldname.substring(0,1).toUpperCase()+fldname.substring(1);
        return pro;
    }
    /**
     * 拼接某属性set 方法
     * @param fldname 根据字段名拼接方法
     * @return
     */
    public static String getSetMethodNameField(String fldname){
        if(null == fldname || "".equals(fldname)){
            mFailListener.onFail(new RuntimeException("无法拼接set方法因为字段为空"));
            return null;
        }
        String pro = "set"+fldname.substring(0,1).toUpperCase()+fldname.substring(1);
        return pro;
    }
    /**
     * 判断该方法是否存在
     * @param methods
     * @param met
     * @return
     */
    public static boolean methodIsExist(Method methods[], String met){
        if(null != methods ){
            for(Method method:methods){
                if(met.equals(method.getName())){
                    return true;
                }
            }
        }
        return false;
    }
}
