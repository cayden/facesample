/**
 * 
 */
package com.cayden.face.vlc;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name="UrlInfo")
public class UrlInfo {
	@Column(name="id", isId=true, autoGen=true)
	private int id;
	@Column(name="url", property="UNIQUE")
	private String url;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	@Override
	public String toString() {
		return url;
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}
}
