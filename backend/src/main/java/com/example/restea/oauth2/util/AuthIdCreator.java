package com.example.restea.oauth2.util;

import com.example.restea.oauth2.dto.OAuth2Response;

public class AuthIdCreator {
    // 전달받은 데이터에서 username으로 지칭할 수 있는 것이 없기에 별도의 메소드를 구현한다.
    public static String getAuthId(OAuth2Response oAuth2Response) {
        return oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
    }
}
