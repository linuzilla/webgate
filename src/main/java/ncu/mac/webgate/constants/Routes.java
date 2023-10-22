package ncu.mac.webgate.constants;

public class Routes {
    private static final String REDIRECT = "redirect:";

    public static final String ROOT = "/";

    public static final String LOGIN = "/login";
    public static final String LOGOUT = "/logout";
    public static final String LOGOUT_AND_FORGET = "/logout-and-forget";

    public static final String FAVICONS = "/favicon.ico";
    public static final String WELL_KNOWN = "/.well-known";

    public static final String ABOUT = "/about";

    public static final String SIGN_IN = "/sign-in";
    public static final String SIGN_IN_SET = "/set";
    public static final String SIGN_IN_DONE = "/done";
    public static final String SIGN_IN_AGREE = "/agree";

    public static final String ERROR = "/error";

    public static final String TESTING = "/testing";

    public static final String PORTAL = "/portal";
    public static final String PORTAL_ATTEND = "/attend";
    public static final String PORTAL_HR = "/hr";
    public static final String PORTAL_EFORM = "/eform";
    public static final String PORTAL_SUCCESS = "/success";
    public static final String RESOURCES = "/resources";
    public static final String WEBJAR = "/webjar";
    public static final String SHOW = "/show";
    public static final String SHOW_STREAM = "/stream";
    public static final String STREAM = "/stream";
    public static final String STREAM_SSE = "/server-side-events";
    public static final String SOCKET = "/socket";
    public static final String HOME = "/home";
    public static final String HOME_HR = "/hr";
    public static final String HOME_EFORM = "/eform";
    public static final String HOME_LOCATION = "/location";
    public static final String HOME_SIGN_STATUS = "/sign-status";
    public static final String HOME_FAKE_SIGNIN = "/fake-signin";
    public static final String HOME_SHIFT_APPLY = "/shift-apply";
    public static final String HOME_SCAN_SHIFT_WORKING = "/scan-shift-working";
    public static final String HOME_VACATION_DATA = "/vacation-data";
    public static final String HOME_VACATION_RECORDS = "/vacation-records";
    public static final String HOME_VACATION_APPLY = "/submit-vacation-apply";
    public static final String HOME_UNFINISHED_VACATION_REQUEST = "/unfinished-vacation-request";
//    public static final String HOME_VACATION_INFO = "/vacation-info";
    public static final String HOME_ATTEND_SIGN = "/attend-sign";
    public static final String HOME_ATTEND_RECORDS = "/attend-records";
    public static final String HOME_UNFINISHED_SHIFT_REQUEST = "/unfinished-shift-request";
    public static final String HOME_TODO = "/todo";
//    public static final String HOME_PORTAL = "/portal";
    public static final String HOME_APIS = "/apis";
    public static final String HOME_APIS_SHIFT_APPLY = "/shift-apply";
    public static final String HOME_APIS_MEMBERS = "/members";
    public static final String HOME_APIS_CHECK_LOCATION = "/check-location";
    //    public static final String HOME_SHOW_COOKIE = "/show-cookies";
    public static final String HUMAN_SYS = "/HumanSys";
    public static final String HUMAN_SYS_DOT_TWEAK = "/tweak";
    public static final String HUMAN_SYS_DOT_TWEAK_ADD_COOKIE = "/add-cookie";
    public static final String EFORM_SYS = "/Eform";
    public static final String EFORM_SYS_DOT_TWEAK = "/tweak";
    public static final String EFORM_SYS_DOT_TWEAK_ADD_COOKIE = "/add-cookie";
    public static final String ATTEND_APPLY = "/HumanSys/onlineAttend/attendApply";
    public static final String ATTEND_SIGN = "/HumanSys/onlineAttend/attendSign";

    public static String redirect(String... routes) {
        if (routes.length == 0) {
            return REDIRECT + "/";
        } else if (routes.length == 1) {
            return REDIRECT + routes[0];
        } else {
            return REDIRECT + String.join("", routes);
        }
    }
}
