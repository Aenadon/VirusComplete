package aenadon.viruscomplete;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

interface VirusTotalApiCalls {

    @POST("url/report")
    @FormUrlEncoded
    Call<VirusTotalResponse> getURLScanResults(@Field("apikey") String apikey, @Field("resource") String resource);

    @POST("url/scan")
    @FormUrlEncoded
    Call<VirusTotalResponse> forceURLRescan(@Field("apikey") String apikey, @Field("url") String url);

    @POST("file/report")
    @FormUrlEncoded
    Call<VirusTotalResponse> getFileReportForHash(@Field("apikey") String apikey, @Field("resource") String hash);

    @POST("file/rescan")
    @FormUrlEncoded
    Call<VirusTotalResponse> forceHashRescan(@Field("apikey") String apikey, @Field("resource") String resource);

    /*
Not needed:
    @POST("file/scan")
    @Multipart
    Call<VirusTotalResponse> sendFileForScan(@Part("request") MultipartBody file);
    */

}