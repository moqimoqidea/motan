package com.weibo.api.motan.admin;

import com.weibo.api.motan.common.MotanConstants;
import com.weibo.api.motan.common.URLParamType;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.util.MotanGlobalConfigUtil;
import com.weibo.api.motan.util.MotanSwitcherUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zhanglei28
 * @date 2023/11/3.
 */
public class DefaultPermissionChecker implements PermissionChecker {
    public static final String ADMIN_DISABLE_SWITCHER = "feature.motan.admin.disable";

    protected String token;

    public DefaultPermissionChecker() {
        init();
    }

    @Override
    public boolean check(Request request) {
        // check ip
        String ip = request.getAttachments().get(URLParamType.host.getName());
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return true;
        }
        if (MotanSwitcherUtil.isOpen(ADMIN_DISABLE_SWITCHER)) { // disable token validation and extended validation
            return false;
        }
        // check token
        if (token != null && token.equals(getToken(request))) {
            return true;
        }
        // for custom extension
        return extendValidate(request);
    }

    protected boolean extendValidate(Request request) {
        // Override this method for extended validation
        return false;
    }

    protected String getToken(Request request){
        String requestToken = request.getAttachments().get("Token");
        if (requestToken == null){
            requestToken = request.getAttachments().get("token");
        }
        return requestToken;
    }

    private void init() {
        // set token
        String token = System.getenv(MotanConstants.ENV_MOTAN_ADMIN_TOKEN);
        if (StringUtils.isBlank(token)) {
            token = MotanGlobalConfigUtil.getAdminToken();
        }
        if (token != null) {
            this.token = token.trim();
        }
        MotanSwitcherUtil.switcherIsOpenWithDefault(ADMIN_DISABLE_SWITCHER, false);
    }
}
