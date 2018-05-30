package com.lixy.boothigh.service;


import com.lixy.boothigh.excep.ServiceException;
import com.lixy.boothigh.vo.page.ColumnInfoVO;
import com.lixy.boothigh.vo.page.SandPageViewVO;

import java.util.List;

/**
 * @Author: MR LIS
 * @Description:沙盘公共服务接口
 * @Date: Create in 14:45 2018/5/25
 * @Modified By:
 */
public interface GenCommonService {
    /**
     * @Author: MR LIS
     * @Description:获取数据库表的字段名、注释、数据类型
     * @param dbId 数据库id
     * @param tableName 表名
     * @Date: 14:50 2018/5/25
     * @return
     */
    List<ColumnInfoVO> getAllColumnInfo(Integer dbId, String tableName) throws ServiceException;

    /**
     * @Author: MR LIS
     * @Description: 根据dbId，tableName执行分页查询，包含总记录数
     * @Date: 14:54 2018/5/25
     * @return
     */
    SandPageViewVO executePageQuery(Integer dbId, String tableName, int pageNum, int pageSize)throws ServiceException;
    /**
     * @Author: MR LIS
     * @Description: 根据dbId，tableName执行分页查询，不进行总记录数的查询
     * @Date: 14:54 2018/5/25
     * @return
     */
    List<List<Object>> executePageQueryNotCount(Integer dbId, String tableName, Integer pageNum, Integer pageSize)throws ServiceException;;
}