/**
 * 
 */
package com.cayden.face.vlc;

import org.xutils.ex.DbException;

import java.util.List;


public class UrlInfoService extends AppBeanService<UrlInfo>{

	@Override
	public boolean isExist(UrlInfo object) {
		return false;
	}

	public List<UrlInfo> getAll(){
		return getAll(UrlInfo.class);
	}
	
	public void deleteAll(){
		try {
			UdpPlayerApplication.mDBManager.delete(UrlInfo.class);
		} catch (DbException e) {
			e.printStackTrace();
		}
	}
}
