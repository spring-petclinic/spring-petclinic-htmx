package org.springframework.samples.petclinic.system;

import org.springframework.web.servlet.support.BindStatus;

import io.jstach.jstache.JStache;

@JStache(path = "fragments/inputField")
public class InputField {

	public String label;

	public String name;

	public boolean date;

	public boolean valid = true;

	public String value;

	public String[] errors = new String[0];

	public InputField(String label, String name, String value, String type, BindStatus status) {
		this.label = label;
		this.name = name;
		this.value = value == null ? "" : value;
		if (status != null) {
			valid = !status.isError();
			errors = status.getErrorMessages();
			value = status.getValue() == null ? "" : status.getValue().toString();
		}
		this.date = "date".equals(type);
	}

}
