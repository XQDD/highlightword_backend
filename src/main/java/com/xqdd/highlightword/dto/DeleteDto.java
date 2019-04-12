package com.xqdd.highlightword.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class DeleteDto extends BaseDto {
    @NotBlank
    private String word;
}
