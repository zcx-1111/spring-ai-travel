package com.ruoyi.system.domain.dto;

/**
 * 前台多轮对话修改行程请求参数
 *
 * @author ruoyi
 * @date 2026-09-21
 */
public class TourChatDTO
{
    /** 行程方案ID（biz_travel_plan.id） */
    private Long itineraryId;

    /** 用户本轮提问内容 */
    private String userMessage;

    public Long getItineraryId()
    {
        return itineraryId;
    }

    public void setItineraryId(Long itineraryId)
    {
        this.itineraryId = itineraryId;
    }

    public String getUserMessage()
    {
        return userMessage;
    }

    public void setUserMessage(String userMessage)
    {
        this.userMessage = userMessage;
    }
}
