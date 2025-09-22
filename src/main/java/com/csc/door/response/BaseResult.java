package com.csc.door.response;

import com.csc.door.enums.ResultStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Objects;

public class BaseResult <T>{

    //70 : 成功；90 : 失败
    private String resultCls;
    //文字描述
    private String resultDetail;

    private T data;

    private List<T> datas;

    public BaseResult() {
    }

    public BaseResult(String resultCls, String resultDetail) {
        this.resultCls = resultCls;
        this.resultDetail = resultDetail;
    }

    public String getResultCls() {
        return resultCls;
    }

    public void setResultCls(String resultCls) {
        this.resultCls = resultCls;
    }

    public String getResultDetail() {
        return resultDetail;
    }

    public void setResultDetail(String resultDetail) {
        this.resultDetail = resultDetail;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<T> getDatas() {
        return datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
    }

    @JsonIgnore
    public boolean isSuccess(){
        return Objects.equals(resultCls, ResultStatusEnum.SUCCESS.code);
    }

    @JsonIgnore
    public boolean isFailure(){
        return Objects.equals(resultCls, ResultStatusEnum.FAILURE.code);
    }

    public static BaseResult success(){
        BaseResult baseResult = new BaseResult(ResultStatusEnum.SUCCESS.code, null);
        return baseResult;
    }
    public static <T> BaseResult success(T data){
        BaseResult baseResult = new BaseResult(ResultStatusEnum.SUCCESS.code, null);
        return baseResult;
    }
    public static <T>  BaseResult success(List<T> datas){
        BaseResult baseResult = new BaseResult(ResultStatusEnum.SUCCESS.code, null);
        return baseResult;
    }

    public static BaseResult failure(String msg){
        return new BaseResult(ResultStatusEnum.FAILURE.code,msg);
    }

}
