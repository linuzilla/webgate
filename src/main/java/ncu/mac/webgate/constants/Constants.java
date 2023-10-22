package ncu.mac.webgate.constants;

import java.text.Collator;
import java.util.List;
import java.util.Locale;

public class Constants {
    public static final int SESSION_DATA_TTL = 90 * 86400;
    public static final int MAX_REDIRECT_DEPTH = 12;

    public static final String PERSISTENT_COOKIE_NAME = "latte";
    public static final long PERSISTENT_COOKIE_MAX_AGE = 90 * 86400;
    public static final boolean PERSISTENT_COOKIE_SECURE = false;
    public static final String PERSISTENT_COOKIE_SAME_SITE = "Lax"; // ""Strict", "Lax", "None"

    public static final List<String> CORE_API_AUTH_USER_TYPE = List.of("eform");

    public static final Collator CHINESE_COLLATOR = Collator.getInstance(Locale.TRADITIONAL_CHINESE);

    public static final String PORTAL_HOST = "portal.ncu.edu.tw";
    public static final String PORTAL_BASE_URI = "https://" + PORTAL_HOST;
    public static final String PORTAL_LOGIN_PATH = "/login";
    public static final String PORTAL_EXPIRED_PATH = "/expired";
    public static final String PORTAL_LOGOUT_PATH = "/logout";
    public static final String PORTAL_LEAVING_PATH = "/leaving";

    public static final String HUMAN_RESOURCE_URI = "/HumanSys";
    public static final String HUMAN_RESOURCE_LOGIN_PATH = "/login";

    public static final String EFORM_URI = "/Eform";
    public static final String EFORM_URI_LOGIN_PATH = "/signin";

    public static final String LOCAL_COOKIE_NAME = "SESSION";

    public static final String X_REQUESTED_WITH = "X-Requested-With";
    public static final String XML_HTTP_REQUEST = "XMLHttpRequest";

    public static final long WEBCLIENT_REQUEST_TIMEOUT = 30L;
    public static final int WEBCLIENT_CONNECT_TIMEOUT_MILLS = 30_000;

    public static final String NCU_COMPUTER_CENTER_UNIT_ID = "A800";

    public static final String WEBSOCKET_JSON_DATA_LEADING = "json::";

    public static final String G_RECAPTCHA_RESPONSE = "g-recaptcha-response";
    public static final long REMEMBER_ME_TTL = 7 * 86400L;

//    public static final String HUMAN_RESOURCE_SYSTEM_PORTAL_URI = PORTAL_BASE_URI + "/system/142";
}
