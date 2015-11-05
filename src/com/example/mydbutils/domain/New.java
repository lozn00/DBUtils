package com.example.mydbutils.domain;

import com.example.mydbutils.R;
import com.example.mydbutils.anotation.Column;
import com.example.mydbutils.anotation.ID;
import com.example.mydbutils.anotation.Table;
@Table("News")
public class New {
	@ID
	int id;
	@Column("mingcheng")
	String title;
	String url;
	String commment;
	int count;
	/**
	 * 创建的时候必须有一个空参数构造，由于反射里面做了try处理 ，结合里面new的新实例是空参数的，多条数据存在查询出来是多个null
	 */
	public New() {
	}
	public New(int id, String title, String url, String commment, int count) {
		super();
		this.id = id;
		this.title = title;
		this.url = url;
		this.commment = commment;
		this.count = count;
	}
	@Override
	public String toString() {
		return "New [id=" + id + ", title=" + title + ", url=" + url + ", commment=" + commment + ", count=" + count + "]\n";
	}
	
	
}
