package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BizScenicCategory;

/**
 * 景点分类Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public interface BizScenicCategoryMapper 
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
     * 删除景点分类
     * 
     * @param id 景点分类主键
     * @return 结果
     */
    public int deleteBizScenicCategoryById(Long id);

    /**
     * 批量删除景点分类
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizScenicCategoryByIds(Long[] ids);
}
