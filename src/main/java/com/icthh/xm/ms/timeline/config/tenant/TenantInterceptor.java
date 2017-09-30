package com.icthh.xm.ms.timeline.config.tenant;

import static com.icthh.xm.ms.timeline.config.Constants.AUTH_TENANT_KEY;
import static com.icthh.xm.ms.timeline.config.Constants.AUTH_USER_KEY;
import static com.icthh.xm.ms.timeline.config.Constants.HEADER_TENANT;

import com.icthh.xm.commons.config.client.repository.TenantListRepository;
import com.icthh.xm.ms.timeline.config.ApplicationProperties;
import io.undertow.util.LocaleUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;



/**
 * Intercept JWT tenant and save it in context.
 */
@Slf4j
@Component
public class TenantInterceptor extends HandlerInterceptorAdapter {

    private final AntPathMatcher matcher = new AntPathMatcher();

    private final ApplicationProperties applicationProperties;

    private final TenantListRepository tenantListRepository;

    public TenantInterceptor(ApplicationProperties applicationProperties,
                             TenantListRepository tenantListRepository) {
        this.applicationProperties = applicationProperties;
        this.tenantListRepository = tenantListRepository;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        if (isIgnoredRequest(request)) {
            return true;
        }

        try {
            String tenant;
            final OAuth2Authentication auth = getAuthentication();
            if (auth == null) {
                tenant = request.getHeader(HEADER_TENANT);
                TenantContext.setCurrent(tenant);
            } else {
                Map<String, String> details = null;

                if (auth.getDetails() != null) {
                    details = Map.class.cast(OAuth2AuthenticationDetails.class.cast(auth.getDetails())
                                                 .getDecodedDetails());
                }

                if (details == null) {
                    details = new HashMap<>();
                }

                tenant = details.getOrDefault(AUTH_TENANT_KEY, "");

                String xmUserLogin = (String) auth.getPrincipal();
                String xmUserKey = details.getOrDefault(AUTH_USER_KEY, "");

                TenantContext.setCurrent(new TenantInfo(tenant, xmUserLogin, xmUserKey));

            }

            final boolean tenantSet;
            if (StringUtils.isBlank(tenant)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"error\": \"No tenant supplied\"}");
                response.getWriter().flush();
                tenantSet = false;
            } else if (tenantListRepository.getSuspendedTenants().contains(tenant.toLowerCase())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"error\": \"Tenant is suspended\"}");
                response.getWriter().flush();
                tenantSet = false;
            } else {
                tenantSet = true;
            }

            return tenantSet;
        } catch (Exception e) {
            log.error("Error in tenant interceptor: ", e);
            throw e;
        }
    }

    @Override
    public void postHandle(
        HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
        throws Exception {

        // clear tenant context
        TenantContext.clear();
    }

    private static OAuth2Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof OAuth2Authentication) {
            return (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        }
        return null;
    }

    private boolean isIgnoredRequest(HttpServletRequest request) {
        String path = request.getServletPath();
        List<String> ignoredPatterns = applicationProperties.getTenantIgnoredPathList();
        if (ignoredPatterns != null && path != null) {
            for (String pattern : ignoredPatterns) {
                if (matcher.match(pattern, path)) {
                    return true;
                }
            }
        }
        return false;
    }

}
