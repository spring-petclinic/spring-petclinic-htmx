package org.springframework.samples.petclinic.system;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Controller advice that returns a special fragment in case the request was coming from
 * HTMX.
 *
 * @author Alexandre Grison
 */
@ControllerAdvice
public class CrashControllerAdvice {

	private final HttpServletRequest httpServletRequest;

	public CrashControllerAdvice(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}

	@ExceptionHandler(Exception.class)
	public String globalError(Exception exception, Model model) throws Exception {
		if (Boolean.parseBoolean(httpServletRequest.getHeader("HX-Request"))) {
			model.addAttribute("message",
					"Expected: controller used to showcase what " + "happens when an exception is thrown");
			return "fragments/errors :: general";
		}

		// let thymeleaf handle it on its own
		throw exception;
	}

}
