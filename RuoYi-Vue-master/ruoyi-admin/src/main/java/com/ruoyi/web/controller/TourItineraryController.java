package com.ruoyi.web.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.github.pagehelper.PageHelper;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.service.ITourItineraryService;

/**
 * 前台行程详情Controller
 * 提供行程分页列表、完整详情查询、行程删除
 *
 * @author ruoyi
 * @date 2026-09-21
 */
@RestController
@RequestMapping("/api/travel/itinerary")
public class TourItineraryController extends BaseController
{
    @Autowired
    private ITourItineraryService tourItineraryService;

    /**
     * 分页查询当前登录游客的行程方案列表
     * GET /api/travel/itinerary/list?pageNum=1&pageSize=6
     * 返回：{ code, rows: 行程卡片数组, total: 总条数 }
     */
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                              @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize)
    {
        Long userId = SecurityUtils.getUserId();
        // PageHelper 分页：紧跟的第一条查询SQL会被自动追加 limit，固定按创建时间倒序
        PageHelper.startPage(pageNum, pageSize, "create_time desc");
        List<Map<String, Object>> list = tourItineraryService.listMyPlans(userId);
        return getDataTable(list);
    }

    /**
     * 获取行程完整详情
     * GET /api/travel/itinerary/get?itineraryId=xxx
     * 返回：data = { itinerary: 基础信息, dayList: 每日行程, messages: 历史对话 }
     */
    @GetMapping("/get")
    public AjaxResult get(@RequestParam("itineraryId") Long itineraryId)
    {
        Long userId = SecurityUtils.getUserId();
        Map<String, Object> data = tourItineraryService.getItinerary(itineraryId, userId);
        return success(data);
    }

    /**
     * 删除行程（级联删除每日行程、景点关联与对话记录）
     * DELETE /api/travel/itinerary/remove?itineraryId=xxx
     */
    @DeleteMapping("/remove")
    public AjaxResult remove(@RequestParam("itineraryId") Long itineraryId)
    {
        Long userId = SecurityUtils.getUserId();
        int rows = tourItineraryService.removePlan(itineraryId, userId);
        return rows > 0 ? AjaxResult.success("行程删除成功") : AjaxResult.error("行程删除失败");
    }
}
