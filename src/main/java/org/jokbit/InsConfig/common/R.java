package org.jokbit.InsConfig.common;

public class R {

    public static Integer OK = 1;

    public static Integer WARN = 2;

    public static Integer ERROR = 3;

    private Integer code;

    private String message;

    public R(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public R() {
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final R r = new R();

        public R build() {
            return r;
        }

        public Builder code(int code) {
            r.code = code;
            return this;
        }

        public Builder message(String message) {
            r.message = message;
            return this;
        }
    }
}
