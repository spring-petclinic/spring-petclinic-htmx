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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import io.jstach.jstache.JStache;
import io.jstach.opt.spring.webmvc.JStachioModelView;

@Controller
class WelcomeController {

	@GetMapping("/")
	public JStachioModelView welcome() {
		return JStachioModelView.of(new WelcomePage());
	}

}

@JStache(path = "welcome")
class WelcomePage extends BasePage {

}
