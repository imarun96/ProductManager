package net.codejava.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import net.codejava.model.PatientRecord;

@Controller
public class AppController {
	@Autowired
	private DiscoveryClient client;

	@GetMapping("/")
	public String viewHomePage(Model model) {
		URI uri = URI.create(StringUtils.EMPTY);
		Optional<URI> value = client.getInstances("Patient-Service").stream().map(ServiceInstance::getUri).findFirst();
		if (value.isPresent()) {
			Optional<URI> uriValue = value.map(mapper -> mapper.resolve("/listAllPatient"));
			if (uriValue.isPresent()) {
				uri = uriValue.get();
			}
		}
		RestTemplate restTemplate = new RestTemplate();
		Object[] patients = restTemplate.getForObject(uri, PatientRecord[].class);
		model.addAttribute("patients", patients);
		return "index";
	}

	@GetMapping("/new")
	public String showNewProductForm(Model model) {
		PatientRecord patientRecord = new PatientRecord();
		model.addAttribute("patientRecord", patientRecord);
		return "new_patient";
	}

	@PostMapping(value = "/save")
	public String saveProduct(@ModelAttribute("patientRecord") PatientRecord patientRecord) {
		URI uri = URI.create(StringUtils.EMPTY);
		Optional<URI> value = client.getInstances("Patient-Service").stream().map(ServiceInstance::getUri).findFirst();
		if (value.isPresent()) {
			Optional<URI> uriValue = value.map(mapper -> mapper.resolve("/addNewRecord"));
			if (uriValue.isPresent()) {
				uri = uriValue.get();
			}
		}
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> result = restTemplate.postForEntity(uri, patientRecord, String.class);
		if (result.getStatusCodeValue() == 200 || result.getStatusCodeValue() == 201) {
			return "redirect:/";
		} else {
			throw new RuntimeException("Some Error has occured " + result);
		}
	}

	@GetMapping("/delete/{id}")
	public String deleteProduct(@PathVariable(name = "id") Long id) throws URISyntaxException {
		URI uri = URI.create(StringUtils.EMPTY);
		Optional<URI> value = client.getInstances("Patient-Service").stream().map(ServiceInstance::getUri).findFirst();
		if (value.isPresent()) {
			Optional<URI> uriValue = value.map(mapper -> mapper.resolve("/delete/" + id));
			if (uriValue.isPresent()) {
				uri = uriValue.get();
			}
		}
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);
		if (result.getBody().equalsIgnoreCase("Deleted")) {
			return "redirect:/";
		} else {
			throw new RuntimeException("Some Error has occured " + result);
		}
	}
}