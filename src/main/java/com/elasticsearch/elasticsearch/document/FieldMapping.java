package com.elasticsearch.elasticsearch.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldMapping {

    private String field;

    private String type;

    private int participle;

    private int ignoreAbove;
}
