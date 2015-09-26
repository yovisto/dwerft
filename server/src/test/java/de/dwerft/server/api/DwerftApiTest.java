package de.dwerft.server.api;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import de.dwerft.server.app.DwerftApplication;

/**
 * End-to-End API test class.
 * This class amkes API calls to {@link DwerftApi} and test if the results are correct.
 * 
 * Extend the {@link JerseyTest} and override the configure method to specify how to set up
 * {@link Application}. This tells the container how to set up the servlet.
 * 
 * Provide only api test calls and verify them with a basic approach. No internal tests.
 * 
 * @author Henrik Jürges (juerges.henrik@gmail.com)
 */

public class DwerftApiTest extends JerseyTest {

	@Override
	protected Application configure() {
		return new DwerftApplication();
	}
	
	// test if a non restricted api call gets us a 200 ok
	@Test
	public void testApiIsReachable() {
		callResponseIsOk(performCall("/no"));
	}
	
	// test if that one has access
	@Test
	public void testApiIsRestricted() {
		callResponseIsForbidden(performCall("/"));
	}

	
	/**
	 * 
	 * @param callResponse - response from a previous api call
	 */
	private void callResponseIsOk(Response callResponse) {
		checkResponse(callResponse, Response.Status.OK.getStatusCode());
	}
	
	private void callResponseIsForbidden(Response callResponse) {
		checkResponse(callResponse, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
	}
	
	private void checkResponse(Response callResponse, int httpStatusCode) {
		assertEquals(httpStatusCode, callResponse.getStatus());
	}
	
	/**
	 * 
	 * @param call - the api query
	 * @return a api call response
	 */
	private Response performCall(String call) {
		return target(call).request().get();
	}
	
}
