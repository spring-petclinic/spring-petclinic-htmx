package org.springframework.samples.petclinic.system;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.servlet.support.BindStatus;

import io.jstach.jstache.JStache;

@JStache(path = "fragments/selectField")
public class SelectField {

	public String label;

	public String name;

	public boolean date;

	public boolean valid = true;

	public String value;

	public List<SelectValue> values = new ArrayList<>();

	public String[] errors = new String[0];

	public SelectField(String label, String name, String value, List<String> values, BindStatus status) {
		this.label = label;
		this.name = name;
		this.value = value == null ? "" : value;
		if (status != null) {
			valid = !status.isError();
			errors = status.getErrorMessages();
			value = status.getValue() == null ? "" : status.getValue().toString();
		}
		for (String selection : values) {
			this.values.add(new SelectValue(selection, value.equals(selection)));
		}
	}

}

record SelectValue(String value, boolean selected) {
}
