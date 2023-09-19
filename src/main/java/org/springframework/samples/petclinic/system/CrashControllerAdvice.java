package org.springframework.samples.petclinic.system;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;

/**
 * Controller advice that returns a special fragment in case the request was coming from
 * HTMX.
 *
 * @author Alexandre Grison
 */
@ControllerAdvice
public class CrashControllerAdvice {

	@ExceptionHandler(Exception.class)
	public String globalError(Exception exception, HtmxRequest request, Model model) throws Exception {
		if (request.isHtmxRequest()) {
			model.addAttribute("message", exception.getMessage());
			return "fragments/errors :: general";
		}

		// let thymeleaf handle it on its own
		throw exception;
	}

}
