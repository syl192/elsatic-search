package com.elasticsearch.elasticsearch.document;


import com.elasticsearch.elasticsearch.annotation.FieldInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author syl
 * @since 2020-12-31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageTo implements Serializable {

    private static final long serialVersionUID = 1L;

    @FieldInfo(type = "Long")
    private Long id;

    @FieldInfo(type = "Long")
    private Long messageId;

    @FieldInfo(type = "Long")
    private Long toId;

    @FieldInfo(type = "Long")
    private Integer toType;

    @FieldInfo(type = "datetime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date sendTime;

    @FieldInfo(type = "Integer")
    private Integer isRead;

    @FieldInfo(type = "Integer")
    private Integer isDel;

    @FieldInfo(participle = 2)
    private String condition;
}
