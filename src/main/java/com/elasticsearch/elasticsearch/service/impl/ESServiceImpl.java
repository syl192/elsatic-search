package com.elasticsearch.elasticsearch.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.elasticsearch.elasticsearch.annotation.FieldInfo;
import com.elasticsearch.elasticsearch.document.FieldMapping;
import com.elasticsearch.elasticsearch.service.ESService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.CcrClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ESServiceImpl implements ESService<Object> {

    private final RestHighLevelClient restClient;

    @Override
    public boolean createIndexAndCreateMapping(String indexName, Class clazz, boolean dropOldIndex, int number_of_shards, int number_of_replicas) {
        if (isIndexExists(indexName)) {
            if (dropOldIndex) {
                DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
                try {
                    AcknowledgedResponse deleteIndexResponse = restClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
                    if (deleteIndexResponse.isAcknowledged()) {
                        return createIndexAndCreateMapping(indexName, getFieldInfo(clazz), number_of_shards, number_of_replicas);
                    }
                } catch (IOException e) {
                    log.error("\n删除旧索引数据失败，创建索引mapping失败");
                    return false;
                }
            }
            log.info("\n不需要删除旧的索引，没有进一步创建索引结构哦~");
            return true;
        } else {
            return createIndexAndCreateMapping(indexName, getFieldInfo(clazz), number_of_shards, number_of_replicas);
        }
    }

    @Override
    public boolean isIndexExists(String indexName) {
        GetIndexRequest request = new GetIndexRequest(indexName);
        try {
            return restClient.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("\n判断索引:{}是否存在失败", indexName);
            return false;
        }
    }

    @Override
    public boolean deleteIndex(String indexName) {
        if (isIndexExists(indexName)) {
            // 创建DeleteIndexRequest 接受 index(索引名) 参数
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
            try {
                AcknowledgedResponse deleteIndexResponse = restClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
                return deleteIndexResponse.isAcknowledged();
            } catch (IOException e) {
                log.error("\n删除索引:{}数据失败", indexName);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean createDocument(String indexName, Long id, Object entity) {
        String JSONStr = JSON.toJSONStringWithDateFormat(entity, "yyyy-MM-dd HH:mm:ss", SerializerFeature.WriteDateUseDateFormat);
        IndexRequest request = new IndexRequest(indexName);
        request.opType(DocWriteRequest.OpType.INDEX);
        request.id(id.toString()).source(JSONStr, XContentType.JSON);
        try {
            IndexResponse response = restClient.index(request, RequestOptions.DEFAULT);
            return response.status().equals(RestStatus.OK);
        } catch (ElasticsearchStatusException | IOException e) {
            log.error("\n单个创建文档:{}数据失败,索引:{},id:{}", JSONStr, indexName, id);
            return false;
        }
    }

    @Override
    public boolean updateDocument(String indexName, Long id, Object entity) {
        Map<String, Object> documentById = getDocumentById(indexName, id.toString());
        String JSONStr = JSON.toJSONStringWithDateFormat(entity, "yyyy-MM-dd HH:mm:ss", SerializerFeature.WriteDateUseDateFormat);
        if (null != documentById && 0 != documentById.size()) {
            UpdateRequest updateRequest = new UpdateRequest(indexName, id.toString());
            updateRequest.doc(JSONStr, XContentType.JSON);
            try {
                UpdateResponse update = restClient.update(updateRequest, RequestOptions.DEFAULT);
                return update.status().equals(RestStatus.OK);
            } catch (IOException e) {
                log.error("\n单个修改文档数据失败,索引:{},id:{},JSONStr:{}", indexName, JSONUtil.toJsonStr(JSONStr));
                return false;
            }
        } else {
            return createDocument(indexName, id, entity);
        }
    }

    @Override
    public boolean deleteDocumentById(String indexName, Long id) {
        DeleteRequest deleteRequest = new DeleteRequest(indexName, id.toString());
        try {
            DeleteResponse delete = restClient.delete(deleteRequest, RequestOptions.DEFAULT);
            return delete.status().equals(RestStatus.OK);
        } catch (IOException e) {
            log.error("\n单个删除文档数据失败,索引:{},id:{}", indexName, id);
            return false;
        }
    }

    @Override
    public Map<String, Object> getDocumentById(String indexName, String id) {
        GetRequest request = new GetRequest(indexName, id);
        try {
            GetResponse response = restClient.get(request, RequestOptions.DEFAULT);
            return response.getSourceAsMap();
        } catch (IOException e) {
            log.error("查找索引:{}id:{}的文档失败", indexName, id);
            return null;
        }
    }

    @Override
    public boolean bulkCreateDocument(String indexName, Map<Long, Object> maps) {
        BulkRequest bulkRequest = new BulkRequest();
        Iterator<Map.Entry<Long, Object>> iterator = maps.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Object> next = iterator.next();
            IndexRequest indexRequest = new IndexRequest(indexName)
                    .id(next.getKey().toString())
                    .source(JSON.toJSONStringWithDateFormat(next.getValue(), "yyyy-MM-dd HH:mm:ss", SerializerFeature.WriteDateUseDateFormat), XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        try {
            BulkResponse bulkResponse = restClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            return bulkResponse.status().equals(RestStatus.OK);
        } catch (IOException e) {
            log.error("\n批量创建文档数据失败,索引:{},maps:{}", indexName, maps);
            return false;
        }
    }

    @Override
    public boolean bulkUpdateDocument(String indexName, Map<Long, Object> maps) {
        BulkRequest bulkRequest = new BulkRequest();
        Iterator<Map.Entry<Long, Object>> iterator = maps.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Object> next = iterator.next();
            String key = next.getKey().toString();
            String value = JSON.toJSONStringWithDateFormat(next.getValue(), "yyyy-MM-dd HH:mm:ss", SerializerFeature.WriteDateUseDateFormat);
            Map<String, Object> documentById = getDocumentById(indexName, key);
            if (null != documentById && 0 != documentById.size()) {
                bulkRequest.add(new UpdateRequest(indexName, key).doc(value, XContentType.JSON));
            } else {
                bulkRequest.add(new IndexRequest(indexName).id(key).source(value, XContentType.JSON));
            }
        }
        try {
            BulkResponse bulkResponse = restClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            return bulkResponse.status().equals(RestStatus.OK);
        } catch (IOException e) {
            log.error("\n批量创建文档数据失败,索引:{},maps:{}", indexName, maps);
            return false;
        }
    }

    @Override
    public boolean bulkDeleteDocumentByIds(String indexName, List ids) {
        BulkRequest bulkRequest = new BulkRequest();
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i).toString();
            bulkRequest.add(new DeleteRequest(indexName, id));
        }
        try {
            BulkResponse bulkResponse = restClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            return bulkResponse.status().equals(RestStatus.OK);
        } catch (IOException e) {
            log.error("\n批量删除文档数据失败,索引:{},ids:{}", indexName, ids);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> textSearch(String indexName, String orderField, int current, int size, String fieldValue, String... fieldNames) {
        current = (current - 1) * size;
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(indexName);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.multiMatchQuery(fieldValue, fieldNames));
            searchSourceBuilder.from(current);
            searchSourceBuilder.size(size);
            if (StrUtil.isNotBlank(orderField)) {
                searchSourceBuilder.sort(new FieldSortBuilder(orderField).order(SortOrder.DESC)); //指定字段倒叙
            }
            searchRequest.source(searchSourceBuilder);
            SearchResponse search = restClient.search(searchRequest, RequestOptions.DEFAULT);
            log.info(JSONUtil.toJsonStr(search));
            for (SearchHit hit : search.getHits().getHits()) {
                Map<String, Object> map = hit.getSourceAsMap();
                result.add(map);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> rangeMathQuery(String indexName, String orderField, int current, int size, List<Map<String, Map<String, String>>> maps, String fieldValue, String... fieldNames) {
        current = (current - 1) * size;
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(indexName);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.multiMatchQuery(fieldValue, fieldNames));
            for (int i = 0; i < maps.size(); i++) {
                Map<String, Map<String, String>> stringMapMap = maps.get(i);
                Iterator<Map.Entry<String, Map<String, String>>> iterator = stringMapMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    RangeQueryBuilder rangeQueryBuilder;
                    Map.Entry<String, Map<String, String>> next = iterator.next();
                    rangeQueryBuilder = QueryBuilders.rangeQuery(next.getKey());
                    Map<String, String> value = next.getValue();
                    if (value.containsKey("start")) {
                        String start = value.get("start");
                        if (StrUtil.isNotBlank(start))
                            rangeQueryBuilder.gte(start);
                    }
                    if (value.containsKey("end")) {
                        String end = value.get("end");
                        if (StrUtil.isNotBlank(end))
                            rangeQueryBuilder.lte(end);
                    }
                    searchSourceBuilder.postFilter(rangeQueryBuilder);
                }
            }
            searchSourceBuilder.from(current);
            searchSourceBuilder.size(size);
            if (StrUtil.isNotBlank(orderField)) {
                searchSourceBuilder.sort(new FieldSortBuilder(orderField).order(SortOrder.DESC)); //指定字段倒叙
            }
            searchRequest.source(searchSourceBuilder);
            SearchResponse search = restClient.search(searchRequest, RequestOptions.DEFAULT);
            for (SearchHit hit : search.getHits().getHits()) {
                Map<String, Object> map = hit.getSourceAsMap();
                result.add(map);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    @Override
    public Long textCount(String indexName, String value, String... fieldNames) {
        Long result = null;
        try {
            CountRequest countRequest = new CountRequest();
            countRequest.indices(indexName);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.multiMatchQuery(value, fieldNames));
            countRequest.source(searchSourceBuilder);
            CountResponse count = restClient.count(countRequest, RequestOptions.DEFAULT);
            result = count.getCount();
            log.info(JSONUtil.toJsonStr(count));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<FieldMapping> getFieldInfo(Class clazz) {
        return getFieldInfo(clazz, null);
    }

    private List<FieldMapping> getFieldInfo(Class clazz, String fieldName) {
        Field[] fields = clazz.getDeclaredFields();
        List<FieldMapping> fieldMappingList = new ArrayList<>();
        for (Field field : fields) {
            FieldInfo fieldInfo = field.getAnnotation(FieldInfo.class);
            if (null == fieldInfo) continue;
            String type = fieldInfo.type();
            int participle = fieldInfo.participle();
            int ignoreAbove = fieldInfo.ignoreAbove();
            String name = field.getName();
            if ("object".equals(type)) {
                Class fc = field.getType();
                if (fc.isPrimitive()) {
                    if (StrUtil.isNotBlank(fieldName)) {
                        name = name + "." + fieldName;
                    }
                    fieldMappingList.add(new FieldMapping(name, type, participle, ignoreAbove));
                } else {
                    if (fc.isAssignableFrom(List.class)) {
                        Type gt = field.getGenericType();
                        ParameterizedType pt = (ParameterizedType) gt;
                        Class lll = (Class) pt.getActualTypeArguments()[0];
                        fieldMappingList.addAll(getFieldInfo(lll, name));
                    } else {
                        fieldMappingList.addAll(getFieldInfo(fc, name));
                    }
                }
            } else {
                if (StrUtil.isNotBlank(fieldName)) {
                    name = fieldName + "." + name;
                }
                fieldMappingList.add(new FieldMapping(name, type, participle, ignoreAbove));
            }
        }
        return fieldMappingList;
    }

    private boolean createIndexAndCreateMapping(String indexName, List<FieldMapping> fieldMappingList, int number_of_shards, int number_of_replicas) {
        try {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
            Settings.Builder put = Settings.builder()
                    .put("index.number_of_shards", number_of_shards)
                    .put("index.number_of_replicas", number_of_replicas)
                    .put("analysis.analyzer.ik_smart_pinyin.type", "custom")
                    .put("analysis.analyzer.ik_smart_pinyin.tokenizer", "ik_smart")
                    .putList("analysis.analyzer.ik_smart_pinyin.filter", "my_pinyin", "word_delimiter")
                    .put("analysis.filter.my_pinyin.type", "pinyin")
                    .put("analysis.filter.my_pinyin.keep_first_letter", true)
                    .put("analysis.filter.my_pinyin.keep_separate_first_letter", false)
                    .put("analysis.filter.my_pinyin.keep_full_pinyin", true)
                    .put("analysis.filter.my_pinyin.keep_original", true)
                    .put("analysis.filter.my_pinyin.limit_first_letter_length", 16)
                    .put("analysis.filter.my_pinyin.lowercase", true)
                    .put("analysis.filter.my_pinyin.remove_duplicated_term", true);
            createIndexRequest.settings(put);

            XContentBuilder mapping = JsonXContent.contentBuilder();
            mapping.startObject().startObject("properties");
            for (FieldMapping info : fieldMappingList) {
                String field = info.getField();
                String dateType = info.getType();
                if (null == dateType || "".equals(dateType.trim())) {
                    dateType = "string";
                }
                dateType = dateType.toLowerCase();
                int participle = info.getParticiple();
                if ("string".equals(dateType)) {
                    if (0 == participle) {
                        mapping.startObject(field)
                                .field("type", "keyword")
                                .field("index", false)
                                .field("ignore_above", info.getIgnoreAbove())
                                .endObject();
                    } else if (1 == participle) {
                        mapping.startObject(field)
                                .field("type", "text")
                                .field("analyzer", "ik_max_word")
                                .endObject();
                    } else if (2 == participle) {
                        mapping.startObject(field)
                                .field("type", "text")
                                .field("analyzer", "ik_smart_pinyin")
                                .endObject();
                    }
                } else if ("datetime".equals(dateType)) {
                    mapping.startObject(field)
                            .field("type", "date")
                            .field("format", "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
                            .endObject();
                } else if ("timestamp".equals(dateType)) {
                    mapping.startObject(field)
                            .field("type", "date")
                            .field("format", "strict_date_optional_time||epoch_millis")
                            .endObject();
                } else if ("float".equals(dateType) || "double".equals(dateType)) {
                    mapping.startObject(field)
                            .field("type", "scaled_float")
                            .field("scaling_factor", 100)
                            .endObject();
                } else {
                    mapping.startObject(field)
                            .field("type", dateType)
                            .endObject();
                }
            }
            mapping.endObject().endObject();
            createIndexRequest.mapping(mapping);
            CreateIndexResponse createIndexResponse = restClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            CcrClient ccr = restClient.ccr();
            return createIndexResponse.isAcknowledged();
        } catch (IOException e) {
            log.error("\n根据信息自动创建索引与mapping创建失败，失败信息为:{}", e.getMessage());
            return false;
        }
    }

    private Map<String, String> ObjToMap(Object obj) {
        Map<String, String> hashMap = new HashMap<>();
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object o = field.get(obj);
                if (null != o && !"".equals(o)) {
                    hashMap.put(field.getName(), o.toString());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return hashMap;
    }
}
