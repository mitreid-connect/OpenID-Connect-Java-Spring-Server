package cz.muni.ics.mdc;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.MDC;

public class SessionIdMDCFilter {

    private static final int SIZE = 12;
    private static final String SESSION_ID = "sessionID";

    public void doFilter(ServletRequest servletRequest) {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        if (req.getSession() != null) {
            String id = req.getSession().getId();
            if (id != null && id.length() > SIZE) {
                id = id.substring(0, SIZE);
            }
            MDC.put(SESSION_ID, id);
        }
    }

}
