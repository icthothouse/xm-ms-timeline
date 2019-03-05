package com.icthh.xm.ms.timeline.web.rest;

import com.icthh.xm.ms.timeline.config.DefaultProfileUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Resource to return information about the currently running Spring profiles.
 */
@RestController
@RequestMapping("/api")
public class ProfileInfoResource {

    private final Environment env;

    public ProfileInfoResource(Environment env) {
        this.env = env;
    }

    @GetMapping("/profile-info")
    @PostAuthorize("hasPermission({'returnObject': returnObject}, 'BALANCE.PROFILE.GET_LIST.ITEM')")
    public ProfileInfoVM getActiveProfiles() {
        String[] activeProfiles = DefaultProfileUtil.getActiveProfiles(env);

        return new ProfileInfoVM(activeProfiles, getRibbonEnv(activeProfiles));
    }

    private String getRibbonEnv(String[] activeProfiles) {
        String[] displayOnActiveProfiles = {};
        if (displayOnActiveProfiles == null) {
            return null;
        }
        List<String> ribbonProfiles = new ArrayList<>(Arrays.asList(displayOnActiveProfiles));
        List<String> springBootProfiles = Arrays.asList(activeProfiles);
        ribbonProfiles.retainAll(springBootProfiles);
        if (!ribbonProfiles.isEmpty()) {
            return ribbonProfiles.get(0);
        }
        return null;
    }

    class ProfileInfoVM {

        private String[] activeProfiles;

        private String ribbonEnv;

        ProfileInfoVM(String[] activeProfiles, String ribbonEnv) {
            this.activeProfiles = activeProfiles;
            this.ribbonEnv = ribbonEnv;
        }

        public String[] getActiveProfiles() {
            return activeProfiles;
        }
    }
}
