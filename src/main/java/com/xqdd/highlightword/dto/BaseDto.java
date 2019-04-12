package com.xqdd.highlightword.dto;


import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class BaseDto  {
    @NotBlank
    protected String cookie;
}
