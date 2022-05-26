package edu.leicester.co2103.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import edu.leicester.co2103.ErrorInfo;
import edu.leicester.co2103.domain.Convenor;
import edu.leicester.co2103.domain.Module;
import edu.leicester.co2103.repo.ConvenorRepository;

@RestController
@RequestMapping("/api")
public class ConvenorRestController {
	
	public static final Logger logger = LoggerFactory.getLogger(ConvenorRestController.class);
	
	@Autowired
	private ConvenorRepository convenorRepo;
	
	@GetMapping("/convenors/{id}")
	public ResponseEntity<?> getConvenor(@PathVariable("id") long id) {
		
		if (convenorRepo.findById((long) id).isPresent()) {
			Convenor convenor = convenorRepo.findById((long) id).get();
			return new ResponseEntity<Convenor>(convenor, HttpStatus.OK);
		} else
			return new ResponseEntity<ErrorInfo>(new ErrorInfo("Convenor with id " + id + " not found"), HttpStatus.NOT_FOUND);
	}
	
	@GetMapping("/convenors")
	public ResponseEntity<List<Convenor>> listAllConvenors() {
		List<Convenor> convenors = convenorRepo.findAll();
		if (convenors.isEmpty()) {
			return new ResponseEntity<List<Convenor>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<Convenor>>(convenors, HttpStatus.OK);
	}
	
	@GetMapping("/convenors/{id}/modules")
	public ResponseEntity<?> getModules(@PathVariable("id") long id) {
		
		if (convenorRepo.findById((long) id).isPresent()) {
			Convenor convenor = convenorRepo.findById((long) id).get();
			return new ResponseEntity <List<Module>>(convenor.getModules(), HttpStatus.OK);
		} else
			return new ResponseEntity<ErrorInfo>(new ErrorInfo("Convenor with id " + id + " not found"), HttpStatus.NOT_FOUND);
	}
	
	@PostMapping("/convenors")
	public ResponseEntity<?> createConvenor(@RequestBody Convenor convenor, UriComponentsBuilder ucBuilder) {

		if (convenorRepo.existsById(convenor.getId())) {
			return new ResponseEntity<ErrorInfo>(new ErrorInfo("A convenor with Id " + convenor.getId() + " already exists."),
					HttpStatus.CONFLICT);
		}
		convenorRepo.save(convenor);
		

		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(ucBuilder.path("/api/convenors/{id}").buildAndExpand(convenor.getId()).toUri());
		return new ResponseEntity<String>("A new convenor has been created" , headers, HttpStatus.CREATED);
	}
	
	@PutMapping("/convenors/{id}")
	public ResponseEntity<?> updateConvenor(@PathVariable("id") long id, @RequestBody Convenor newConvenor) {

		if (convenorRepo.findById((long) id).isPresent()) {
			Convenor currentConvenor = convenorRepo.findById((long) id).get();
			currentConvenor.setName(newConvenor.getName());
			currentConvenor.setPosition(newConvenor.getPosition());
			
			currentConvenor.getModules().clear();
			currentConvenor.getModules().addAll(newConvenor.getModules());

			convenorRepo.save(currentConvenor);
			return new ResponseEntity<Convenor>(currentConvenor, HttpStatus.OK);
		} else
			return new ResponseEntity<ErrorInfo>(new ErrorInfo("Convenor with id " + id + " not found."),
					HttpStatus.NOT_FOUND);

	}
	
	@DeleteMapping("/convenors/{id}")
	public ResponseEntity<?> deleteConvenor(@PathVariable("id") long id) {

		if (convenorRepo.findById(id).isPresent()) {
			convenorRepo.deleteById(id);
	        return ResponseEntity.ok("Convenor deleted successfully.");
	    } else
	        return new ResponseEntity<ErrorInfo>(new ErrorInfo("Convenor with id " + id + " not found."),
	                HttpStatus.NOT_FOUND);
	}
	
}
