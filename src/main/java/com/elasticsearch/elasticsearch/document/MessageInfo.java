package com.elasticsearch.elasticsearch.document;


import com.elasticsearch.elasticsearch.annotation.FieldInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageInfo implements Serializable {

    @FieldInfo(type = "Long")
    private Long messageId;

    @FieldInfo(participle = 2)
    private String msg;

    @FieldInfo(type = "datetime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @FieldInfo(type = "Long")
    private Long bizId;

    @FieldInfo(participle = 2)
    private String bizCondition;

    @FieldInfo(type = "Integer")
    private Integer bizType;

    @FieldInfo
    private String bizTypeName;

    @FieldInfo(type = "Long")
    private Long fromPersonId;

    @FieldInfo(participle = 1)
    private String fromPersonName;

    @FieldInfo(type = "Long")
    private Long patientPersonId;

    @FieldInfo(participle = 2)
    private String patientPersonName;

    @FieldInfo(type = "Long")
    private Long allocationId;

    @FieldInfo(type = "Long")
    private Long templateId;

    @FieldInfo(participle = 2)
    private String content;

    @FieldInfo
    private String iosDocRule;

    @FieldInfo
    private String androidDocRule;

    @FieldInfo
    private String appletEnqRule;

    @FieldInfo(type = "object")
    private List<MessageTo> messageToList;
}
