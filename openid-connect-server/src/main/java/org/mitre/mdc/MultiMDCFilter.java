package org.mitre.mdc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class MultiMDCFilter extends GenericFilterBean {

    private static final Logger log = LoggerFactory.getLogger(MultiMDCFilter.class);

    private final RemoteAddressMDCFilter remoteAddressMDCFilter;
    private final SessionIdMDCFilter sessionIdMDCFilter;

    public MultiMDCFilter() {
        this.remoteAddressMDCFilter = new RemoteAddressMDCFilter();
        this.sessionIdMDCFilter = new SessionIdMDCFilter();
		log.info("--- Initialized MultiMDCFilter ---");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException
    {
        remoteAddressMDCFilter.doFilter(servletRequest);
        sessionIdMDCFilter.doFilter(servletRequest);
        filterChain.doFilter(servletRequest, servletResponse);
        MDC.clear();
    }

}
