package edu.leicester.co2103.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.leicester.co2103.ErrorInfo;
import edu.leicester.co2103.domain.Convenor;
import edu.leicester.co2103.domain.Module;
import edu.leicester.co2103.domain.Session;
import edu.leicester.co2103.repo.ConvenorRepository;
import edu.leicester.co2103.repo.ModuleRepository;
import edu.leicester.co2103.repo.SessionRepository;

@RestController
@RequestMapping("/api")
public class SessionRestController {
	
	
	public static final Logger logger = LoggerFactory.getLogger(SessionRestController.class);
	
	@Autowired
	private ConvenorRepository convenorRepo;
	
	@Autowired
	private ModuleRepository moduleRepo;
	
	@Autowired
	private SessionRepository sessionRepo;
	
	@GetMapping("/sessions")
	public ResponseEntity<?> listSessions(@RequestParam(value = "convenor", required = false) Long id, 
			@RequestParam(value = "module", required = false) String code) {
		
		if (id == null && code == null) {
			List<Session> sessions = sessionRepo.findAll();
			if (sessions.isEmpty()) {
				return new ResponseEntity<List<Session>>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<List<Session>>(sessions, HttpStatus.OK);
		}
		else {
			List<Session> sessions = null;
			if (id != null && code != null) {
				if (convenorRepo.findById(id).isPresent() && moduleRepo.findById(code).isPresent()) {
					Convenor convenor = convenorRepo.findById(id).get();
					Module module = moduleRepo.findById(code).get();
					if (convenor.getModules().contains(module)) {
						sessions = module.getSessions();
					}
					return new ResponseEntity<List<Session>>(sessions, HttpStatus.OK);
				} else {
					if (!convenorRepo.findById(id).isPresent()) {
						return new ResponseEntity<ErrorInfo>(new ErrorInfo("Convenor with id " + id + " not found."),
								HttpStatus.NOT_FOUND);
					}
					else {
						return new ResponseEntity<ErrorInfo>(new ErrorInfo("Module with code " + code + " not found."),
								HttpStatus.NOT_FOUND);
					}
				}
			}
			else if (id != null || code != null){
				if (id != null) {
					if (convenorRepo.findById(id).isPresent()) {
						Convenor convenor = convenorRepo.findById(id).get();
						
						List<Module> moduleList = convenor.getModules();
						
						List<Session> sessionList = new ArrayList<Session>();
						for (Module m : moduleList) {
							sessionList.addAll(m.getSessions());
						}
						return new ResponseEntity<List<Session>>(sessionList, HttpStatus.OK);
					} else {
						return new ResponseEntity<ErrorInfo>(new ErrorInfo("Convenor with id " + id + " not found."),
								HttpStatus.NOT_FOUND);
					}
				}
			} else {
				return new ResponseEntity<ErrorInfo>(new ErrorInfo("Unkown request."),
						HttpStatus.BAD_REQUEST);
			}
		}
		return new ResponseEntity<ErrorInfo>(new ErrorInfo("Unkown request."),
				HttpStatus.BAD_REQUEST);
		
	}
	
	@DeleteMapping("/sessions")
	public ResponseEntity<?> deleteSessions() {

		sessionRepo.deleteAll();
		return ResponseEntity.ok("All sessions have been deleted");	
	}
	
}
