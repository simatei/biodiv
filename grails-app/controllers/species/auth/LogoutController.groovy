package species.auth

import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.core.context.SecurityContextHolder

class LogoutController {

    def grailsLinkGenerator;
    def logoutHandlers;

	/**
	 * Index action. Redirects to the Spring security logout uri.
	 */
	def index = {
		// TODO put any pre-logout code here
        // Logout programmatically
        def auth = SecurityContextHolder.context.authentication
        if (auth) {
            logoutHandlers.each  { handler->
                handler.logout(request,response,auth)
            }
        }
//		redirect uri: request.scheme+"://"+request.serverName+SpringSecurityUtils.securityConfig.logout.filterProcessesUrl // '/j_spring_security_logout'
		redirect uri: request.scheme+"://"+request.serverName+"/biodiv-api/logout?refresh_token=${params.refresh_token}"
	}
}
