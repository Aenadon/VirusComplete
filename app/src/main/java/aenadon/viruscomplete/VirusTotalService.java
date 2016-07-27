package aenadon.viruscomplete;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface VirusTotalService {

    @POST("url/report")
    @FormUrlEncoded
    Call<VirusTotalURLResponse> getURLScanResults (@Field("apikey") String apikey, @Field("resource") String resource, @Field("scan") int scan);

    @POST("url/scan")
    @FormUrlEncoded
    Call<VirusTotalURLResponse> forceURLScan (@Field("apikey") String apikey, @Field("url") String url);

}
