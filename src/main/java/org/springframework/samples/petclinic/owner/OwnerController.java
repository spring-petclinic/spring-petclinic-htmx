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
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
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

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";

	private static final String FRAGMENTS_OWNERS_EDIT = "fragments/owners :: edit";

	private static final String FRAGMENTS_OWNERS_FIND_FORM = "fragments/owners :: find-form";

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
	public String initCreationForm(Map<String, Object> model) {
		return handleInitCreationForm(model, VIEWS_OWNER_CREATE_OR_UPDATE_FORM);
	}

	@HxRequest
	@GetMapping("/owners/new")
	public String htmxInitCreationForm(Map<String, Object> model) {
		return handleInitCreationForm(model, FRAGMENTS_OWNERS_EDIT);
	}

	protected String handleInitCreationForm(Map<String, Object> model, String view) {
		Owner owner = new Owner();
		model.put("owner", owner);
		return view;
	}

	@PostMapping("/owners/new")
	public String processCreationForm(@Valid Owner owner, BindingResult result) {
		return handleProcessCreationForm(owner, result, VIEWS_OWNER_CREATE_OR_UPDATE_FORM);
	}

	@HxRequest
	@PostMapping("/owners/new")
	public String htmxProcessCreationForm(@Valid Owner owner, BindingResult result) {
		return handleProcessCreationForm(owner, result, FRAGMENTS_OWNERS_EDIT);
	}

	protected String handleProcessCreationForm(@Valid Owner owner, BindingResult result, String errorView) {
		if (result.hasErrors()) {
			return errorView;
		}

		this.owners.save(owner);
		return "redirect:/owners/" + owner.getId();
	}

	@GetMapping("/owners/find")
	public String initFindForm() {
		return "owners/findOwners";
	}

	@HxRequest
	@GetMapping("/owners/find")
	public String htmxInitFindForm() {
		return FRAGMENTS_OWNERS_FIND_FORM;
	}

	@GetMapping("/owners")
	public String ownersList(@RequestParam(defaultValue = "1") int page, Owner owner, BindingResult result,
			Model model) {
		return processFindForm(page, owner, result, model, "owners/findOwners", "owners/ownersList");
	}

	@HxRequest
	@GetMapping("/owners")
	public HtmxResponse htmxOwnersList(@RequestParam(defaultValue = "1") int page, Owner owner, BindingResult result,
			Model model) {
		String view = processFindForm(page, owner, result, model, FRAGMENTS_OWNERS_FIND_FORM,
				"fragments/owners :: list");
		return new HtmxResponse().addTemplate(view);
	}

	public String processFindForm(@RequestParam(defaultValue = "1") int page, Owner owner, BindingResult result,
			Model model, String emptyView, String listView) {
		// allow parameterless GET request for /owners to return all records
		if (owner.getLastName() == null) {
			owner.setLastName(""); // empty string signifies broadest possible search
		}

		// find owners by last name
		Page<Owner> ownersResults = findPaginatedForOwnersLastName(page, owner.getLastName());
		if (ownersResults.isEmpty()) {
			// no owners found
			result.rejectValue("lastName", "notFound", "not found");
			return emptyView;
		}

		if (ownersResults.getTotalElements() == 1) {
			// 1 owner found
			owner = ownersResults.iterator().next();
			return "redirect:/owners/" + owner.getId();
		}

		// multiple owners found
		return addPaginationModel(owner.getLastName(), page, model, ownersResults, listView);
	}

	private String addPaginationModel(String lastName, int page, Model model, Page<Owner> paginated, String listView) {
		model.addAttribute("listOwners", paginated);
		List<Owner> listOwners = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listOwners", listOwners);
		return listView;
	}

	private Page<Owner> findPaginatedForOwnersLastName(int page, String lastname) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return owners.findByLastName(lastname, pageable);
	}

	@GetMapping("/owners/{ownerId}/edit")
	public String initUpdateOwnerForm(@PathVariable("ownerId") int ownerId, Model model) {
		return handleInitUpdateOwnerForm(ownerId, model, VIEWS_OWNER_CREATE_OR_UPDATE_FORM);
	}

	@HxRequest
	@GetMapping("/owners/{ownerId}/edit")
	public String htmxInitUpdateOwnerForm(@PathVariable("ownerId") int ownerId, Model model) {
		return handleInitUpdateOwnerForm(ownerId, model, FRAGMENTS_OWNERS_EDIT);
	}

	protected String handleInitUpdateOwnerForm(int ownerId, Model model, String view) {
		Owner owner = this.owners.findById(ownerId);
		model.addAttribute(owner);
		return view;
	}

	@PostMapping("/owners/{ownerId}/edit")
	public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result,
			@PathVariable("ownerId") int ownerId) {
		return handleProcessUpdateOwnerForm(owner, result, ownerId, VIEWS_OWNER_CREATE_OR_UPDATE_FORM);
	}

	@HxRequest
	@PostMapping("/owners/{ownerId}/edit")
	public String htmxProcessUpdateOwnerForm(@Valid Owner owner, BindingResult result,
			@PathVariable("ownerId") int ownerId) {
		return handleProcessUpdateOwnerForm(owner, result, ownerId, FRAGMENTS_OWNERS_EDIT);
	}

	protected String handleProcessUpdateOwnerForm(Owner owner, BindingResult result, int ownerId, String view) {
		if (result.hasErrors()) {
			return view;
		}

		owner.setId(ownerId);
		this.owners.save(owner);
		return "redirect:/owners/{ownerId}";
	}

	/**
	 * Custom handler for displaying an owner.
	 * @param ownerId the ID of the owner to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/owners/{ownerId}")
	public ModelAndView showOwner(@PathVariable("ownerId") int ownerId) {
		return handleShowOwner(ownerId, "owners/ownerDetails");
	}

	@HxRequest
	@GetMapping("/owners/{ownerId}")
	public ModelAndView htmxShowOwner(@PathVariable("ownerId") int ownerId) {
		return handleShowOwner(ownerId, "fragments/owners :: details");
	}

	protected ModelAndView handleShowOwner(int ownerId, String view) {
		ModelAndView mav = new ModelAndView(view);
		Owner owner = this.owners.findById(ownerId);
		mav.addObject(owner);
		return mav;
	}

}
