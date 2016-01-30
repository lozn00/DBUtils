package com.example.mydbutils;

import java.util.ArrayList;
import java.util.List;

import com.example.mydbutils.domain.New;

import android.test.AndroidTestCase;
import android.util.Log;

public class MyTest extends AndroidTestCase {
	private static final String TAG = "MyTest";
	private DBUtils dbUtils;

	@Override
	protected void setUp() throws Exception {
		Log.i(TAG, "setUp");
		dbUtils = new DBUtils(getContext(), "itcast.db");
	}
	/**
	 * 测试创建表
	 */
	public void test()
	{
//		DBUtils dbUtils=new DBUtils(getContext(), "itcast.db");
		dbUtils.createTable(New.class);
		dbUtils.deleteTable(New.class);
		dbUtils.createTable(New.class);
//		dbUtils.createTable(klass);
	}
	public void testInsert()
	{
		New new1=new New(-1, "我喜欢你 啊啊啊啊啊啊新闻", "http://baidu.com", "并没有评论", 5);//-1为自增长
		long result = dbUtils.insert(new1);
		Log.i(TAG, "插入之后的index"+result);
	}
	public void testUpdate()
	{
		New new1=new New(1, "你的新闻", "http://baidu.com", "并没有评论", 5);//-1为自增长
		long result = dbUtils.update(new1);
		Log.i(TAG, "更新之后的index"+result);
	}
	public void testQueryAll()
	{
		Log.i(TAG, "测试");
//		New new1=new New(1, "你的新闻", "http://baidu.com", "并没有评论", 5);//-1为自增长
		 List<New> list = dbUtils.queryAll(New.class);
		Log.i(TAG, "查询所有:"+list);
	}
	/**
	 * 测试成功
	 */
	public void testQuery()
	{
//		New new1=new New(1, "你的新闻", "http://baidu.com", "并没有评论", 5);//-1为自增长
		  New new1 = dbUtils.queryByID(New.class, 1);
		Log.i(TAG, "查询id为1的:"+new1);
	}
	/**
	 * 测试成功，不过方法字段比较麻烦
	 */
	public void testQueryBy(){
	New	 new1=dbUtils.queryByColumn(New.class, "url", "http://baidu.com");
	}
}
