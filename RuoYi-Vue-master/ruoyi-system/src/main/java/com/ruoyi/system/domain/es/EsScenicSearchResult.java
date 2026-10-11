package com.ruoyi.system.domain.es;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.system.domain.BizScenicSpot;

/**
 * 景点关键词检索分页结果
 *
 * <p>同时承载 ES 检索与 MySQL 降级检索的返回，前端无需感知当前走的是哪条链路，
 * source 字段仅用于后端排查与前端调试展示。</p>
 *
 * @author ruoyi
 * @date 2026-10-05
 */
public class EsScenicSearchResult
{
    /** 命中总条数 */
    private long total;

    /** 当前页码（从1开始） */
    private int pageNum;

    /** 每页条数 */
    private int pageSize;

    /** 当前页景点数据（实体字段完整，由ES命中ID回查MySQL得到，保证字段与现有业务一致） */
    private List<BizScenicSpot> rows = new ArrayList<>();

    /** 数据来源：elasticsearch=ES全文检索，mysql=ES不可用时降级的MySQL模糊查询 */
    private String source;

    public EsScenicSearchResult()
    {
    }

    public EsScenicSearchResult(long total, int pageNum, int pageSize, List<BizScenicSpot> rows, String source)
    {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.rows = rows;
        this.source = source;
    }

    public long getTotal()
    {
        return total;
    }

    public void setTotal(long total)
    {
        this.total = total;
    }

    public int getPageNum()
    {
        return pageNum;
    }

    public void setPageNum(int pageNum)
    {
        this.pageNum = pageNum;
    }

    public int getPageSize()
    {
        return pageSize;
    }

    public void setPageSize(int pageSize)
    {
        this.pageSize = pageSize;
    }

    public List<BizScenicSpot> getRows()
    {
        return rows;
    }

    public void setRows(List<BizScenicSpot> rows)
    {
        this.rows = rows;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }
}
