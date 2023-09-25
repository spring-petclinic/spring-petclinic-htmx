/*
 * Copyright 2012-2019 the original author or authors.
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

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import io.jstach.jstache.JStache;
import io.jstach.opt.spring.webmvc.JStachioModelView;
import io.jstach.opt.spring.webmvc.JStachioModelViewConfigurer;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller used to showcase what happens when an exception is thrown
 *
 * @author Michael Isvy
 * <p/>
 * Also see how a view that resolves to "error" has been added ("error.html").
 */
@Controller
class CrashController {

	@GetMapping("/oups")
	public String triggerException() {
		throw new RuntimeException(
				"Expected: controller used to showcase what " + "happens when an exception is thrown");
	}

}

@Component("error")
// TODO: it would be better to have this in request scope?
class CrashPageView implements JStachioModelView {

	private CrashPage page = new CrashPage();

	@Override
	public Object model() {
		return this.page;
	}

}

@JStache(path = "error")
class CrashPage extends BasePage {

	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}

@Component
class CrashPageConfigurer implements JStachioModelViewConfigurer {

	@Override
	public void configure(Object page, Map<String, ?> model, HttpServletRequest request) {
		if (page instanceof CrashPage) {
			CrashPage base = (CrashPage) page;
			base.setMessage((String) model.get("message"));
		}
	}

}
