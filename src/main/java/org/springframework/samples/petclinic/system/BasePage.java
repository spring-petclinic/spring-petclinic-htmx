package org.springframework.samples.petclinic.system;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.system.Application.Menu;
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.servlet.support.RequestContext;

public class BasePage {

	private Application application;

	private String active = "home";

	private @NonNull RequestContext context;

	@Autowired
	public void setApplication(Application application) {
		this.application = application;
	}

	public void activate(String name) {
		this.active = name;
	}

	public List<Menu> getMenus() {
		Menu menu = application.getMenu(active);
		if (menu != null) {
			application.getMenus().forEach(m -> m.setActive(false));
			menu.setActive(true);
		}
		return application.getMenus();
	}

	public BindStatus status(String name) {
		return this.context.getBindStatus(name + ".*");
	}

	public BindStatus status(String name, String field) {
		return this.context.getBindStatus(name + "." + field);
	}

	public void setRequestContext(@NonNull RequestContext context) {
		this.context = context;
	}

}
