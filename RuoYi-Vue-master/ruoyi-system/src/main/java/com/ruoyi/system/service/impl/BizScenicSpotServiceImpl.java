package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.ai.EsScenicSpotService;
import com.ruoyi.system.mapper.BizScenicSpotMapper;
import com.ruoyi.system.domain.BizScenicSpot;
import com.ruoyi.system.service.IBizScenicSpotService;

/**
 * 景点主Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@Service
public class BizScenicSpotServiceImpl implements IBizScenicSpotService 
{
    @Autowired
    private BizScenicSpotMapper bizScenicSpotMapper;

    /** ES全文检索服务（增量同步用）；其内部已吞掉ES异常，不会影响MySQL主流程 */
    @Autowired
    private EsScenicSpotService esScenicSpotService;

    /**
     * 查询景点主
     * 
     * @param id 景点主主键
     * @return 景点主
     */
    @Override
    public BizScenicSpot selectBizScenicSpotById(Long id)
    {
        return bizScenicSpotMapper.selectBizScenicSpotById(id);
    }

    /**
     * 查询景点主列表
     * 
     * @param bizScenicSpot 景点主
     * @return 景点主
     */
    @Override
    public List<BizScenicSpot> selectBizScenicSpotList(BizScenicSpot bizScenicSpot)
    {
        return bizScenicSpotMapper.selectBizScenicSpotList(bizScenicSpot);
    }

    /**
     * 新增景点主
     * 
     * @param bizScenicSpot 景点主
     * @return 结果
     */
    @Override
    public int insertBizScenicSpot(BizScenicSpot bizScenicSpot)
    {
        bizScenicSpot.setCreateTime(DateUtils.getNowDate());
        int rows = bizScenicSpotMapper.insertBizScenicSpot(bizScenicSpot);
        // MySQL写成功后增量同步ES（ES异常在Service内部吞掉，不影响主流程）
        if (rows > 0)
        {
            esScenicSpotService.upsertBySpotId(bizScenicSpot.getId());
        }
        return rows;
    }

    /**
     * 修改景点主
     * 
     * @param bizScenicSpot 景点主
     * @return 结果
     */
    @Override
    public int updateBizScenicSpot(BizScenicSpot bizScenicSpot)
    {
        bizScenicSpot.setUpdateTime(DateUtils.getNowDate());
        int rows = bizScenicSpotMapper.updateBizScenicSpot(bizScenicSpot);
        // 修改成功后同步更新ES文档（含软删除场景：delFlag=1时upsert内部自动删ES文档）
        if (rows > 0)
        {
            esScenicSpotService.upsertBySpotId(bizScenicSpot.getId());
        }
        return rows;
    }

    /**
     * 批量删除景点主
     * 
     * @param ids 需要删除的景点主主键
     * @return 结果
     */
    @Override
    public int deleteBizScenicSpotByIds(Long[] ids)
    {
        int rows = bizScenicSpotMapper.deleteBizScenicSpotByIds(ids);
        // 物理删除后同步删除ES文档
        if (rows > 0 && ids != null)
        {
            for (Long id : ids)
            {
                esScenicSpotService.deleteBySpotId(id);
            }
        }
        return rows;
    }

    /**
     * 删除景点主信息
     * 
     * @param id 景点主主键
     * @return 结果
     */
    @Override
    public int deleteBizScenicSpotById(Long id)
    {
        int rows = bizScenicSpotMapper.deleteBizScenicSpotById(id);
        if (rows > 0)
        {
            esScenicSpotService.deleteBySpotId(id);
        }
        return rows;
    }
}
