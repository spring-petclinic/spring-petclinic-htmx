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
package org.springframework.samples.petclinic.vet;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.system.PagedModelPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.View;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import io.jstach.jstache.JStache;
import io.jstach.opt.spring.webmvc.JStachioModelView;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Alexandre Grison
 */
@Controller
class VetController {

	private final VetRepository vetRepository;

	public VetController(VetRepository clinicService) {
		this.vetRepository = clinicService;
	}

	@GetMapping("/vets.html")
	public View showVetList(@RequestParam(defaultValue = "1") int page) {
		Page<Vet> paginated = findPaginated(page);
		return JStachioModelView.of(new VetsPage(page, paginated));
	}

	@HxRequest
	@GetMapping("/vets.html")
	public View htmxShowVetList(@RequestParam(defaultValue = "1") int page) {
		Page<Vet> paginated = findPaginated(page);
		return JStachioModelView.of(new VetList(page, paginated));
	}

	protected String handleVetList(int page, Model model, String view, HttpServletResponse response) {
		// Here we are returning an object of type 'Vets' rather than a collection of
		// Vet
		// objects so it is simpler for Object-Xml mapping
		Vets vets = new Vets();
		Page<Vet> paginated = findPaginated(page);
		vets.getVetList().addAll(paginated.toList());
		return addPaginationModel(page, paginated, model, view, response);
	}

	private String addPaginationModel(int page, Page<Vet> paginated, Model model, String view,
			HttpServletResponse response) {
		List<Vet> listVets = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listVets", listVets);
		response.addHeader("HX-Push-Url", "/vets.html");
		return view;
	}

	private Page<Vet> findPaginated(int page) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return vetRepository.findAll(pageable);
	}

	@GetMapping({ "/vets" })
	@ResponseBody
	public Vets showResourcesVetList() {
		// Here we are returning an object of type 'Vets' rather than a collection of
		// Vet objects so it is simpler for JSon/Object mapping
		Vets vets = new Vets();
		vets.getVetList().addAll(this.vetRepository.findAll());
		return vets;
	}

}

@JStache(path = "fragments/vets")
class VetList extends PagedModelPage<Vet> {

	VetList(int page, Page<Vet> paginated) {
		super(page, paginated);
	}

	public List<Vet> listVets() {
		return list();
	}

}

@JStache(path = "vets/vetList")
class VetsPage extends VetList {

	VetsPage(int page, Page<Vet> paginated) {
		super(page, paginated);
	}

}