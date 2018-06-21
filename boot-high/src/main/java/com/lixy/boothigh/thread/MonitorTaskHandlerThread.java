package com.lixy.boothigh.thread;

import com.lixy.boothigh.constants.BConstant;
import com.lixy.boothigh.service.RedisService;
import com.lixy.boothigh.utils.IPUtil;
import com.lixy.boothigh.vo.TaskHandleVO;
import com.lixy.boothigh.vo.TaskScanVO;
import com.lixy.boothigh.websocket.WebSocketEndPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @Author: MR LIS
 * @Description: TaskScanVO对象获取上传附件id,根据上传附件id去redis中获取对象，如果状态都为true,推送消息到前端
 * @Date: Create in 16:25 2018/6/14
 * @Modified By:
 */
@Service(value = "monitorTaskHandlerThread")
public class MonitorTaskHandlerThread implements Runnable{
    private final static Logger logger = LoggerFactory.getLogger(MonitorTaskHandlerThread.class);
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisService redisService;
    @Autowired
    private WebSocketEndPoint endPoint;

    @Override
    public void run() {
        ListOperations<String, TaskScanVO> opsList = redisTemplate.opsForList();
        TaskScanVO scanVO=null;
        try {
            while (true) {
                //两分钟弹出一个数据，如果两分钟内没有数据，等待
                scanVO = opsList.rightPop(BConstant.TEMPLATE_REDIS_TASK_SCAN_KEY);
//                logger.info("MonitorTaskHandlerThread类 uploadPathId=[{}]",uploadPathId);
                //扫描对象为空或上传文件id为空，进行等待
                if (scanVO==null||Objects.isNull(scanVO.getUploadPathId())) {
                    logger.info("MonitorTaskHandlerThread类 队列数据为空，waiting 30 seconds");
                    //等待30秒
                    Thread.sleep(30_000);
                    continue;
                }

                //ip地址非本机,重新放入，进行等待
                if (!scanVO.getIp().equals(IPUtil.getLocalIP())) {
                    logger.info("MonitorTaskHandlerThread类 ip地址非本机，waiting 30 seconds");
                    opsList.leftPush(BConstant.TEMPLATE_REDIS_TASK_SCAN_KEY, scanVO);
                    //等待30秒
                    Thread.sleep(30_000);
                    continue;
                }



                TaskHandleVO taskHandleVO = redisService.getRedisKeyToTaskVO(BConstant.TEMPLATE_REDIS_HANDLER_KEY+scanVO.getUploadPathId());
                //对象为空，不需要将scanVO重新放入
                if (taskHandleVO == null) {
                    logger.info("MonitorTaskHandlerThread类 任务对象为空");
                    continue;
                }

                //如果es和tidb均完成了同步，发送通知
                if (taskHandleVO.isEsFlag() && taskHandleVO.isTiFlag()) {
                    redisService.removeKey(BConstant.TEMPLATE_REDIS_HANDLER_KEY + scanVO.getUploadPathId());
                    logger.info("发送通知");
                    //服务器端推送消息到前端
                    endPoint.sendToUser("task is done！|用户1");
                    continue;

                }

                //未完成同步时将扫描任务重新放入队列
                opsList.leftPush(BConstant.TEMPLATE_REDIS_TASK_SCAN_KEY, scanVO);
                Thread.sleep(10_000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("任务处理异常：",e.getMessage());
        }finally {
            if(scanVO!=null&&scanVO.getUploadPathId()!=null)
            //异常时将其放入redis,防止数据丢失
            opsList.leftPush(BConstant.TEMPLATE_REDIS_TASK_SCAN_KEY, scanVO);
        }
    }
}