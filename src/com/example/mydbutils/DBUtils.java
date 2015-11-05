package com.example.mydbutils;

import java.lang.reflect.Field;
//import java.lang.reflect.Type;
//import java.util.Iterator;


import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 
 * @author luozheng
 * 
 *         创建表，创建库，插入数据，更新数据 删除数据
 *
 */
public class DBUtils {
	private static final String TAG = "DBUtils";
	private Context context;
	private String dbName;
	private SQLiteDatabase mDb;
	private Db dbObj;

	public DBUtils(Context context, String dbName) {
		this.context = context;
		this.dbName = dbName;
		init();
	}

	private void init() {
		dbObj = new Db();
		mDb = dbObj.createDb();
	}

	/**
	 * 根据类的字节码自动创建表，如果存在不会创建,如果是int,或者integer类型的将创建的是integer类型，如果注解是id那么自动创建id字段，此类必须有注解，否则将无主见。其他类型将默认按字符串来创建表
	 * 
	 * @param klass
	 * @return
	 */
	public boolean createTable(Class<?> klass) {
		String tableName = ReflectUtils.getTableNameByClass(klass);
		if (dbObj.tableExist(tableName)) {
			return false;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("create table " + tableName);
		// sb.append("create table " + ReflectUtils.
		// getTableNameByClass(klass));
		sb.append("(");
		Field[] fields = ReflectUtils.getFields(klass);
		Log.i(TAG, "field总数" + fields.length);
		for (int i = 0; i < fields.length; i++) {

			if (ReflectUtils.isIDField(fields[i])) {
				sb.append(ReflectUtils.getColumnNameByField(fields[i]) + " integer primary key autoincrement");
			} else if (ReflectUtils.isIntType(fields[i])) {
				sb.append(ReflectUtils.getColumnNameByField(fields[i]) + " integer");
			}else {
				sb.append(ReflectUtils.getColumnNameByField(fields[i]) + " " + (fields[i].getType() == Integer.class ? "integer" : "varchar"));
			}
			if (i != fields.length - 1 && fields.length > 1) {
				sb.append(",");
			}
		}

		sb.append(")");
		Log.i(TAG, "sql:" + sb.toString());
		// sb.append("("++")");
		// String sql="create table"+tableName;
		// mDb.execSQL(sql);
		dbObj.execSQL(sb.toString());
		return true;
	}

	/**
	 * 删除表
	 * 
	 * @param klass
	 */
	public void deleteTable(Class<?> klass) {
		dbObj.execSQL("DROP TABLE " + ReflectUtils.getTableNameByClass(klass));
	}
	/**
	 * 给我对象我会自动根据里面的id字段来修改 数据库中存在的
	 * @param object
	 * @return
	 */
	public int update(Object object){
		Class<? extends Object> klass = object.getClass();
		ContentValues values = new ContentValues();
		fillContentValues(object, klass, values);
		int valueId=ReflectUtils.getIntValue(object, ReflectUtils.getIDField(klass));//获取id字段的 值
		return mDb.update(ReflectUtils.getTableNameByClass(klass), values, ReflectUtils.getColumnNameByField(ReflectUtils.getIDField(klass))+"=?",new String[]{""+valueId});
	}
	public long insert(Object object) {
		Class<? extends Object> klass = object.getClass();
		ContentValues values = new ContentValues();
		fillContentValues(object, klass, values);
		return mDb.insert(ReflectUtils.getTableNameByClass(klass), null, values);
	}
	/**
	 * 删除通过id
	 * @param klass
	 * @return 
	 */
	public <T> int deleteById(Class<T> klass,int id){
		return delete(klass,ReflectUtils.getColumnNameByField(ReflectUtils.getIDField(klass))+"=?",new String[]{""+id});
	}
	/**
	 * 删除所有
	 * @param klass
	 * @return 
	 */
	public <T> int deleteAll(Class<T> klass){
		return delete(klass,null,null);
	}
	public <T> int delete(Class<T> klass,String whereClause,String[] whereArgs){

		return mDb.delete(ReflectUtils.getTableNameByClass(klass), whereClause, whereArgs);
	}
	/**
	 * 查询id=某某
	 * @param <t>
	 * @param t
	 * @param id
	 * @return 
	 * @return
	 */
	public <T> T queryByID(Class<T> klass,int id)
	{
		String selection=ReflectUtils.getColumnNameByField(ReflectUtils.getIDField(klass))+"=?";
		String table = ReflectUtils.getTableNameByClass(klass);
		Cursor cursor = mDb.query(table, null, selection, new String[]{""+id}, null, null, null, null);
		while (cursor.moveToNext()) {
			T object = getObjectByCurosr(klass, cursor);
			cursor.close();
			return object;
		}
		cursor.close();
//		return query(t, null, selection, new String []{""+id});
		return null;
	}
	/**
	 * 查询类名通过 指定的字段
	 * @param t
	 * @param field
	 * @param obj
	 * @return 
	 */
	public <T> List<T> queryByColumn(Class<T> t,Field field,Object obj)
	{
			String columnName = ReflectUtils.getColumnNameByField(field);
		String selection=columnName+"=?";
		return query(t, null, selection, new String[]{obj.toString()});
	}
	/**
	 * 查询所有
	 * @param klass
	 * @return
	 */
	public <T> List<T> queryAll(Class<T> klass) {
		return query(klass, null, null, null);
	}
	/**
	 *
	 * @param klass
	 * @param columns
	 * @param selection
	 * @param selectionArgs
	 * @return
	 */
	public <T> List<T> query(Class<T> klass, String[] columns, String selection, String[] selectionArgs) {
		/**
		 * 查询所有还是弄一个对象来吧 查询的填充到一个对象是
		 * 
		 */
		String table = ReflectUtils.getTableNameByClass(klass);
		ArrayList<T> arrayList=null;
		Log.i(TAG, "数据库是是否打开"+mDb.isOpen());
//		if(mDb.isOpen()){
		Cursor cursor = mDb.query(table, columns, selection, selectionArgs, null, null, null, null);
			arrayList=new ArrayList<T>();
			while (cursor.moveToNext()) {
				T object = getObjectByCurosr(klass, cursor);
				arrayList.add(object);
			}
			cursor.close();
//		}
		return arrayList;
	}

	private <T> T getObjectByCurosr(Class<T> klass, Cursor cursor) {
		T object = ReflectUtils.getInstance(klass);//创建一个对象
		Field[] fields = ReflectUtils.getFields(klass);
		for (Field field : fields) {
			String columnName = ReflectUtils.getColumnNameByField(field);
			if(ReflectUtils.isIntType(field))
			{
				//直接通过名字找值 以前是通过getInt(下标)找方法为
				int valueInt=cursor.getInt(cursor.getColumnIndex(columnName));//管它是-1还是啥都赋值
				ReflectUtils.setValue(object, field, valueInt);
			}else{
				
				String valueStr=cursor.getString(cursor.getColumnIndex(columnName));//管它是-1还是啥都赋值
				ReflectUtils.setValue(object, field, valueStr);
			}
		}
		return object;
	}

	private void fillContentValues(Object object, Class<? extends Object> klass, ContentValues values) {

		Field[] fields = ReflectUtils.getFields(klass);

		for (Field field : fields) {
			if (ReflectUtils.isIDField(field)) {
				int valueInt = ReflectUtils.getIntValue(object, field);
				if (valueInt > 0)// 大于0说明指定了值
				{
					values.put(ReflectUtils.getColumnNameByField(field), valueInt);
				}
			} else {
				values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getStringValue(object, field));
			}
		}
	}

	private class Db {

		public SQLiteDatabase createDb() {
			return context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
		}

		private boolean tableExist(String tableName) {
			/**
			 * upper表示转大写，所以所有转大写要么全都不转大写
			 */
			// name,type字段 sql表示为建表语句
			String sql = "select count(*) from sqlite_master where type = 'table' and upper(name) =upper( ? )";
			Cursor cursor = mDb.rawQuery(sql, new String[] { tableName });

			return cursor.moveToNext() && cursor.getInt(0) > 0;
		}

		private void execSQL(String sql) {
			mDb.execSQL(sql);
		}
		// public boolean
	}

}
