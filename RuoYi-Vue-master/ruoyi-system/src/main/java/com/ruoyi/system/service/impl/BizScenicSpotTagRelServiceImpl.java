package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.BizScenicSpotTagRelMapper;
import com.ruoyi.system.domain.BizScenicSpotTagRel;
import com.ruoyi.system.service.IBizScenicSpotTagRelService;

/**
 * 景点-标签关联Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@Service
public class BizScenicSpotTagRelServiceImpl implements IBizScenicSpotTagRelService 
{
    @Autowired
    private BizScenicSpotTagRelMapper bizScenicSpotTagRelMapper;

    /**
     * 查询景点-标签关联
     * 
     * @param id 景点-标签关联主键
     * @return 景点-标签关联
     */
    @Override
    public BizScenicSpotTagRel selectBizScenicSpotTagRelById(Long id)
    {
        return bizScenicSpotTagRelMapper.selectBizScenicSpotTagRelById(id);
    }

    /**
     * 查询景点-标签关联列表
     * 
     * @param bizScenicSpotTagRel 景点-标签关联
     * @return 景点-标签关联
     */
    @Override
    public List<BizScenicSpotTagRel> selectBizScenicSpotTagRelList(BizScenicSpotTagRel bizScenicSpotTagRel)
    {
        return bizScenicSpotTagRelMapper.selectBizScenicSpotTagRelList(bizScenicSpotTagRel);
    }

    /**
     * 新增景点-标签关联
     * 
     * @param bizScenicSpotTagRel 景点-标签关联
     * @return 结果
     */
    @Override
    public int insertBizScenicSpotTagRel(BizScenicSpotTagRel bizScenicSpotTagRel)
    {
        bizScenicSpotTagRel.setCreateTime(DateUtils.getNowDate());
        return bizScenicSpotTagRelMapper.insertBizScenicSpotTagRel(bizScenicSpotTagRel);
    }

    /**
     * 修改景点-标签关联
     * 
     * @param bizScenicSpotTagRel 景点-标签关联
     * @return 结果
     */
    @Override
    public int updateBizScenicSpotTagRel(BizScenicSpotTagRel bizScenicSpotTagRel)
    {
        bizScenicSpotTagRel.setUpdateTime(DateUtils.getNowDate());
        return bizScenicSpotTagRelMapper.updateBizScenicSpotTagRel(bizScenicSpotTagRel);
    }

    /**
     * 批量删除景点-标签关联
     * 
     * @param ids 需要删除的景点-标签关联主键
     * @return 结果
     */
    @Override
    public int deleteBizScenicSpotTagRelByIds(Long[] ids)
    {
        return bizScenicSpotTagRelMapper.deleteBizScenicSpotTagRelByIds(ids);
    }

    /**
     * 删除景点-标签关联信息
     * 
     * @param id 景点-标签关联主键
     * @return 结果
     */
    @Override
    public int deleteBizScenicSpotTagRelById(Long id)
    {
        return bizScenicSpotTagRelMapper.deleteBizScenicSpotTagRelById(id);
    }
}
