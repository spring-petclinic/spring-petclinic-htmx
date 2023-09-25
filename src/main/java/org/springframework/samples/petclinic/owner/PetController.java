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

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.samples.petclinic.system.BasePage;
import org.springframework.samples.petclinic.system.Form;
import org.springframework.samples.petclinic.system.InputField;
import org.springframework.samples.petclinic.system.SelectField;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
 * @author Alexandre Grison
 */
@Controller
@RequestMapping("/owners/{ownerId}")
class PetController {

	private final OwnerRepository owners;

	public PetController(OwnerRepository owners) {
		this.owners = owners;
	}

	@ModelAttribute("types")
	public Collection<PetType> populatePetTypes() {
		return this.owners.findPetTypes();
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable("ownerId") int ownerId) {
		return this.owners.findById(ownerId);
	}

	@ModelAttribute("pet")
	public Pet findPet(@PathVariable("ownerId") int ownerId,
			@PathVariable(name = "petId", required = false) Integer petId) {
		return petId == null ? new Pet() : this.owners.findById(ownerId).getPet(petId);
	}

	@InitBinder("owner")
	public void initOwnerBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@InitBinder("pet")
	public void initPetBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new PetValidator());
	}

	@GetMapping("/pets/new")
	public View initCreationForm(Owner owner, Pet pet, ModelMap model) {
		owner.addPet(pet);
		@SuppressWarnings("unchecked")
		Collection<PetType> types = (Collection<PetType>) model.get("types");
		return JStachioModelView.of(new PetPage(owner, pet, types));
	}

	@HxRequest
	@GetMapping("/pets/new")
	public View htmxInitCreationForm(Owner owner, ModelMap model, HttpServletRequest request,
			HttpServletResponse response) {
		response.addHeader("HX-Push-Url", request.getServletPath());
		Pet pet = new Pet();
		owner.addPet(pet);
		model.put("pet", pet);
		@SuppressWarnings("unchecked")
		Collection<PetType> types = (Collection<PetType>) model.get("types");
		return JStachioModelView.of(new PetForm(owner, pet, types));
	}

	@PostMapping("/pets/new")
	public View processCreationForm(Owner owner, @Valid Pet pet, BindingResult result, ModelMap model) {
		if (StringUtils.hasLength(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null) {
			result.rejectValue("name", "duplicate", "already exists");
		}

		owner.addPet(pet);
		if (result.hasErrors()) {
			model.put("pet", pet);
			@SuppressWarnings("unchecked")
			Collection<PetType> types = (Collection<PetType>) model.get("types");
			return JStachioModelView.of(new PetPage(owner, pet, types));
		}

		this.owners.save(owner);
		return new RedirectView("/owners/{ownerId}");
	}

	@HxRequest
	@PostMapping("/pets/new")
	public View htmxProcessCreationForm(Owner owner, @Valid Pet pet, BindingResult result, ModelMap model) {
		if (StringUtils.hasLength(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null) {
			result.rejectValue("name", "duplicate", "already exists");
		}

		owner.addPet(pet);
		if (result.hasErrors()) {
			model.put("pet", pet);
			@SuppressWarnings("unchecked")
			Collection<PetType> types = (Collection<PetType>) model.get("types");
			return JStachioModelView.of(new PetForm(owner, pet, types));
		}

		this.owners.save(owner);
		return new RedirectView("/owners/{ownerId}");
	}

	@GetMapping("/pets/{petId}/edit")
	public View initUpdateForm(Owner owner, Pet pet, ModelMap model) {
		@SuppressWarnings("unchecked")
		Collection<PetType> types = (Collection<PetType>) model.get("types");
		return JStachioModelView.of(new PetPage(owner, pet, types));
	}

	@HxRequest
	@GetMapping("/pets/{petId}/edit")
	public View htmxInitUpdateForm(Owner owner, Pet pet, ModelMap model, HttpServletResponse response) {
		response.addHeader("HX-Push-Url", "/owners/" + owner.getId() + "/pets/" + pet.getId() + "/edit");
		@SuppressWarnings("unchecked")
		Collection<PetType> types = (Collection<PetType>) model.get("types");
		return JStachioModelView.of(new PetForm(owner, pet, types));
	}

	protected String handleInitUpdateForm(Owner owner, int petId, ModelMap model, String view) {
		Pet pet = owner.getPet(petId);
		model.put("owner", owner);
		model.put("pet", pet);
		return view;
	}

	@PostMapping("/pets/{petId}/edit")
	public View processUpdateForm(@Valid Pet pet, BindingResult result, Owner owner, ModelMap model) {
		return handleProcessUpdateForm(pet, result, owner, model, false);
	}

	@HxRequest
	@PostMapping("/pets/{petId}/edit")
	public View htmxProcessUpdateForm(@Valid Pet pet, BindingResult result, Owner owner, ModelMap model) {
		return handleProcessUpdateForm(pet, result, owner, model, true);
	}

	protected View handleProcessUpdateForm(@Valid Pet pet, BindingResult result, Owner owner, ModelMap model,
			boolean fragment) {
		if (result.hasErrors()) {
			model.put("pet", pet);
			@SuppressWarnings("unchecked")
			Collection<PetType> types = (Collection<PetType>) model.get("types");
			return fragment ? JStachioModelView.of(new PetForm(owner, pet, types))
					: JStachioModelView.of(new PetPage(owner, pet, types));
		}

		owner.addPet(pet);
		this.owners.save(owner);
		return new RedirectView("/owners/{ownerId}");
	}

}

@JStache(path = "pets/createOrUpdatePetForm")
@JStachePartials(@JStachePartial(name = "inputField", path = "fragments/inputField"))
class PetPage extends PetForm {

	PetPage(Owner owner, Pet pet, Collection<PetType> types) {
		super(owner, pet, types);
	}

}

@JStache(path = "fragments/pets#edit")
@JStachePartials(@JStachePartial(name = "inputField", path = "fragments/inputField"))
class PetForm extends BasePage {

	final Pet pet;

	final Owner owner;

	final Collection<PetType> types;

	PetForm(Owner owner, Pet pet, Collection<PetType> types) {
		this.pet = pet;
		this.owner = owner;
		this.types = types;
	}

	public Owner getOwner() {
		return owner;
	}

	public Pet getPet() {
		return pet;
	}

	public Collection<PetType> getTypes() {
		return types;
	}

	public Form form() {
		return new Form("pet", this.pet);
	}

	public String[] errors() {
		return status("pet").getErrorMessages();
	}

	public InputField nameField() {
		return new InputField("Name", "name", this.pet.getName(), "text", status("pet", "name"));
	}

	public InputField birthDate() {
		return new InputField("Birth Date", "birthDate", this.pet.getBirthDate().toString(), "date",
				status("pet", "birthDate"));
	}

	public SelectField type() {
		return new SelectField("Type", "type", this.pet.getType() == null ? "" : this.pet.getType().toString(),
				this.types.stream().map(item -> item.toString()).collect(Collectors.toList()), status("pet", "type"));
	}

}
