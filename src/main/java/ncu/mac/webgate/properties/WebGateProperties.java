package ncu.mac.webgate.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties("application.proxy")
public class WebGateProperties {
    public static class VirtualHost {
        private String target;
        private String hostHeader;
        private String pauseLoginOnRedirectTo;
        private List<List<String>> textReplacements;

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getHostHeader() {
            return hostHeader;
        }

        public void setHostHeader(String hostHeader) {
            this.hostHeader = hostHeader;
        }

        public String getPauseLoginOnRedirectTo() {
            return pauseLoginOnRedirectTo;
        }

        public void setPauseLoginOnRedirectTo(String pauseLoginOnRedirectTo) {
            this.pauseLoginOnRedirectTo = pauseLoginOnRedirectTo;
        }

        public List<List<String>> getTextReplacements() {
            return textReplacements;
        }

        public void setTextReplacements(List<List<String>> textReplacements) {
            this.textReplacements = textReplacements;
        }
    }

    private Map<String, VirtualHost> hosts;
    private String userAgent;
    private String defaultRedirection;

    public Map<String, VirtualHost> getHosts() {
        return hosts;
    }

    public void setHosts(Map<String, VirtualHost> hosts) {
        this.hosts = hosts;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDefaultRedirection() {
        return defaultRedirection;
    }

    public void setDefaultRedirection(String defaultRedirection) {
        this.defaultRedirection = defaultRedirection;
    }
}
