package org.csu.api.utils;

import lombok.extern.slf4j.Slf4j;
import org.csu.api.common.CommonResponse;
import org.csu.api.common.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResponse handleValidException(MethodArgumentNotValidException e) {
        logger.error(e.getMessage());
        return CommonResponse.createForError(ResponseCode.ERROR.getCode(), formatValidErrorsMessage(e.getAllErrors()));
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public CommonResponse handleValidException(Exception e) {
        logger.error(e.getMessage());
        return CommonResponse.createForError("服务器异常");
    }

    private String formatValidErrorsMessage(List<ObjectError> errors){
        StringBuffer errorMessage = new StringBuffer();
        errors.forEach(error -> errorMessage.append(error.getDefaultMessage()).append(","));
        errorMessage.deleteCharAt(errorMessage.length()-1);
        return errorMessage.toString();
    }

}
