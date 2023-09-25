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

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.system.BasePage;
import org.springframework.samples.petclinic.system.Form;
import org.springframework.samples.petclinic.system.InputField;
import org.springframework.samples.petclinic.system.PagedModelPage;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
 * @author Alexandre Grison
 */
@Controller
class OwnerController {

	private final OwnerRepository owners;

	public OwnerController(OwnerRepository clinicService) {
		this.owners = clinicService;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable(name = "ownerId", required = false) Integer ownerId) {
		return ownerId == null ? new Owner() : this.owners.findById(ownerId);
	}

	@GetMapping("/owners/new")
	public View initCreationForm(Owner owner) {
		return JStachioModelView.of(new EditOwnerPage(owner));
	}

	@HxRequest
	@GetMapping("/owners/new")
	public View htmxInitCreationForm() {
		return JStachioModelView.of(new EditOwnerForm(new Owner()));
	}

	@PostMapping("/owners/new")
	public View processCreationForm(@Valid Owner owner, BindingResult result) {
		return handleProcessCreationForm(owner, result, false);
	}

	@HxRequest
	@PostMapping("/owners/new")
	public View htmxProcessCreationForm(@Valid Owner owner, BindingResult result) {
		return handleProcessCreationForm(owner, result, true);
	}

	protected View handleProcessCreationForm(@Valid Owner owner, BindingResult result, boolean fragment) {
		if (result.hasErrors()) {
			return fragment ? JStachioModelView.of(new EditOwnerForm(owner))
					: JStachioModelView.of(new EditOwnerPage(owner));
		}

		this.owners.save(owner);
		return new RedirectView("/owners/" + owner.getId());
	}

	@GetMapping("/owners/find")
	public View initFindForm(Owner owner) {
		return JStachioModelView.of(new FindOwnerPage(owner));
	}

	@HxRequest
	@GetMapping("/owners/find")
	public View htmxInitFindForm(Owner owner) {
		return JStachioModelView.of(new FindOwnerForm(owner));
	}

	@GetMapping("/owners")
	public View ownersList(@RequestParam(defaultValue = "1") int page, Owner owner, BindingResult result) {
		return processFindForm(page, owner, result, false);
	}

	@HxRequest
	@GetMapping("/owners")
	public View htmxOwnersList(@RequestParam(defaultValue = "1") int page, Owner owner, BindingResult result) {
		return processFindForm(page, owner, result, true);
	}

	public View processFindForm(@RequestParam(defaultValue = "1") int page, Owner owner, BindingResult result,
			boolean fragment) {
		// allow parameterless GET request for /owners to return all records
		if (owner.getLastName() == null) {
			owner.setLastName(""); // empty string signifies broadest possible search
		}

		// find owners by last name
		Page<Owner> ownersResults = findPaginatedForOwnersLastName(page, owner.getLastName());
		if (ownersResults.isEmpty()) {
			// no owners found
			result.rejectValue("lastName", "notFound", "not found");
			return JStachioModelView.of(fragment ? new FindOwnerForm(owner) : new FindOwnerPage(owner));
		}

		if (ownersResults.getTotalElements() == 1) {
			// 1 owner found
			owner = ownersResults.iterator().next();
			return new RedirectView("/owners/" + owner.getId());
		}

		// multiple owners found
		return fragment ? JStachioModelView.of(new OwnersList(page, ownersResults))
				: JStachioModelView.of(new OwnersPage(page, ownersResults));
	}

	private Page<Owner> findPaginatedForOwnersLastName(int page, String lastname) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return owners.findByLastName(lastname, pageable);
	}

	@GetMapping("/owners/{ownerId}/edit")
	public View initUpdateOwnerForm(@PathVariable("ownerId") int ownerId) {
		Owner owner = this.owners.findById(ownerId);
		return JStachioModelView.of(new EditOwnerPage(owner));
	}

	@HxRequest
	@GetMapping("/owners/{ownerId}/edit")
	public View htmxInitUpdateOwnerForm(@PathVariable("ownerId") int ownerId, HttpServletRequest request,
			HttpServletResponse response) {
		Owner owner = this.owners.findById(ownerId);
		response.addHeader("HX-Push-Url", request.getServletPath());
		return JStachioModelView.of(new EditOwnerForm(owner));
	}

	@PostMapping("/owners/{ownerId}/edit")
	public View processUpdateOwnerForm(@Valid Owner owner, BindingResult result, @PathVariable("ownerId") int ownerId) {
		return handleProcessUpdateOwnerForm(owner, result, ownerId, false);
	}

	@HxRequest
	@PostMapping("/owners/{ownerId}/edit")
	public View htmxProcessUpdateOwnerForm(@Valid Owner owner, BindingResult result,
			@PathVariable("ownerId") int ownerId) {
		return handleProcessUpdateOwnerForm(owner, result, ownerId, true);
	}

	protected View handleProcessUpdateOwnerForm(Owner owner, BindingResult result, int ownerId, boolean fragment) {
		if (result.hasErrors()) {
			return fragment ? JStachioModelView.of(new EditOwnerForm(owner))
					: JStachioModelView.of(new EditOwnerPage(owner));
		}

		owner.setId(ownerId);
		this.owners.save(owner);
		return new RedirectView("/owners/" + owner.getId());
	}

	/**
	 * Custom handler for displaying an owner.
	 * @param ownerId the ID of the owner to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/owners/{ownerId}")
	public View showOwner(@PathVariable("ownerId") int ownerId) {
		Owner owner = this.owners.findById(ownerId); // is this necessary?
		return JStachioModelView.of(new OwnerPage(owner));
	}

	@HxRequest
	@GetMapping("/owners/{ownerId}")
	public View htmxShowOwner(@PathVariable("ownerId") int ownerId, HttpServletResponse response) {
		response.addHeader("HX-Push-Url", "/owners/" + ownerId);
		Owner owner = this.owners.findById(ownerId); // is this necessary?
		return JStachioModelView.of(new OwnerDetails(owner));
	}

}

@JStache(path = "fragments/owners#findForm")
class FindOwnerForm extends BasePage {

	private final Owner owner;

	FindOwnerForm(Owner owner) {
		this.owner = owner;
	}

	public Form form() {
		return new Form("owner", this.owner);
	}

	public String[] errors() {
		return status("owner").getErrorMessages();
	}

	public Owner getOwner() {
		return owner;
	}

}

@JStache(path = "owners/findOwners")
class FindOwnerPage extends FindOwnerForm {

	FindOwnerPage(Owner owner) {
		super(owner);
	}

}

@JStache(path = "fragments/owners#list")
class OwnersList extends PagedModelPage<Owner> {

	OwnersList(int page, Page<Owner> paginated) {
		super(page, paginated);
	}

	public List<Owner> listOwners() {
		return list();
	}

}

@JStache(path = "owners/ownersList")
class OwnersPage extends OwnersList {

	OwnersPage(int page, Page<Owner> paginated) {
		super(page, paginated);
	}

}

@JStache(path = "fragments/owners#editForm")
@JStachePartials(@JStachePartial(name = "inputField", path = "fragments/inputField"))
class EditOwnerForm extends BasePage {

	final Owner owner;

	EditOwnerForm(Owner owner) {
		this.owner = owner;
	}

	public Owner getOwner() {
		return owner;
	}

	public Form form() {
		return new Form("owner", this.owner);
	}

	public String[] errors() {
		return status("owner").getErrorMessages();
	}

	public InputField firstName() {
		return new InputField("First Name", "firstName", this.owner.getFirstName(), "text",
				status("owner", "firstName"));
	}

	public InputField lastName() {
		return new InputField("Last Name", "lastName", this.owner.getLastName(), "text", status("owner", "lastName"));
	}

	public InputField address() {
		return new InputField("Address", "address", this.owner.getAddress(), "text", status("owner", "address"));
	}

	public InputField city() {
		return new InputField("City", "city", this.owner.getCity(), "text", status("owner", "city"));
	}

	public InputField telephone() {
		return new InputField("Telephone", "telephone", this.owner.getTelephone(), "text",
				status("owner", "telephone"));
	}

}

@JStache(path = "owners/createOrUpdateOwnerForm")
@JStachePartials(@JStachePartial(name = "inputField", path = "fragments/inputField"))
class EditOwnerPage extends EditOwnerForm {

	EditOwnerPage(Owner owner) {
		super(owner);
	}

}

@JStache(path = "fragments/owners#details")
class OwnerDetails extends BasePage {

	final Owner owner;

	OwnerDetails(Owner owner) {
		this.owner = owner;
	}

	public Owner getOwner() {
		return owner;
	}

}

@JStache(path = "owners/ownerDetails")
class OwnerPage extends OwnerDetails {

	OwnerPage(Owner owner) {
		super(owner);
	}

}
