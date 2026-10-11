package com.ruoyi.system.ai;

import java.util.HashMap;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 旅游AI对话客户端配置
 * 基于 Spring AI 自动装配的 ChatClient.Builder（底层走阿里云百炼 DashScope OpenAI 兼容模式），
 * 统一设置旅游规划师的System系统提示词与生成参数，约束大模型：
 * 1. 只能使用Qdrant向量库召回的景点知识库信息，禁止编造不存在的景点；
 * 2. 严格输出结构化JSON行程，方便后端解析落库；
 * 3. 关闭深度推理 + 限制输出长度，把单次生成控制在 10~20 秒内，避免前端超时。
 *
 * @author ruoyi
 * @date 2026-09-21
 */
@Configuration
public class TravelChatClientConfig
{
    /**
     * 旅游规划专用ChatClient（@Qualifier与其他业务ChatClient隔离）
     */
    @Bean("travelChatClient")
    public ChatClient travelChatClient(ChatClient.Builder builder)
    {
        // 通过 extraBody 透传百炼专属参数：enable_thinking=false 关闭深度推理，
        // 实测可把 qwen3.8-max 单次生成从 119~178秒 降到 10秒左右，且输出结构完整
        Map<String, Object> extraBody = new HashMap<>();
        extraBody.put("enable_thinking", false);

        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                // 限制单次输出最大token，行程JSON约 600~1000 token，1500 足够且避免超长拖慢
                .maxTokens(1500)
                .temperature(0.7)
                .extraBody(extraBody);

        return builder
                // 注意：Spring AI 2.0.1 的 defaultOptions 接受 ChatOptions.Builder，不要 .build()
                .defaultOptions(optionsBuilder)
                .defaultSystem("""
                        你是一名资深旅游规划师，服务于AI智能旅游规划助手系统。
                        请严格遵守以下规则：
                        1. 只能使用用户提供的【景点知识库】中存在的景点安排行程，严禁编造任何知识库中没有的景点、酒店或餐厅名称；
                        2. 行程安排要符合实际：每天1-2个主要景点，考虑景点开放时间、地理位置顺路与出行人群特点；
                        3. 预算、住宿、交通建议要与用户选择的消费档次匹配；
                        4. 当要求输出JSON时，只能输出JSON本身，不要输出Markdown代码块、不要输出任何解释性文字；
                        5. 多轮对话中只输出需要变化的内容依据用户要求调整，未要求调整的日期保持原有安排；
                        6. 语言简洁、亲切，面向普通游客，不要使用管理员后台口吻。
                        """)
                .build();
    }
}
