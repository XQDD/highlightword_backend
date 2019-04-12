package com.xqdd.highlightword.controller;

import com.xqdd.highlightword.dto.BaseDto;
import com.xqdd.highlightword.dto.DeleteDto;
import com.xqdd.highlightword.exception.Result;
import com.xqdd.highlightword.service.WordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;

@RestController
@RequestMapping(value = "word", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@RequiredArgsConstructor
@Validated
@Slf4j
public class WordController {

    private final WordService wordService;


    @PostMapping("delete")
    public Result delete(@RequestBody @Valid DeleteDto deleteDto) throws IOException, URISyntaxException {
        wordService.deleteWord(deleteDto.getWord(), deleteDto.getCookie());
        log.info("删除单词：" + deleteDto.getWord());
        return Result.success();
    }


    @PostMapping("getWords")
    public Result getWords(@RequestBody @Valid BaseDto baseDto) throws IOException, URISyntaxException {
        var list = wordService.getWordItems(baseDto.getCookie());
        log.info("获取单词：" + list);
        return Result.success(list);
    }

}
