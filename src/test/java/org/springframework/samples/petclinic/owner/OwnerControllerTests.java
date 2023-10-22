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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.samples.petclinic.htmx.HtmxTestUtils.toggleHtmx;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.util.Lists;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link OwnerController}
 *
 * @author Colin But
 * @author Alexandre Grison
 */
@WebMvcTest(OwnerController.class)
class OwnerControllerTests {

	private static final int TEST_OWNER_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private OwnerRepository owners;

	private Owner george() {
		Owner george = new Owner();
		george.setId(TEST_OWNER_ID);
		george.setFirstName("George");
		george.setLastName("Franklin");
		george.setAddress("110 W. Liberty St.");
		george.setCity("Madison");
		george.setTelephone("6085551023");
		Pet max = new Pet();
		PetType dog = new PetType();
		dog.setName("dog");
		max.setType(dog);
		max.setName("Max");
		max.setBirthDate(LocalDate.now());
		george.addPet(max);
		max.setId(1);
		return george;
	}

	;

	@BeforeEach
	void setup() {

		Owner george = george();
		given(this.owners.findByLastName(eq("Franklin"), any(Pageable.class)))
			.willReturn(new PageImpl<Owner>(Lists.newArrayList(george)));

		given(this.owners.findAll(any(Pageable.class))).willReturn(new PageImpl<Owner>(Lists.newArrayList(george)));

		given(this.owners.findById(TEST_OWNER_ID)).willReturn(george);
		Visit visit = new Visit();
		visit.setDate(LocalDate.now());
		george.getPet("Max").getVisits().add(visit);

	}

	@CsvSource({ "false,owners/createOrUpdateOwnerForm", "true,fragments/owners :: edit" })
	@ParameterizedTest
	void testInitCreationForm(boolean hxRequest, String expectedViewName) throws Exception {
		mockMvc.perform(toggleHtmx(get("/owners/new"), hxRequest))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("owner"))
			.andExpect(view().name(expectedViewName));
	}

	@ValueSource(booleans = { false, true })
	@ParameterizedTest
	void testProcessCreationFormSuccess(boolean hxRequest) throws Exception {
		mockMvc
			.perform(toggleHtmx(post("/owners/new"), hxRequest).param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "123 Caramel Street")
				.param("city", "London")
				.param("telephone", "01316761638"))
			.andExpect(status().is3xxRedirection());
	}

	@CsvSource({ "false,owners/createOrUpdateOwnerForm", "true,fragments/owners :: edit" })
	@ParameterizedTest
	void testProcessCreationFormHasErrors(boolean hxRequest, String expectedViewName) throws Exception {
		mockMvc
			.perform(toggleHtmx(post("/owners/new"), hxRequest).param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("city", "London"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("owner"))
			.andExpect(model().attributeHasFieldErrors("owner", "address"))
			.andExpect(model().attributeHasFieldErrors("owner", "telephone"))
			.andExpect(view().name(expectedViewName));
	}

	@CsvSource({ "false,owners/findOwners", "true,fragments/owners :: find-form" })
	@ParameterizedTest
	void testInitFindForm(boolean hxRequest, String expectedViewName) throws Exception {
		mockMvc.perform(toggleHtmx(get("/owners/find"), hxRequest))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("owner"))
			.andExpect(view().name(expectedViewName));
	}

	@CsvSource({ "false,owners/ownersList", "true,fragments/owners :: list" })
	@ParameterizedTest
	void testProcessFindFormSuccess(boolean hxRequest, String expectedViewName) throws Exception {
		Page<Owner> tasks = new PageImpl<Owner>(Lists.newArrayList(george(), new Owner()));
		Mockito.when(this.owners.findByLastName(anyString(), any(Pageable.class))).thenReturn(tasks);
		mockMvc.perform(toggleHtmx(get("/owners?page=1"), hxRequest))
			.andExpect(status().isOk())
			.andExpect(view().name(expectedViewName.contains("::") ? null : expectedViewName));
	}

	@ValueSource(booleans = { false, true })
	@ParameterizedTest
	void testProcessFindFormByLastName(boolean hxRequest) throws Exception {
		Page<Owner> tasks = new PageImpl<Owner>(Lists.newArrayList(george()));
		Mockito.when(this.owners.findByLastName(eq("Franklin"), any(Pageable.class))).thenReturn(tasks);
		mockMvc.perform(toggleHtmx(get("/owners?page=1"), hxRequest).param("lastName", "Franklin"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name(!hxRequest ? "redirect:/owners/" + TEST_OWNER_ID : null));
	}

	@CsvSource({ "false,owners/findOwners", "true,fragments/owners :: find-form" })
	@ParameterizedTest
	void testProcessFindFormNoOwnersFound(boolean hxRequest, String expectedViewName) throws Exception {
		Page<Owner> tasks = new PageImpl<Owner>(Lists.newArrayList());
		Mockito.when(this.owners.findByLastName(eq("Unknown Surname"), any(Pageable.class))).thenReturn(tasks);
		mockMvc.perform(toggleHtmx(get("/owners?page=1"), hxRequest).param("lastName", "Unknown Surname"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("owner", "lastName"))
			.andExpect(model().attributeHasFieldErrorCode("owner", "lastName", "notFound"))
			.andExpect(view().name(expectedViewName.contains("::") ? null : expectedViewName));

	}

	@CsvSource({ "false,owners/createOrUpdateOwnerForm", "true,fragments/owners :: edit" })
	@ParameterizedTest
	void testInitUpdateOwnerForm(boolean hxRequest, String expectedViewName) throws Exception {
		mockMvc.perform(toggleHtmx(get("/owners/{ownerId}/edit", TEST_OWNER_ID), hxRequest))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("owner"))
			.andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
			.andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
			.andExpect(model().attribute("owner", hasProperty("address", is("110 W. Liberty St."))))
			.andExpect(model().attribute("owner", hasProperty("city", is("Madison"))))
			.andExpect(model().attribute("owner", hasProperty("telephone", is("6085551023"))))
			.andExpect(view().name(expectedViewName));
	}

	@ValueSource(booleans = { false, true })
	@ParameterizedTest
	void testProcessUpdateOwnerFormSuccess(boolean hxRequest) throws Exception {
		mockMvc
			.perform(toggleHtmx(post("/owners/{ownerId}/edit", TEST_OWNER_ID), hxRequest).param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "123 Caramel Street")
				.param("city", "London")
				.param("telephone", "01616291589"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@ValueSource(booleans = { false, true })
	@ParameterizedTest
	void testProcessUpdateOwnerFormUnchangedSuccess(boolean hxRequest) throws Exception {
		mockMvc.perform(toggleHtmx(post("/owners/{ownerId}/edit", TEST_OWNER_ID), hxRequest))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@CsvSource({ "false,owners/createOrUpdateOwnerForm", "true,fragments/owners :: edit" })
	@ParameterizedTest
	void testProcessUpdateOwnerFormHasErrors(boolean hxRequest, String expectedViewName) throws Exception {
		mockMvc
			.perform(toggleHtmx(post("/owners/{ownerId}/edit", TEST_OWNER_ID), hxRequest).param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "")
				.param("telephone", ""))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("owner"))
			.andExpect(model().attributeHasFieldErrors("owner", "address"))
			.andExpect(model().attributeHasFieldErrors("owner", "telephone"))
			.andExpect(view().name(expectedViewName));
	}

	@CsvSource({ "false,owners/ownerDetails", "true,fragments/owners :: details" })
	@ParameterizedTest
	void testShowOwner(boolean hxRequest, String expectedViewName) throws Exception {
		mockMvc.perform(toggleHtmx(get("/owners/{ownerId}", TEST_OWNER_ID), hxRequest))
			.andExpect(status().isOk())
			.andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
			.andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
			.andExpect(model().attribute("owner", hasProperty("address", is("110 W. Liberty St."))))
			.andExpect(model().attribute("owner", hasProperty("city", is("Madison"))))
			.andExpect(model().attribute("owner", hasProperty("telephone", is("6085551023"))))
			.andExpect(model().attribute("owner", hasProperty("pets", not(empty()))))
			.andExpect(model().attribute("owner", hasProperty("pets", new BaseMatcher<List<Pet>>() {

				@Override
				public boolean matches(Object item) {
					@SuppressWarnings("unchecked")
					List<Pet> pets = (List<Pet>) item;
					Pet pet = pets.get(0);
					if (pet.getVisits().isEmpty()) {
						return false;
					}
					return true;
				}

				@Override
				public void describeTo(Description description) {
					description.appendText("Max did not have any visits");
				}
			})))
			.andExpect(view().name(expectedViewName));
	}

}
