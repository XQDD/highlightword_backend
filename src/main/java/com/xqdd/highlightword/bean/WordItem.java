package com.xqdd.highlightword.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WordItem {
    private String word;
    private String phonetic;
    private String trans;
    private String tags;
}
