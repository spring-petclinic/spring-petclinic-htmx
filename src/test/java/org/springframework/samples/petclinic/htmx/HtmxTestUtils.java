package org.springframework.samples.petclinic.htmx;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * @author Alexandre Grison
 */
public class HtmxTestUtils {

	private HtmxTestUtils() {
	}

	public static MockHttpServletRequestBuilder toggleHtmx(MockHttpServletRequestBuilder builder, boolean toggle) {
		if (toggle) {
			builder.header("HX-Request", "true");
		}

		return builder;
	}

}
