package de.dwerft.server.app;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.secnod.shiro.jaxrs.ShiroExceptionMapper;
import org.secnod.shiro.jersey.AuthInjectionBinder;
import org.secnod.shiro.jersey.AuthorizationFilterFeature;
import org.secnod.shiro.jersey.SubjectFactory;

import de.dwerft.server.api.DwerftApi;

/**
 * The Class DwerftApplication.
 * Sets up the dwerft web application.
 * Register new application parts here.
 * 
 * @author Henrik Jürges (juerges.henrik@gmail.com)
 */
public class DwerftApplication extends ResourceConfig {
	
	/**
	 * Instantiates a new dwerft application.
	 */
	public DwerftApplication() {
		super();
		register(LoggingFilter.class);
		
		// register shiro security
		register(new AuthorizationFilterFeature());
		register(new SubjectFactory());
		register(new AuthInjectionBinder());
		register(new ShiroExceptionMapper());
	
		// register real application
		register(new DwerftApi());
	}

}
