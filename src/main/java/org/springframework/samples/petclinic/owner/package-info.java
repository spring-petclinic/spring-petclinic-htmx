@JStacheConfig(using = PetClinicApplication.class)
@JStacheFormatterTypes(types = { PetType.class, LocalDate.class })
package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.PetClinicApplication;

import io.jstach.jstache.JStacheConfig;
import io.jstach.jstache.JStacheFormatterTypes;
