package com.example.mydbutils;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

//import java.lang.reflect.Type;
//import java.util.Iterator;

/**
 *
 * http://supershll.blog.163.com/blog/static/3707043620123153547193/
 * @author luozheng
 * 
 *         创建表，创建库，插入数据，更新数据 删除数据
 *
 */
public class DBUtils {
	private static final String TAG = "DBUtils";
	private Context context;
	private String dbName= "zheng_db";

	public SQLiteDatabase getDb() {
		return mDb;
	}

	public void setDb(SQLiteDatabase mDb) {
		this.mDb = mDb;
	}

	private SQLiteDatabase mDb;

	public Db getDbObj() {
		return dbObj;
	}

	public void setDbObj(Db dbObj) {
		this.dbObj = dbObj;
	}

	private Db dbObj;

	public DBUtils(Context context, String dbName) {
		this.context = context;
		this.dbName = dbName;
		init();
	}
	public DBUtils(Context context) {
		this.context = context;
		init();
	}
	public void close(){
		if(dbObj!=null){
			dbObj.close();
			dbObj=null;
		}
	}

	private void init() {
		dbObj = new Db();
		mDb = dbObj.getSQLiteDatabase();
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
			}else if (ReflectUtils.isBooleanType(fields[i])) {

//					boolean aBoolean = fields[i].getBoolean(klass);
					// Caused by: java.lang.IllegalArgumentException: Expected receiver of type com.huluboshi.model.PlayModel, but got java.lang.Class<com.huluboshi.model.PlayModel>
					sb.append(ReflectUtils.getColumnNameByField(fields[i]) + " integer");


			}
			else if (ReflectUtils.isFloatType(fields[i]) || ReflectUtils.isDoubleType(fields[i])) {
				sb.append(ReflectUtils.getColumnNameByField(fields[i]) + " REAL");//浮点型
			}
			else {
				sb.append(ReflectUtils.getColumnNameByField(fields[i]) + " " + "varchar");
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
		boolean succ=fillContentValues(object, klass, values);
		if(!succ){
			return -2;
		}

		int valueId=ReflectUtils.getIntValue(object, ReflectUtils.getIDField(klass));//获取id字段的 值
		return mDb.update(ReflectUtils.getTableNameByClass(klass), values, ReflectUtils.getColumnNameByField(ReflectUtils.getIDField(klass))+"=?",new String[]{""+valueId});
	}

	/**
	 * 更新指定字段 失败返回值小于0
	 * @param object 更新的对象
	 * @param fieldstr 需要更新的字段
     * @return
     */
	public int update(Object object,String fieldstr){
		Class<? extends Object> klass = object.getClass();
		ContentValues values = new ContentValues();
		boolean b = fillContentValue(object, klass, values, fieldstr);
		if(b==false){
			return -2;
		}
		int valueId=ReflectUtils.getIntValue(object, ReflectUtils.getIDField(klass));//获取id字段的 值
		return mDb.update(ReflectUtils.getTableNameByClass(klass), values, ReflectUtils.getColumnNameByField(ReflectUtils.getIDField(klass))+"=?",new String[]{""+valueId});
	}
	public long insert(Object object) {
		Class<? extends Object> klass = object.getClass();
		ContentValues values = new ContentValues();
		boolean b = fillContentValues(object, klass, values);
		if(!b){
			return-2;
		}
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
	 *
	 * @param klass
	 * @param column 要查询的列
	 * @param value 要查询列所等于的值
	 * @param <T>
     * @return
     */
	public <T> T queryByColumn(Class<T> klass,String column,String value)
	{
		String selection=column+"=?";
		String table = ReflectUtils.getTableNameByClass(klass);
		Cursor cursor = mDb.query(table, null, selection, new String[]{""+value}, null, null, null, null);
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
	public <T> List<T> queryAllByField(Class<T> t,Field field,Object obj)
	{
			String columnName = ReflectUtils.getColumnNameByField(field);
		return queryAllByField(t, columnName,obj);
	}

	public <T> List<T> queryAllByField(Class<T> klass, String  filedName,Object value)
	{
		String selection=filedName+"=?";
		return query(klass,null,selection,new String[]{""+value.toString()});
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
	 * 查询id=某某 所有

	 * @param id
	 * @return
	 * @return
	 */
	public <T> List<T> queryAllByID(Class<T> klass, int id)
	{
		String selection=ReflectUtils.getColumnNameByField(ReflectUtils.getIDField(klass))+"=?";
		return query(klass,null,selection,new String[]{""+id});
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

	/**
	 *
	 * 逆向过程通过游标 赋值  从数据库查询出来赋值给对象
	 * @param klass
	 * @param cursor
	 * @param <T>
     * @return
     */
	private <T> T getObjectByCurosr(Class<T> klass, Cursor cursor) {
		T object = ReflectUtils.getInstance(klass);//创建一个对象
		Field[] fields = ReflectUtils.getFields(klass);
		for (Field field : fields) {
			String columnName = ReflectUtils.getColumnNameByField(field);
				int columnIndex = cursor.getColumnIndex(columnName);
			if(columnIndex!=-1){

				if(ReflectUtils.isIntType(field))//INT
				{
					//直接通过名字找值 以前是通过getInt(下标)找方法为
					int valueInt=cursor.getInt(columnIndex);//管它是-1还是啥都赋值
					ReflectUtils.setValue(object, field, valueInt);
				}else if(ReflectUtils.isStringType(field)){//String
					String valueString = cursor.getString(columnIndex);
					ReflectUtils.setValue(object,field,valueString);
				}else if(ReflectUtils.isBooleanType(field)){//Boolean
					boolean valueBoolean = cursor.getInt(columnIndex)==1?true:false;
					ReflectUtils.setValue(object,field,valueBoolean);
				}else if(ReflectUtils.isBytesType(field)){//byte[]
					byte[] valueBytes = cursor.getBlob(columnIndex);
					ReflectUtils.setValue(object,field,valueBytes);
				}else if(ReflectUtils.isShortType(field)){//Short
					double valueShort = cursor.getShort(columnIndex);
					ReflectUtils.setValue(object,field,valueShort);
				}else if(ReflectUtils.isLongType(field)){//Long
					double valueLong = cursor.getLong(columnIndex);
					ReflectUtils.setValue(object,field,valueLong);
				}else if(ReflectUtils.isFloatType(field)){//Float
					double valueFloat = cursor.getFloat(columnIndex);
					ReflectUtils.setValue(object,field,valueFloat);
				}else if(ReflectUtils.isDoubleType(field)){//Double
					double valueDouble = cursor.getDouble(columnIndex);
					ReflectUtils.setValue(object,field,valueDouble);
				}else{
					//这里无法解决boolean类型和一些对象类型，所以还是不推荐这么做
					String valueStr=cursor.getString(columnIndex);//管它是-1还是啥都赋值
					ReflectUtils.setValue(object, field, valueStr);//这样赋值是字符串
				}
			}else{
				Log.e(TAG,"抱歉 "+field+"对于的列"+columnName+"找不到index");
			}

		}
		return object;
	}

	private boolean fillContentValues(Object object, Class<? extends Object> klass, ContentValues values) {
			boolean flag=false;
		Field[] fields = ReflectUtils.getFields(klass);

		for (Field field : fields) {
			boolean currentflag=fillContentValue(object,klass,values,field);
			if(currentflag && flag==false){
				flag=true;
			}
		}
		return flag;
	}

	/**
	 *
	 * @param object
	 * @param klass
	 * @param values
     * @param fieldStr 字符串字段 将根据字节码反射出字段对象
     */
	private boolean fillContentValue(Object object, Class<? extends Object> klass, ContentValues values,String fieldStr) {

		Field field = ReflectUtils.getField(klass,fieldStr);
		return fillContentValue(object,klass,values,field);

	}

	/**
	 *  正向过程 把对象中的值赋值写入数据库
	 * @param object 对象实例
	 * @param klass 字节码
	 * @param values  已经初始化的contentValues()
     * @param field 字段对象
     */
	private boolean fillContentValue(Object object, Class<? extends Object> klass, ContentValues values,Field field) {
			boolean flag=false;

		if(field==null || TextUtils.isEmpty(field.getName())){
			Log.e(TAG,object.getClass().getSimpleName()+".class中包含字段"+field);
//			new RuntimeException(object.getClass().getSimpleName()+".class中包含字段"+field);
			return false;
		}
		if(field.isEnumConstant()){
			Log.e(TAG,"不能插入枚举常量:"+field.getName());
			return false;
		}
		String name = field.getName();
		String upCaseName=name.toUpperCase();
		if(upCaseName.equals(name)){
			Log.e(TAG,"大写的为常量不能插入:"+field.getName()+",FIELD:"+field+","+field.getModifiers());
			return false;
		}


		try{
			if (ReflectUtils.isIDField(field)) {
				int valueInt = ReflectUtils.getIntValue(object, field);
				if (valueInt > 0)// 大于0说明指定了值
				{
					values.put(ReflectUtils.getColumnNameByField(field), valueInt);
				}
			}else if(ReflectUtils.isShortType(field) ) {//short
				values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getShortValue(object, field));
			} else if(ReflectUtils.isBooleanType(field) ) {//boolean
				values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getBooleanValue(object, field));
			} else if(ReflectUtils.isIntType(field) ) {//int
				values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getIntValue(object, field));
			}else if(ReflectUtils.isLongType(field) ) {//int
				values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getLongValue(object, field));
			}else if(ReflectUtils.isStringType(field)){//String
				values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getStringValue(object, field));
			}else if(ReflectUtils.isBytesType(field)){//Bolb  字节数组 等待验证//TODO 等待验证
				values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getBytesValue(object));
			}else if(ReflectUtils.isFloatType(field)){//Float
				values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getFloatValue(object, field));
			}else if(ReflectUtils.isDoubleType(field)){//Double
				values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getDoubleValue(object, field));
			}else{
				Log.e(TAG,"无法识别字段类型 "+field+",type:"+field.getType());
			}
			return true;
		}catch (Exception e){
			Log.e(TAG,"E:"+e.toString());
			e.printStackTrace();
			return false;
		}
	}
	public class Db {


		private SQLiteDatabase sqLiteDatabase;

		public SQLiteDatabase getSQLiteDatabase() {
				if(sqLiteDatabase==null){
						synchronized (Db.class){
							if(sqLiteDatabase==null){
								sqLiteDatabase = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
							}
						}

				}else{
					if(!sqLiteDatabase.isOpen()){

						sqLiteDatabase = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
					}
				}

				return sqLiteDatabase;
		}

		public boolean tableExist(String tableName) {
			/**
			 * upper表示转大写，所以所有转大写要么全都不转大写
			 */
			// name,type字段 sql表示为建表语句
			String sql = "select count(*) from sqlite_master where type = 'table' and upper(name) =upper( ? )";
			Cursor cursor = mDb.rawQuery(sql, new String[] { tableName });

			return cursor.moveToNext() && cursor.getInt(0) > 0;
		}

		/**
		 * 表是否存在 不存在则创建表，但是必须保证此表能反射 否则将创建失败
		 * @param classTable  字节码对象
         * @return
         */
		public boolean tableExistOrCreate(Class classTable){
			if(!tableExist(ReflectUtils.getTableNameByClass(classTable))){
					createTable(classTable);

				return false;
			}
			return true;
		}

		public void execSQL(String sql) {
			mDb.execSQL(sql);
		}

		public void close() {
			if(mDb.isOpen()){
				mDb.close();
				mDb=null;
			}
		}
		// public boolean
	}

}
