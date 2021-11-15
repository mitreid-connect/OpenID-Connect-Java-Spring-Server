package cz.muni.ics.mdc;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.MDC;

public class RemoteAddressMDCFilter {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    private static final String REMOTE_ADDR = "remoteAddr";

    public void doFilter(ServletRequest servletRequest) {
        MDC.put(REMOTE_ADDR, getRemoteAddr((HttpServletRequest) servletRequest));
    }

    private String getRemoteAddr(HttpServletRequest request) {
        if (request.getRemoteAddr() != null) {
            return request.getRemoteAddr();
        }

        for (String header: IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);
            if (ipList != null && ipList.length() != 0 && !"unknown".equalsIgnoreCase(ipList)) {
                return ipList.split(",")[0];
            }
        }
        return "";
    }

}
