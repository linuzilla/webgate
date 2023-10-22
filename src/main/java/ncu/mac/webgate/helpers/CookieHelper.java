package ncu.mac.webgate.helpers;

import ncu.mac.webgate.constants.Constants;
import ncu.mac.webgate.models.SessionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpCookie;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CookieHelper {
    private static final Logger logger = LoggerFactory.getLogger(CookieHelper.class);
    private static final List<String> PROTECTED_LOCAL_COOKIE = List.of(
            Constants.LOCAL_COOKIE_NAME,
            Constants.PERSISTENT_COOKIE_NAME
    );

    public static String cookiesToString(Collection<SessionData.SerializableCookie> cookies, Predicate<SessionData.SerializableCookie> predicate) {
        return cookies
                .stream()
                .filter(predicate)
                .map(serializableCookie -> MessageFormat.format("{0}={1}", serializableCookie.getName(), serializableCookie.getValue()))
                .collect(Collectors.joining("; "));
    }

    public static Stream<Pair<String, String>> decomposition(String cookieString) {
        return Arrays.stream(cookieString.split("; "))
                .map(s -> s.split("="))
                .filter(strings -> strings.length == 2)
                .map(strings -> Pair.of(strings[0], URLDecoder.decode(strings[1], StandardCharsets.UTF_8)));
    }

    public static String filterCookie(String cookieString, Predicate<String> predicate) {
        return decomposition(cookieString)
                .filter(pair -> predicate.test(pair.getFirst()))
                .map(pair -> MessageFormat.format("{0}={1}", pair.getFirst(), URLEncoder.encode(pair.getSecond(), StandardCharsets.UTF_8)))
                .collect(Collectors.joining("; "));
    }

    public static String filterCookie(List<List<String>> cookieStrings, Predicate<String[]> predicate) {
        return ListHelper.listOfListFlattening(cookieStrings)
                .map(s -> List.of(s.split("; ")))
                .flatMap(Collection::stream)
                .map(s -> s.split("="))
                .filter(strings -> strings.length == 2)
                .filter(predicate)
                .map(strings -> strings[0] + "=" + strings[1])
                .collect(Collectors.joining("; "));
    }

    public static boolean cookieNameFilter(String cookieName) {
        return PROTECTED_LOCAL_COOKIE.stream()
                .noneMatch(s -> s.equals(cookieName));
    }

    public static Map<String, String> cookiesToMap(Collection<List<HttpCookie>> cookies) {
        return cookies.stream()
                .flatMap(Collection::stream)
                .filter(httpCookie -> !Constants.LOCAL_COOKIE_NAME.equals(httpCookie.getName()))
                .collect(Collectors.toMap(HttpCookie::getName,
                        httpCookie -> CookieHelper.cookieValueShorter(httpCookie.getValue())));
    }

    public static String cookieValueShorter(String cookieValue) {
        return cookieValue.substring(0, Math.min(cookieValue.length(), 10)) + " ...";
    }

    public static String cookiePathNormalization(String path) {
        return Objects.requireNonNullElse(path, "/");
    }

    public static boolean cookieBelongsTo(ServerRequest serverRequest, String cookiePath) {
        return serverRequest.path().startsWith(cookiePathNormalization(cookiePath));
    }
}
