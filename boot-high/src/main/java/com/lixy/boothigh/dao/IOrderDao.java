/**  
 * ---------------------------------------------------------------------------
 * Copyright (c) 2017, lixy- All Rights Reserved.
 * Project Name:springboot-test  
 * File Name:IStudentDao.java  
 * Package Name:com.lixy.dao
 * Author   Joe
 * Date:2017年11月7日上午9:22:59
 * ---------------------------------------------------------------------------  
*/  
  
package com.lixy.boothigh.dao;

import com.lixy.boothigh.bean.Order;

import java.util.List;
import java.util.Map;

/**  
 * ClassName:IStudentDao 
 * Date:     2017年11月7日 上午9:22:59
 * @author   Joe  
 * @version    
 * @since    JDK 1.8
 */
public interface IOrderDao {

	/**
	 * findOrders:(查询List).  
	 * @author Joe
	 * Date:2017年11月7日上午9:30:37
	 *
	 * @param map
	 * @return
	 */
	List<Order> findOrders(Map<String, Object> map);
	
	/**
	 * addOrder:(增加).  
	 * @author Joe
	 * Date:2017年11月7日上午9:30:48
	 *
	 * @param order
	 * @return
	 */
	boolean addOrder(Order order);
	
}
  
