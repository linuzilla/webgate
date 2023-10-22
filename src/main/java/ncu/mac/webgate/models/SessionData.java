package ncu.mac.webgate.models;

import org.springframework.http.ResponseCookie;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SessionData implements Serializable {
    public static class SerializableCookie implements Serializable {
        private String name;
        private String value;
        private String domain;
        private String path;
        private boolean secure;
        private long maxAge;
        private boolean httpOnly;
        private String sameSite;

        public SerializableCookie(ResponseCookie responseCookie) {
            this.name = responseCookie.getName();
            this.domain = responseCookie.getDomain();
            this.value = responseCookie.getValue();
            this.path = responseCookie.getPath();
            this.secure = responseCookie.isSecure();
            this.maxAge = responseCookie.getMaxAge().getSeconds();
            this.httpOnly = responseCookie.isHttpOnly();
            this.sameSite = responseCookie.getSameSite();
        }

        public SerializableCookie() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isSecure() {
            return secure;
        }

        public void setSecure(boolean secure) {
            this.secure = secure;
        }

        public long getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(long maxAge) {
            this.maxAge = maxAge;
        }

        public boolean isHttpOnly() {
            return httpOnly;
        }

        public void setHttpOnly(boolean httpOnly) {
            this.httpOnly = httpOnly;
        }

        public String getSameSite() {
            return sameSite;
        }

        public void setSameSite(String sameSite) {
            this.sameSite = sameSite;
        }
    }

    private Map<String, SerializableCookie> cookies = new HashMap<>();

    public Map<String, SerializableCookie> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, SerializableCookie> cookies) {
        this.cookies = cookies;
    }
}
