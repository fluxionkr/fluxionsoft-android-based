package co.kr.fluxionsoft;


import co.kr.fluxionsoft.model.response.BaseResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface ApiService {

    @FormUrlEncoded
    @POST("/theclozet/app/insertUpdatePushToken")
    Call<BaseResponse> insertUpdatePushToken(@Field("mId") String mid, @Field("token") String token, @Field("deviceType") String deviceType,
                                    @Field("useYn") String useYn);

    //@FormUrlEncoded
    @GET("/theclozet/front/member/selectMnoFromMid")
    Call<String> selectMnoFromMid(@Query("mid") String mid);

}
