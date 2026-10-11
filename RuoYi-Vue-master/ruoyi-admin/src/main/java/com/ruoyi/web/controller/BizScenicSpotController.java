package com.ruoyi.web.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.BizScenicSpot;
import com.ruoyi.system.service.IBizScenicSpotService;
import com.ruoyi.system.service.impl.ScenicCsvImportService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 景点主Controller
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@RestController
@RequestMapping("/system/spot")
public class BizScenicSpotController extends BaseController
{
    @Autowired
    private IBizScenicSpotService bizScenicSpotService;

    @Autowired
    private ScenicCsvImportService scenicCsvImportService;

    /**
     * 查询景点主列表
     */
    @PreAuthorize("@ss.hasPermi('system:spot:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizScenicSpot bizScenicSpot)
    {
        startPage();
        List<BizScenicSpot> list = bizScenicSpotService.selectBizScenicSpotList(bizScenicSpot);
        return getDataTable(list);
    }

    /**
     * 导出景点主列表
     */
    @PreAuthorize("@ss.hasPermi('system:spot:export')")
    @Log(title = "景点主", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizScenicSpot bizScenicSpot)
    {
        List<BizScenicSpot> list = bizScenicSpotService.selectBizScenicSpotList(bizScenicSpot);
        ExcelUtil<BizScenicSpot> util = new ExcelUtil<BizScenicSpot>(BizScenicSpot.class);
        util.exportExcel(response, list, "景点主数据");
    }

    /**
     * 获取景点主详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:spot:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(bizScenicSpotService.selectBizScenicSpotById(id));
    }

    /**
     * 新增景点主
     */
    @PreAuthorize("@ss.hasPermi('system:spot:add')")
    @Log(title = "景点主", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizScenicSpot bizScenicSpot)
    {
        return toAjax(bizScenicSpotService.insertBizScenicSpot(bizScenicSpot));
    }

    /**
     * 修改景点主
     */
    @PreAuthorize("@ss.hasPermi('system:spot:edit')")
    @Log(title = "景点主", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizScenicSpot bizScenicSpot)
    {
        return toAjax(bizScenicSpotService.updateBizScenicSpot(bizScenicSpot));
    }

    /**
     * 删除景点主
     */
    @PreAuthorize("@ss.hasPermi('system:spot:remove')")
    @Log(title = "景点主", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bizScenicSpotService.deleteBizScenicSpotByIds(ids));
    }

    /**
     * 导入景点CSV到知识库
     * 接收CSV文件（去哪儿网格式）+ 省份 + 城市，自动清洗数据后批量入库并同步Qdrant向量库
     */
    @PreAuthorize("@ss.hasPermi('system:spot:add')")
    @Log(title = "景点知识库", businessType = BusinessType.IMPORT)
    @PostMapping("/importCsv")
    public AjaxResult importCsv(@RequestParam("file") MultipartFile file,
            @RequestParam("province") String province,
            @RequestParam("city") String city)
    {
        String result = scenicCsvImportService.importCsv(file, province, city);
        return AjaxResult.success(result);
    }
}
