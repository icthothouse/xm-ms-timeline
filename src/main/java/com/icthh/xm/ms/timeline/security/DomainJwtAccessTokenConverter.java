package com.icthh.xm.ms.timeline.security;

import static com.icthh.xm.ms.timeline.config.Constants.AUTH_TENANT_KEY;
import static com.icthh.xm.ms.timeline.config.Constants.AUTH_USER_KEY;
import static com.icthh.xm.ms.timeline.config.Constants.AUTH_XM_COOKIE_KEY;
import static com.icthh.xm.ms.timeline.config.Constants.AUTH_XM_LOCALE;
import static com.icthh.xm.ms.timeline.config.Constants.AUTH_XM_TOKEN_KEY;
import static com.icthh.xm.ms.timeline.config.Constants.AUTH_XM_USERID_KEY;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

/**
 * Overrides to get token tenant.
 */
public class DomainJwtAccessTokenConverter extends JwtAccessTokenConverter {

    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
        final OAuth2Authentication authentication = super.extractAuthentication(map);
        final Map<String, String> details = new HashMap<>();
        details.put(AUTH_TENANT_KEY, (String) map.get(AUTH_TENANT_KEY));
        details.put(AUTH_XM_TOKEN_KEY, (String) map.get(AUTH_XM_TOKEN_KEY));
        details.put(AUTH_XM_COOKIE_KEY, (String) map.get(AUTH_XM_COOKIE_KEY));
        details.put(AUTH_XM_USERID_KEY, (String) map.get(AUTH_XM_USERID_KEY));
        details.put(AUTH_XM_LOCALE, (String) map.get(AUTH_XM_LOCALE));
        details.put(AUTH_USER_KEY, (String) map.get(AUTH_USER_KEY));
        authentication.setDetails(details);

        return authentication;
    }
}
