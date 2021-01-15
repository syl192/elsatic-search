package com.elasticsearch.elasticsearch.config;

import cn.hutool.core.util.StrUtil;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

@Configuration
public class ElasticsearchConfiguration {
    @Resource
    private Environment env;

    @Bean(destroyMethod = "close", name = "restClient")
    public RestHighLevelClient initRestClient() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(getHost(), getPort()))
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout(getConnTimeout())
                        .setSocketTimeout(getSocketTimeout())
                        .setConnectionRequestTimeout(getConnRequestTimeout()));
        return new RestHighLevelClient(builder);
    }

    public String getHost() {
        String property = env.getProperty("elasticsearch.host");
        return property;
    }

    public Integer getPort() {
        String port = env.getProperty("elasticsearch.port");
        if (StrUtil.isNotBlank(port)) {
            return Integer.valueOf(port);
        }
        return null;
    }

    public Integer getConnTimeout() {
        String connTimeout = env.getProperty("elasticsearch.connTimeout");
        if (StrUtil.isNotBlank(connTimeout)) {
            return Integer.valueOf(connTimeout);
        }
        return null;
    }

    public Integer getSocketTimeout() {
        String socketTimeout = env.getProperty("elasticsearch.socketTimeout");
        if (StrUtil.isNotBlank(socketTimeout)) {
            return Integer.valueOf(socketTimeout);
        }
        return null;
    }

    public Integer getConnRequestTimeout() {
        String connectionRequestTimeout = env.getProperty("elasticsearch.connectionRequestTimeout");
        if (StrUtil.isNotBlank(connectionRequestTimeout)) {
            return Integer.valueOf(connectionRequestTimeout);
        }
        return null;
    }
}
