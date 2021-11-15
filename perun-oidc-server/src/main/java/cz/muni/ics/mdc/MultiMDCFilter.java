package cz.muni.ics.mdc;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
public class MultiMDCFilter extends GenericFilterBean {

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
