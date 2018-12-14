package com.lixy.boothigh.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.lixy.boothigh.bean.DataBaseConfig;
import com.lixy.boothigh.dao.DataBaseConfigMapper;
import com.lixy.boothigh.excep.ServiceException;
import com.lixy.boothigh.service.GenCommonService;
import com.lixy.boothigh.utils.GenDBUtils;
import com.lixy.boothigh.vo.ConditionCountVo;
import com.lixy.boothigh.vo.ConditionPageVo;
import com.lixy.boothigh.vo.page.ColumnInfoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author: MR LIS
 * @Description:公共服务接口实现
 * @Date: Create in 14:46 2018/5/25
 * @Modified By:
 */
@Transactional
@Service
public class GenCommonServiceImpl implements GenCommonService {

    private Logger logger = LoggerFactory.getLogger(GenCommonServiceImpl.class);

    @Autowired
    private DataBaseConfigMapper configMapper;

    @Override
    public List<ColumnInfoVO> getAllColumnInfo(Integer dbId, String tableName) throws ServiceException {
        DataBaseConfig DataBaseConfig = configMapper.selectOne(dbId);
        //表头
        List<ColumnInfoVO> columnInfoVOList = GenDBUtils.getAllColumnInfo(DataBaseConfig, tableName);
        logger.info("dbId={},tableName={},columnInfoVOList = {}", dbId, tableName, JSONObject.toJSONString(columnInfoVOList));
        return columnInfoVOList;
    }

    @Override
    public int executePageTotalCount(Integer dbId, String tableName) throws ServiceException {
        DataBaseConfig DataBaseConfig = configMapper.selectOne(dbId);
        //表总记录数
        int totalCount = GenDBUtils.executePageTotalCount(DataBaseConfig, tableName);
        logger.info("dbId={},tableName={},总记录数 = {}", dbId, tableName, totalCount);
        return totalCount;
    }

    @Override
    public List<List<Object>> executePageQueryNotCount(Integer dbId, String tableName, Integer pageNum, Integer pageSize) throws ServiceException {
        int start = (pageNum-1)*pageSize;
        int end = pageSize*pageNum;
        DataBaseConfig dataBaseConfig = configMapper.selectOne(dbId);
        return GenDBUtils.executePage(dataBaseConfig, tableName,pageSize, start, end);
    }


    @Override
    public int executePageTotalCountWithCondition(ConditionCountVo countVo) throws ServiceException {
        DataBaseConfig DataBaseConfig = configMapper.selectOne(countVo.getDbId());
        //表总记录数
        int totalCount = GenDBUtils.executePageTotalCountWithCondition(DataBaseConfig, countVo.getTableName(),countVo.getConditionVos());
        logger.info("dbId={},tableName={},总记录数 = {}", countVo.getDbId(), countVo.getTableName(), totalCount);
        return totalCount;
    }

    @Override
    public List<List<Object>> executePageQueryNotCountWithCondition(ConditionPageVo pageVo) throws ServiceException {
        int start = (pageVo.getPageNum()-1)*pageVo.getPageSize();
        int end = pageVo.getPageSize()*pageVo.getPageNum();
        DataBaseConfig DataBaseConfig = configMapper.selectOne(pageVo.getDbId());
        return GenDBUtils.executePageWithCondition(DataBaseConfig, pageVo.getTableName(),pageVo.getConditionVos(),pageVo.getPageSize(), start, end);
    }
    @Override
    public  List<List<Object>> executePageQueryColumnRecord(Integer dbId, String tableName,String columnArr, Integer pageNum, Integer pageSize) throws ServiceException {
        int start = (pageNum-1)*pageSize;
        int end = pageSize*pageNum;
        DataBaseConfig dataBaseConfig = configMapper.selectOne(dbId);
        return GenDBUtils.executePage(dataBaseConfig,tableName,columnArr,pageSize, start, end);
    }
    @Override
    public void dropTable(Integer dbId, String tableName) throws ServiceException {
        DataBaseConfig dataBaseConfig = configMapper.selectOne(dbId);
        GenDBUtils.dropTable(dataBaseConfig, tableName);
    }
}
