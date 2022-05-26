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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import edu.leicester.co2103.ErrorInfo;
import edu.leicester.co2103.domain.Module;
import edu.leicester.co2103.domain.Session;
import edu.leicester.co2103.repo.ModuleRepository;
import edu.leicester.co2103.repo.SessionRepository;

@RestController
@RequestMapping("/api")
public class ModuleRestController {
	
	public static final Logger logger = LoggerFactory.getLogger(ModuleRestController.class);
	
	@Autowired
	private ModuleRepository moduleRepo;
	
	@Autowired
	private SessionRepository sessionRepo;
	
	@GetMapping("/modules/{code}")
	public ResponseEntity<?> getModule(@PathVariable("code") String code) {
		
		if (moduleRepo.findById((String) code).isPresent()) {
			Module module = moduleRepo.findById((String) code).get();
			return new ResponseEntity<Module>(module, HttpStatus.OK);
		} else
			return new ResponseEntity<ErrorInfo>(new ErrorInfo("Module with code " + code + " not found"), HttpStatus.NOT_FOUND);
	}
	
	@GetMapping("/modules")
	public ResponseEntity<List<Module>> listAllModules() {
		List<Module> modules = moduleRepo.findAll();
		if (modules.isEmpty()) {
			return new ResponseEntity<List<Module>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<Module>>(modules, HttpStatus.OK);
	}
	
	@GetMapping("/modules/{code}/sessions")
	public ResponseEntity<?> getSessions(@PathVariable("code") String code) {
		
		if (moduleRepo.findById((String) code).isPresent()) {
			Module module = moduleRepo.findById((String) code).get();
			return new ResponseEntity<List<Session>>(module.getSessions(), HttpStatus.OK);
		} else
			return new ResponseEntity<ErrorInfo>(new ErrorInfo("Module with code " + code + " not found"), HttpStatus.NOT_FOUND);
	}
	
	@GetMapping("/modules/{code}/sessions/{id}")
    public ResponseEntity<?> getSession(@PathVariable("code") String code, @PathVariable("id") int id) {
        if (moduleRepo.findById(code).isPresent()) {
            Module module = moduleRepo.findById(code).get();
            for (Session s: module.getSessions()) {
                if(s.getId() == id) {
                    return new ResponseEntity<Session>(s, HttpStatus.OK);
                }
            }
            return new ResponseEntity<ErrorInfo>(new ErrorInfo("Session with id " + id + " not found"),
                    HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<ErrorInfo>(new ErrorInfo("Module with code " + code + " not found"),
                HttpStatus.NOT_FOUND);
    }	
	
	
	@PostMapping("/modules")
	public ResponseEntity<?> createModule(@RequestBody Module module, UriComponentsBuilder ucBuilder) {

		if (moduleRepo.existsById(module.getCode())) {
			return new ResponseEntity<ErrorInfo>(new ErrorInfo("A module code " + module.getCode() + " already exists."),
					HttpStatus.CONFLICT);
		}
		moduleRepo.save(module);

		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(ucBuilder.path("/api/modules/{code}").buildAndExpand(module.getTitle()).toUri());
		return new ResponseEntity<String>("A new module has been created",headers, HttpStatus.CREATED);
	}
	
	@PostMapping("/modules/{code}/sessions")
    public ResponseEntity<?> createSession(@RequestBody Session session, @PathVariable("code") String code, UriComponentsBuilder ucBuilder) {
        if (moduleRepo.existsById(code)) {
            Module module = moduleRepo.findById(code).get();
            module.getSessions().add(session);
            moduleRepo.save(module);

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(ucBuilder.path("/api/modules/{id}/sessions").buildAndExpand(session.getId()).toUri());
            return new ResponseEntity<String>("A new session in this module has been created", HttpStatus.CREATED);
        }
        else if (sessionRepo.existsById(session.getId())){
            return new ResponseEntity<ErrorInfo>(new ErrorInfo("A session id " + session.getId() + " already exists."),
                    HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<ErrorInfo>(new ErrorInfo("The module code " + code + " not found."),
                    HttpStatus.NOT_FOUND);
        }
    }
	
	@PatchMapping("/modules/{code}")
	public ResponseEntity<?> updateModule(@PathVariable("code") String code, @RequestBody Module newModule) {

		if (moduleRepo.findById((String) code).isPresent()) {
			Module currentModule = moduleRepo.findById((String) code).get();
			currentModule.setTitle(newModule.getTitle());
			currentModule.setLevel(newModule.getLevel());
			currentModule.setOptional(newModule.isOptional());
			
			currentModule.getSessions().clear();
			currentModule.getSessions().addAll(newModule.getSessions());

			moduleRepo.save(currentModule);
			return new ResponseEntity<Module>(currentModule, HttpStatus.OK);
		} else
			return new ResponseEntity<ErrorInfo>(new ErrorInfo("Module with code " + code + " not found."),
					HttpStatus.NOT_FOUND);

	}
	
	@PutMapping("/modules/{code}/sessions/{id}")
    public ResponseEntity<?> updateSessionPut(@PathVariable("code") String code, @PathVariable("id") long id, @RequestBody Session newSession) {
        if (moduleRepo.findById(code).isPresent()) {
            if (sessionRepo.findById(id).isPresent()) {
                Session currentSession = sessionRepo.findById(id).get();
                currentSession.setTopic(newSession.getTopic());
                currentSession.setDatetime(newSession.getDatetime());
                currentSession.setDuration(newSession.getDuration());

                sessionRepo.save(currentSession);
                return new ResponseEntity<Session>(currentSession, HttpStatus.OK);
            }
            else {
                return new ResponseEntity<ErrorInfo>(new ErrorInfo("Session with id " + id + " not found."),
                        HttpStatus.NOT_FOUND);
            }
        }
        else {
            return new ResponseEntity<ErrorInfo>(new ErrorInfo("Module with code " + code + " not found."), 
                    HttpStatus.NOT_FOUND);

        }
    }
	
	@PatchMapping("/modules/{code}/sessions/{id}")
    public ResponseEntity<?> updateSessionPatch(@PathVariable("code") String code, @PathVariable("id") long id, @RequestBody Session newSession) {
        if (moduleRepo.findById(code).isPresent()) {
            if (sessionRepo.findById(id).isPresent()) {
                Session currentSession = sessionRepo.findById(id).get();
                currentSession.setTopic(newSession.getTopic());
                currentSession.setDatetime(newSession.getDatetime());
                currentSession.setDuration(newSession.getDuration());

                sessionRepo.save(currentSession);
                return new ResponseEntity<Session>(currentSession, HttpStatus.OK);
            }
            else {
                return new ResponseEntity<ErrorInfo>(new ErrorInfo("Session with id " + id + " not found."),
                        HttpStatus.NOT_FOUND);
            }
        }
        else {
            return new ResponseEntity<ErrorInfo>(new ErrorInfo("Module with code " + code + " not found."), 
                    HttpStatus.NOT_FOUND);

        }
    }
	
	@DeleteMapping("/modules/{code}")
	public ResponseEntity<?> deleteModule(@PathVariable("code") String code) {

		if (moduleRepo.findById((String) code).isPresent()) {
			moduleRepo.deleteById((String) code);
			return ResponseEntity.ok("Module deleted successfully.");
		} else
			return new ResponseEntity<ErrorInfo>(new ErrorInfo("Module with code " + code + " not found."),
					HttpStatus.NOT_FOUND);

	}
	
	@DeleteMapping("/modules/{code}/sessions/{id}")
    public ResponseEntity<?> deleteSession(@PathVariable("code") String code, @PathVariable("id") long id) {

        if (moduleRepo.findById(code).isPresent()) {
            if (sessionRepo.findById(id).isPresent()) {
                sessionRepo.deleteById(id);
                return ResponseEntity.ok("Session deleted successfully.");
            }
            else {
                return new ResponseEntity<ErrorInfo>(new ErrorInfo("Session with id " + id + " not found."),
                        HttpStatus.NOT_FOUND);
            }
        } else
            return new ResponseEntity<ErrorInfo>(new ErrorInfo("Module with code " + code + " not found."),
                    HttpStatus.NOT_FOUND);
    }
	
}
