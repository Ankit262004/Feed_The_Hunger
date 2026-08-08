package com.example.feed_the_hunger.Volenteer;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiServices {

    @Multipart
    @POST("food/add")
    Call<ResponseBody> uploadFood(

            @Part("userId") RequestBody userId,
            @Part("foodName") RequestBody foodName,
            @Part("quantity") RequestBody quantity,
            @Part("expiryDate") RequestBody expiryDate,
            @Part("description") RequestBody description,
            @Part("location") RequestBody location,
            @Part("foodType") RequestBody foodType,

            // map location
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,

            @Part MultipartBody.Part image
    );

    @DELETE("food/delete/{id}")
    Call<ResponseBody> deleteFood(@Path("id") String foodId);
}