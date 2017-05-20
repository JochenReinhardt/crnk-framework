package io.crnk.core;

import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.core.boot.CrnkBookt;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.internal.dispatcher.HttpRequestProcessorImpl;
import io.crnk.core.module.discovery.ConstantServiceUrlProvider;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.home.HomeModule;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;

public class HomeModuleTest {


	private CrnkBookt boot;

	@Before
	public void setup() {
		boot = new CrnkBookt();
		boot.addModule(HomeModule.newInstance());
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery("io.crnk.test.mock", new SampleJsonServiceLocator
				()));
		boot.boot();
	}

	@Test
	public void test() throws IOException {
		ArgumentCaptor<Integer> statusCaptor = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<byte[]> responseCaptor = ArgumentCaptor.forClass(byte[].class);

		HttpRequestContextBase requestContextBase = Mockito.mock(HttpRequestContextBase.class);

		Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
		Mockito.when(requestContextBase.getPath()).thenReturn("/");
		Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");

		HttpRequestProcessorImpl requestDispatcher = boot.getRequestDispatcher();
		requestDispatcher.process(requestContextBase);

		Mockito.verify(requestContextBase, Mockito.times(1)).setResponse(statusCaptor.capture(), responseCaptor.capture());
		Assert.assertEquals(200, (int) statusCaptor.getValue());

		String json = new String(responseCaptor.getValue());
		JsonNode response = boot.getObjectMapper().reader().readTree(json);

		JsonNode resourcesNode = response.get("resources");
		JsonNode usersNode = resourcesNode.get("tag:tasks");
		Assert.assertEquals("/tasks/", usersNode.get("href").asText());
	}


}