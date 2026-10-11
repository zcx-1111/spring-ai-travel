package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.BizScenicTagMapper;
import com.ruoyi.system.domain.BizScenicTag;
import com.ruoyi.system.service.IBizScenicTagService;

/**
 * 景点标签Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@Service
public class BizScenicTagServiceImpl implements IBizScenicTagService 
{
    @Autowired
    private BizScenicTagMapper bizScenicTagMapper;

    /**
     * 查询景点标签
     * 
     * @param id 景点标签主键
     * @return 景点标签
     */
    @Override
    public BizScenicTag selectBizScenicTagById(Long id)
    {
        return bizScenicTagMapper.selectBizScenicTagById(id);
    }

    /**
     * 查询景点标签列表
     * 
     * @param bizScenicTag 景点标签
     * @return 景点标签
     */
    @Override
    public List<BizScenicTag> selectBizScenicTagList(BizScenicTag bizScenicTag)
    {
        return bizScenicTagMapper.selectBizScenicTagList(bizScenicTag);
    }

    /**
     * 新增景点标签
     * 
     * @param bizScenicTag 景点标签
     * @return 结果
     */
    @Override
    public int insertBizScenicTag(BizScenicTag bizScenicTag)
    {
        bizScenicTag.setCreateTime(DateUtils.getNowDate());
        return bizScenicTagMapper.insertBizScenicTag(bizScenicTag);
    }

    /**
     * 修改景点标签
     * 
     * @param bizScenicTag 景点标签
     * @return 结果
     */
    @Override
    public int updateBizScenicTag(BizScenicTag bizScenicTag)
    {
        bizScenicTag.setUpdateTime(DateUtils.getNowDate());
        return bizScenicTagMapper.updateBizScenicTag(bizScenicTag);
    }

    /**
     * 批量删除景点标签
     * 
     * @param ids 需要删除的景点标签主键
     * @return 结果
     */
    @Override
    public int deleteBizScenicTagByIds(Long[] ids)
    {
        return bizScenicTagMapper.deleteBizScenicTagByIds(ids);
    }

    /**
     * 删除景点标签信息
     * 
     * @param id 景点标签主键
     * @return 结果
     */
    @Override
    public int deleteBizScenicTagById(Long id)
    {
        return bizScenicTagMapper.deleteBizScenicTagById(id);
    }
}
