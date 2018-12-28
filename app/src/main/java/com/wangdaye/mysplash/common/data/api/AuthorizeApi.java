package com.wangdaye.mysplash.common.data.api;

import com.wangdaye.mysplash.common.data.entity.unsplash.AccessToken;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Authorize api.
 * */

public interface AuthorizeApi {

    @POST("oauth/token")
    Call<AccessToken> getAccessToken(@Query("client_id") String client_id,
                                     @Query("client_secret") String client_secret,
                                     @Query("redirect_uri") String redirect_uri,
                                     @Query("code") String code,
                                     @Query("grant_type") String grant_type);
}
