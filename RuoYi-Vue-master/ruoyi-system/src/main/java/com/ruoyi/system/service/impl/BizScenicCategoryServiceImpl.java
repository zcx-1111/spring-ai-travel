package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.BizScenicCategoryMapper;
import com.ruoyi.system.domain.BizScenicCategory;
import com.ruoyi.system.service.IBizScenicCategoryService;

/**
 * 景点分类Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@Service
public class BizScenicCategoryServiceImpl implements IBizScenicCategoryService 
{
    @Autowired
    private BizScenicCategoryMapper bizScenicCategoryMapper;

    /**
     * 查询景点分类
     * 
     * @param id 景点分类主键
     * @return 景点分类
     */
    @Override
    public BizScenicCategory selectBizScenicCategoryById(Long id)
    {
        return bizScenicCategoryMapper.selectBizScenicCategoryById(id);
    }

    /**
     * 查询景点分类列表
     * 
     * @param bizScenicCategory 景点分类
     * @return 景点分类
     */
    @Override
    public List<BizScenicCategory> selectBizScenicCategoryList(BizScenicCategory bizScenicCategory)
    {
        return bizScenicCategoryMapper.selectBizScenicCategoryList(bizScenicCategory);
    }

    /**
     * 新增景点分类
     * 
     * @param bizScenicCategory 景点分类
     * @return 结果
     */
    @Override
    public int insertBizScenicCategory(BizScenicCategory bizScenicCategory)
    {
        bizScenicCategory.setCreateTime(DateUtils.getNowDate());
        return bizScenicCategoryMapper.insertBizScenicCategory(bizScenicCategory);
    }

    /**
     * 修改景点分类
     * 
     * @param bizScenicCategory 景点分类
     * @return 结果
     */
    @Override
    public int updateBizScenicCategory(BizScenicCategory bizScenicCategory)
    {
        bizScenicCategory.setUpdateTime(DateUtils.getNowDate());
        return bizScenicCategoryMapper.updateBizScenicCategory(bizScenicCategory);
    }

    /**
     * 批量删除景点分类
     * 
     * @param ids 需要删除的景点分类主键
     * @return 结果
     */
    @Override
    public int deleteBizScenicCategoryByIds(Long[] ids)
    {
        return bizScenicCategoryMapper.deleteBizScenicCategoryByIds(ids);
    }

    /**
     * 删除景点分类信息
     * 
     * @param id 景点分类主键
     * @return 结果
     */
    @Override
    public int deleteBizScenicCategoryById(Long id)
    {
        return bizScenicCategoryMapper.deleteBizScenicCategoryById(id);
    }
}
