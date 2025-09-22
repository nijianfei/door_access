package com.csc.door.handler;

import com.csc.door.exception.BusinessException;
import com.csc.door.response.BaseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    // 处理业务异常
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResult> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return new ResponseEntity<>(BaseResult.failure(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    // 处理参数校验异常（@Valid失败）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResult> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(e->e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return new ResponseEntity<>(BaseResult.failure(errorMsg), HttpStatus.BAD_REQUEST);
    }

    // 兜底处理所有未捕获异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResult> handleGlobalException(Exception ex) {
        log.error("系统异常: ", ex);  // 记录详细堆栈
        return new ResponseEntity<>(BaseResult.failure(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}