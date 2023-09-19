/*
 * Copyright 2023 the original author or authors.
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

package org.springframework.samples.petclinic.system;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;

public class PagedModelPage<T> extends BasePage {

	private final Page<T> paginated;

	private int page;

	public PagedModelPage(int page, Page<T> paginated) {
		this.page = page;
		this.paginated = paginated;
	}

	public List<T> list() {
		return paginated.getContent();
	}

	public boolean first() {
		return page == 1;
	}

	public boolean last() {
		return page == paginated.getTotalPages();
	}

	public boolean hasPages() {
		return paginated.getTotalPages() > 1;
	}

	public int totalPages() {
		return paginated.getTotalPages();
	}

	public int previous() {
		return page - 1;
	}

	public int next() {
		return page + 1;
	}

	public List<PageModel> pages() {
		return IntStream.range(1, paginated.getTotalPages() + 1).mapToObj(value -> new PageModel(value == page, value))
				.collect(Collectors.toList());
	}

	public static record PageModel(boolean current, int number) {
	}

}
