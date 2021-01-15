package com.elasticsearch.elasticsearch.service;

import java.util.List;
import java.util.Map;

public interface ESService<T> {

    /**
     * 创建mapping
     *
     * @param indexName          索引名称(LowerCase)
     * @param clazz              索引类
     * @param dropOldIndex       是否删除旧索引
     * @param number_of_shards   分片数
     * @param number_of_replicas 备份数
     * @return
     */
    boolean createIndexAndCreateMapping(String indexName, Class clazz, boolean dropOldIndex, int number_of_shards, int number_of_replicas);

    /**
     * 判断索引是否存在
     *
     * @param indexName 索引名称(LowerCase)
     * @return
     */
    boolean isIndexExists(String indexName);

    /**
     * 删除索引
     */
    boolean deleteIndex(String indexName);

    /**
     * 单个创建文档
     */
    boolean createDocument(String indexName, Long id, T entity);

    /**
     * 单个修改文档
     */
    boolean updateDocument(String indexName, Long id, T entity);

    /**
     * 单个删除文档
     */
    boolean deleteDocumentById(String indexName, Long id);

    /**
     * 批量创建文档
     *
     * @param maps key:id ,value:T->entity
     */
    boolean bulkCreateDocument(String indexName, Map<Long, T> maps);

    /**
     * 批量修改文档
     *
     * @param maps key:id ,value:T->entity
     */
    boolean bulkUpdateDocument(String indexName, Map<Long, T> maps);

    /**
     * 批量删除文档
     *
     * @param ids
     */
    boolean bulkDeleteDocumentByIds(String indexName, List<Long> ids);

    /**
     * 全词搜索
     *
     * @param fieldValue 值
     * @param current    当前页码
     * @param size       页容量
     * @param fieldNames 支持全词搜索的字段
     * @return
     */
    List<Map<String, Object>> textSearch(String indexName, String orderField, int current, int size, String fieldValue, String... fieldNames);

    /**
     * 范围搜索
     *
     * @param maps<key(支持范围搜索的字段),<"start(开始)/end(截止)","2020-12-11">>
     * @return
     */
    List<Map<String, Object>> rangeMathQuery(String indexName, String orderField, int current, int size, List<Map<String, Map<String, String>>> maps, String fieldValue, String... fieldNames);

    /**
     * 全词搜索总条数
     *
     * @param value      值
     * @param fieldNames 支持全词搜索的字段
     * @return
     */
    Long textCount(String indexName, String value, String... fieldNames);

    /**
     * 根据id查找
     */
    Map<String, Object> getDocumentById(String indexName, String id);
}
