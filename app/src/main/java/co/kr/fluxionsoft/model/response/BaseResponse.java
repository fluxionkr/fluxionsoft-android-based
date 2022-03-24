package co.kr.fluxionsoft.model.response;

import com.google.gson.annotations.SerializedName;


public class BaseResponse {

    @SerializedName("code")
    public String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }



}
