package com.elasticsearch.elasticsearch.annotation;

import java.lang.annotation.*;


@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldInfo {

    /**
     * @string text ,keyword
     * @Numeric long, integer, short, byte, double, float, half_float, scaled_float
     * @date date(分 datetime - > yyyy - MM - dd HH : mm : ss, timestamp - > epoch_millis 两种情况处理)
     * @Object object
     */
    String type() default "string";

    /**
     * 分词器选择  0. not_analyzed   1. ik_max_word    2. ik_smart_pinyin
     */
    int participle() default 0;

    /**
     * 当字段文本的长度大于指定值时，不做倒排索引
     */
    int ignoreAbove() default 256;
}
