package com.xqdd.highlightword.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeStacktrace;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestControllerAdvice
@RestController
@Slf4j
public class MyExceptionHandlers extends AbstractErrorController {


    private final ErrorAttributes errorAttributes;

    private final ServerProperties serverProperties;

    public MyExceptionHandlers(ErrorAttributes errorAttributes, ServerProperties serverProperties) {
        super(errorAttributes);
        this.errorAttributes = errorAttributes;
        this.serverProperties = serverProperties;
    }


    /**
     * 方法校验错误
     *
     * @param e 错误
     * @return 错误结果
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Object handleApiConstraintViolationException(ConstraintViolationException e) {
        Map<String, String> errors = new HashMap<>();
        e.getConstraintViolations().forEach(c -> errors.put(StreamSupport.stream(c.getPropertyPath().spliterator(), false).skip(1).map(Path.Node::getName).collect(Collectors.joining(".")), c.getMessage()));
        return Result.error(40000, errors);
    }

    //普通参数校验错误,json参数校验错误
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    public Object validExceptionHandler(Exception e) {
        BindingResult result;
        if (e instanceof BindException) {
            result = ((BindException) e).getBindingResult();
        } else {
            result = ((MethodArgumentNotValidException) e).getBindingResult();
        }
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach((fe) -> errors.put(fe.getField(), fe.getDefaultMessage()));
        return Result.error(40000, errors);
    }


    //没session
    @ExceptionHandler({ServletRequestBindingException.class})
    public Object sessionMiss(ServletRequestBindingException e) {
        return Result.error(40100, "请先登录：" + e.getMessage());
    }

    //业务错误
    @ExceptionHandler({BusinessException.class})
    public Object handleBusinessException(BusinessException e) {
        if (e.getCause() != null || e.getMessage() != null) {
            log.error(e.getMessage(), e.getCause());
            if (e.getResponse() == null)
                return Result.error(50001, "服务器发生未知错误");
        }
        return e.getResponse();
    }


    //参数转化错误
    @ExceptionHandler({
            NumberFormatException.class,
            IllegalArgumentException.class,

    })
    public Object numberError(Throwable t) {
        log.warn("客户端错误：", t);
        return Result.error(40000, getErrors(t));
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class
    })
    public Object formatError(HttpMessageNotReadableException e) {
        log.warn("客户端错误：", e);
        return Result.error(40000, "参数格式有误");
    }


    //参数转化null错误
    @ExceptionHandler({IllegalStateException.class})
    public Object numberError(IllegalStateException e) {
        if (e.getMessage().contains("is present but cannot be translated into a null")) {
            return Result.error(40000, Map.of(e.getMessage().split("'")[1], "不能为空"));
        }
        return Result.error(40000, getErrors(e));
    }

    @RequestMapping(value = "error", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object error(HttpServletRequest request) {
        var status = getStatus(request);
        if (status.is5xxServerError()) {
            var error = errorAttributes.getError(new ServletWebRequest(request) {
            });
            return Result.error(50000, "服务器发生未知错误：" + getErrors(error));
        } else {
            return Result.error(status.value() * 100, getErrorAttributes(request, isIncludeStackTrace(request)));
        }
    }


    @Override
    public String getErrorPath() {
        return "error";
    }

    private String getErrors(Throwable throwable) {
        List<String> errors = new ArrayList<>();
        while (throwable != null) {
            errors.add(throwable.getMessage());
            throwable = throwable.getCause();
        }
        return String.join("--------------->", errors);
    }

    /**
     * Determine if the stacktrace attribute should be included.
     *
     * @param request the source request
     * @return if the stacktrace attribute should be included
     */
    private boolean isIncludeStackTrace(HttpServletRequest request) {
        IncludeStacktrace include = serverProperties.getError().getIncludeStacktrace();
        if (include == IncludeStacktrace.ALWAYS) {
            return true;
        }
        if (include == IncludeStacktrace.ON_TRACE_PARAM) {
            return getTraceParameter(request);
        }
        return false;
    }

}


