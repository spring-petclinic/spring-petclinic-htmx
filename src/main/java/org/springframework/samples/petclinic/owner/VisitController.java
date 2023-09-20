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
package org.springframework.samples.petclinic.owner;

import java.util.Map;

import org.springframework.samples.petclinic.system.BasePage;
import org.springframework.samples.petclinic.system.Form;
import org.springframework.samples.petclinic.system.InputField;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import io.jstach.jstache.JStache;
import io.jstach.jstache.JStachePartial;
import io.jstach.jstache.JStachePartials;
import io.jstach.opt.spring.webmvc.JStachioModelView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 * @author Alexandre Grison
 */
@Controller
class VisitController {

	private final OwnerRepository owners;

	public VisitController(OwnerRepository owners) {
		this.owners = owners;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	/**
	 * Called before each and every @RequestMapping annotated method. 2 goals: - Make sure
	 * we always have fresh data - Since we do not use the session scope, make sure that
	 * Pet object always has an id (Even though id is not part of the form fields)
	 * @param petId
	 * @return Pet
	 */
	@ModelAttribute("visit")
	public Visit loadPetWithVisit(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId,
			Map<String, Object> model) {
		Owner owner = this.owners.findById(ownerId);

		Pet pet = owner.getPet(petId);
		model.put("pet", pet);
		model.put("owner", owner);

		Visit visit = new Visit();
		pet.addVisit(visit);
		return visit;
	}

	// Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is
	// called
	@GetMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public View initNewVisitForm(Owner owner, Pet pet, Visit visit) {
		return JStachioModelView.of(new VisitPage(owner, pet, visit));
	}

	@HxRequest
	@GetMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public View htmxInitNewVisitForm(Owner owner, Pet pet, @Valid Visit visit, HttpServletRequest request,
			HttpServletResponse response) {
		response.addHeader("HX-Push-Url", request.getServletPath());
		return JStachioModelView.of(new VisitForm(owner, pet, visit));
	}

	// Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is
	// called
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public View processNewVisitForm(Owner owner, Pet pet, @Valid Visit visit, BindingResult result) {
		if (result.hasErrors()) {
			return initNewVisitForm(owner, pet, visit);
		}

		owner.addVisit(pet.getId(), visit);
		this.owners.save(owner);
		return new RedirectView("/owners" + owner.getId());
	}

	@HxRequest
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public View htmxProcessNewVisitForm(@ModelAttribute Owner owner, Pet pet, @Valid Visit visit,
			BindingResult result) {
		if (result.hasErrors()) {
			return initNewVisitForm(owner, pet, visit);
		}

		owner.addVisit(pet.getId(), visit);
		this.owners.save(owner);
		return new RedirectView("/owners" + owner.getId());
	}

}

@JStache(path = "pets/createOrUpdateVisitForm")
@JStachePartials(@JStachePartial(name = "inputField", path = "fragments/inputField"))
class VisitPage extends VisitForm {

	VisitPage(Owner owner, Pet pet, Visit visit) {
		super(owner, pet, visit);
	}

}

@JStache(path = "fragments/pets#visits")
@JStachePartials(@JStachePartial(name = "inputField", path = "fragments/inputField"))
class VisitForm extends BasePage {

	final Owner owner;

	final Pet pet;

	final Visit visit;

	VisitForm(Owner owner, Pet pet, Visit visit) {
		this.owner = owner;
		this.pet = pet;
		this.visit = visit;
	}

	public Visit getVisit() {
		return visit;
	}

	public Owner getOwner() {
		return owner;
	}

	public Pet getPet() {
		return pet;
	}

	public Form form() {
		return new Form("visit", this.pet);
	}

	public String[] errors() {
		return status("visit").getErrorMessages();
	}

	public InputField date() {
		return new InputField("Date", "date", this.visit.getDate().toString(), "date", status("visit", "date"));
	}

	public InputField description() {
		return new InputField("Description", "description", this.visit.getDescription(), "text",
				status("visit", "description"));
	}

}
