package aenadon.viruscomplete;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

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

    @POST("file/scan")
    @Multipart
    Call<VirusTotalResponse> sendFileForScan(@Part("request") MultipartBody file);

}