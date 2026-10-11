package com.ruoyi.system.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import com.ruoyi.system.domain.es.EsScenicSpot;

/**
 * 景点 ES 索引 Repository
 *
 * <p>继承 {@link ElasticsearchRepository} 即可获得 save/saveAll/findById/deleteById
 * 等基础读写能力；复杂的多字段布尔检索在 EsScenicSpotService 中
 * 通过 ElasticsearchOperations + NativeQuery 实现。</p>
 *
 * <p>扫描说明：启动类在 com.ruoyi 包下，Spring Boot 自动配置会扫描其所有子包，
 * 因此本接口放在 com.ruoyi.system.repository 即可被自动装配。</p>
 *
 * @author ruoyi
 * @date 2026-10-05
 */
@Repository
public interface EsScenicSpotRepository extends ElasticsearchRepository<EsScenicSpot, String>
{
}
