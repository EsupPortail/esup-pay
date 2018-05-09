package org.esupportail.pay.security;

import org.jboss.logging.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Service;

@Service
public class PayboxCallbackAuthorizationFailureEventListener {

	private final Logger log = Logger.getLogger(getClass());

	@EventListener
	public void authorizationFailureEventCatch(AuthorizationFailureEvent authorizationFailureEvent) {

		if(authorizationFailureEvent.getSource() instanceof FilterInvocation) {
			FilterInvocation filterInvocation = (FilterInvocation) authorizationFailureEvent.getSource();
			if("/payboxcallback".equals(filterInvocation.getHttpRequest().getRequestURI())) {
				log.warn("payboxcallback from IP " + filterInvocation.getHttpRequest().getRemoteAddr() + " failed because authorizationFailure on spring security filter");
				log.debug("filterInvocation : "  + filterInvocation);
			}
		}

	}

}
