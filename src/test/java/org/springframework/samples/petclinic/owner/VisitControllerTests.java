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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.samples.petclinic.htmx.HtmxTestUtils.toggleHtmx;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link VisitController}
 *
 * @author Colin But
 * @author Alexandre Grison
 */
@WebMvcTest(VisitController.class)
class VisitControllerTests {

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_PET_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private OwnerRepository owners;

	@BeforeEach
	void init() {
		Owner owner = new Owner();
		Pet pet = new Pet();
		owner.addPet(pet);
		pet.setId(TEST_PET_ID);
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(owner);
	}

	@CsvSource({ "false,pets/createOrUpdateVisitForm", "true,fragments/pets :: visits" })
	@ParameterizedTest
	void testInitNewVisitForm(boolean hxRequest, String expectedView) throws Exception {
		mockMvc
			.perform(
					toggleHtmx(get("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID), hxRequest))
			.andExpect(status().isOk())
			.andExpect(view().name(expectedView));
	}

	@ValueSource(booleans = { false, true })
	@ParameterizedTest
	void testProcessNewVisitFormSuccess(boolean hxRequest) throws Exception {
		mockMvc
			.perform(
					toggleHtmx(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID), hxRequest)
						.param("name", "George")
						.param("description", "Visit Description"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@CsvSource({ "false,pets/createOrUpdateVisitForm", "true,fragments/pets :: visits" })
	@ParameterizedTest
	void testProcessNewVisitFormHasErrors(boolean hxRequest, String expectedView) throws Exception {
		mockMvc
			.perform(
					toggleHtmx(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID), hxRequest)
						.param("name", "George"))
			.andExpect(model().attributeHasErrors("visit"))
			.andExpect(status().isOk())
			.andExpect(view().name(expectedView));
	}

}
