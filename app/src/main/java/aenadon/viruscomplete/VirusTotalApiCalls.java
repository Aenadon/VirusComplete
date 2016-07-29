package aenadon.viruscomplete;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface VirusTotalApiCalls {

    @POST("url/report")
    @FormUrlEncoded
    Call<VirusTotalResponse> getURLScanResults(@Field("apikey") String apikey, @Field("resource") String resource);

    @POST("url/scan")
    @FormUrlEncoded
    Call<VirusTotalResponse> forceURLScan(@Field("apikey") String apikey, @Field("url") String url);

    @POST("file/report")
    @FormUrlEncoded
    Call<VirusTotalResponse> getFileReportForHash(@Field("apikey") String apikey, @Field("resource") String hash);

}