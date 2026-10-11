package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BizScenicCategory;

/**
 * 景点分类Service接口
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public interface IBizScenicCategoryService 
{
    /**
     * 查询景点分类
     * 
     * @param id 景点分类主键
     * @return 景点分类
     */
    public BizScenicCategory selectBizScenicCategoryById(Long id);

    /**
     * 查询景点分类列表
     * 
     * @param bizScenicCategory 景点分类
     * @return 景点分类集合
     */
    public List<BizScenicCategory> selectBizScenicCategoryList(BizScenicCategory bizScenicCategory);

    /**
     * 新增景点分类
     * 
     * @param bizScenicCategory 景点分类
     * @return 结果
     */
    public int insertBizScenicCategory(BizScenicCategory bizScenicCategory);

    /**
     * 修改景点分类
     * 
     * @param bizScenicCategory 景点分类
     * @return 结果
     */
    public int updateBizScenicCategory(BizScenicCategory bizScenicCategory);

    /**
     * 批量删除景点分类
     * 
     * @param ids 需要删除的景点分类主键集合
     * @return 结果
     */
    public int deleteBizScenicCategoryByIds(Long[] ids);

    /**
     * 删除景点分类信息
     * 
     * @param id 景点分类主键
     * @return 结果
     */
    public int deleteBizScenicCategoryById(Long id);
}
