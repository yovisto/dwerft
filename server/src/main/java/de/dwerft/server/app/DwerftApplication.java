package de.dwerft.server.app;

import org.glassfish.jersey.server.ResourceConfig;
import org.secnod.shiro.jaxrs.ShiroExceptionMapper;
import org.secnod.shiro.jersey.AuthInjectionBinder;
import org.secnod.shiro.jersey.AuthorizationFilterFeature;
import org.secnod.shiro.jersey.SubjectFactory;

import de.dwerft.server.api.DwerftApi;

public class DwerftApplication extends ResourceConfig {
	
	public DwerftApplication() {
		super();
		
		// register shiro security
		register(new AuthorizationFilterFeature());
		register(new SubjectFactory());
		register(new AuthInjectionBinder());
		register(new ShiroExceptionMapper());
	
		// register real application
		register(new DwerftApi());
	}

}
