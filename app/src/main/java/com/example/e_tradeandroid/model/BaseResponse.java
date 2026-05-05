package com.example.e_tradeandroid.model;

public class BaseResponse<T> {
    private int code;
    private String msg;
    private T data;

    // 判断是否成功
    public boolean isSuccess() {
        return code == 200;
    }

    public String getMsg() {
        return msg;
    }

    public String getMessage() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}