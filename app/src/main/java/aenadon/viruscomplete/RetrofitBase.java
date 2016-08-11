package aenadon.viruscomplete;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class RetrofitBase {

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://www.virustotal.com/vtapi/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static Retrofit getRetrofit() {
        return retrofit;
    }

}
