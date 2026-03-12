package xyz.lzw.nestful.ref.model;

/**
 * 统一响应结构:
 * code: 0 表示成功，非 0 表示业务错误
 * message: 提示信息
 * data: 业务数据
 */
public class Resp<T> {

    private int code;
    private String message;
    private T data;

    public Resp() {
    }

    public Resp(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Resp<T> ok(T data) {
        return new Resp<>(0, "ok", data);
    }

    public static <T> Resp<T> ok() {
        return new Resp<>(0, "ok", null);
    }

    public static <T> Resp<T> error(int code, String message) {
        return new Resp<>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public Resp<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Resp<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getData() {
        return data;
    }

    public Resp<T> setData(T data) {
        this.data = data;
        return this;
    }
}
