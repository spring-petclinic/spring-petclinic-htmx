/*
 * Copyright 2019-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.system;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.RequestContext;

import io.jstach.opt.spring.webmvc.JStachioModelViewConfigurer;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Utilities for rendering the HTML layout (menus, logs etc.)
 *
 * @author Dave Syer
 *
 */
@Component
public class ApplicationPageConfigurer implements JStachioModelViewConfigurer {

	private final Application application;

	public ApplicationPageConfigurer(Application application) {
		this.application = application;
	}

	@Override
	public void configure(Object page, Map<String, ?> model, HttpServletRequest request) {
		if (page instanceof BasePage) {
			BasePage base = (BasePage) page;
			Map<String, Object> map = new HashMap<>(model);
			base.setRequestContext(new RequestContext(request, map));
			base.setApplication(application);
		}
	}

}
