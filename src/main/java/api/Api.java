package api;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import datastructures.EditUserDTO;
import datastructures.User;
import datastructures.WholeStoryDTO;
import datastructures.post.CompletePostDTO;
import datastructures.post.PostBody;
import db.DbMapper;
import db.neo4j.MyNeo4jMapper;
import io.prometheus.client.Summary;
import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import util.JSONMapper;
import util.StatusMonitor;

@CrossOrigin
@RestController
public class Api {

	private DbMapper mapper = new MyNeo4jMapper();
	private JSONMapper jsonmap = new JSONMapper();
	private ApiUtil util = new ApiUtil();
	private ExecutorService threads = Executors.newCachedThreadPool();

	private static final Logger logger = LogManager.getLogger("logstash");

	@Autowired
	public Api() {
	}

	@RequestMapping("/java")
	public String echo() {
		logger.info("Logging java call");
		return "java call";
	}


	@RequestMapping(path = "/post", method = RequestMethod.POST)
	public ResponseEntity<String> post2(@RequestBody String json) {
		if (StatusMonitor.isOperational()) {
			Summary.Timer requestTimer = StatusMonitor.getRequestlatency().startTimer();
			StatusMonitor.incrementCounter();

			PostBody post = jsonmap.jsonToType(PostBody.class, json);
			if (!util.validatePost(post)) {
				requestTimer.observeDuration();
				logger.warn("This request is wrong: " + json);
				return ResponseEntity.status(400).body("Request is invalid. Check Hanesst_id, post_parent, "
						+ "post_title, post_type, pwd_hash and username ");
			}
			post.setTimestamp(System.currentTimeMillis());

			threads.execute(() -> mapper.persistPost(post));

			String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
			checkSpeed(requestTimer.observeDuration(), methodName, json);
			return ResponseEntity.status(201).body((post.getPost_parent() + " " + post.getPost_url() + " "
					+ post.getUsername() + " " + StatusMonitor.getLastPostId()));
		}
		return ResponseEntity.status(423).body("Application is under update");
	}

	@RequestMapping(path = "/getPosts", method = RequestMethod.GET)
	public List<CompletePostDTO> getPostsWithCount(@RequestParam(value = "page") int page,
			@RequestParam(value = "limit") int limit) {
		Summary.Timer requestTimer = StatusMonitor.getRequestlatency().startTimer();
		StatusMonitor.incrementCounter();

		/*
		 * When page is 0, then we skip 0 posts. Otherwise we skip multiplicity of limit
		 * posts (by default 30 posts are shown at once)
		 */
		int skip = limit * page;

		List<CompletePostDTO> posts = mapper.getPostsLimitHeavy(skip, limit);

		String input = "{page: " + page + ", limit: " + limit + "}";
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		checkSpeed(requestTimer.observeDuration(), methodName, input);
		return posts;
	}
	
	@RequestMapping(path = "/getPostsNoCount", method = RequestMethod.GET)
	public List<PostBody> getPosts(@RequestParam(value = "page") int page,
			@RequestParam(value = "limit") int limit) {
		Summary.Timer requestTimer = StatusMonitor.getRequestlatency().startTimer();
		StatusMonitor.incrementCounter();

		/*
		 * When page is 0, then we skip 0 posts. Otherwise we skip multiplicity of limit
		 * posts (by default 30 posts are shown at once)
		 */
		int skip = limit * page;

		List<PostBody> posts = mapper.getPostsLimit(skip, limit);

		String input = "{page: " + page + ", limit: " + limit + "}";
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		checkSpeed(requestTimer.observeDuration(), methodName, input);
		return posts;
	}
	
	@RequestMapping(path = "/upVote", method = RequestMethod.POST)
	public boolean upVote(@RequestParam(value = "hanesst_id") Integer hanesst_id) {
		Summary.Timer requestTimer = StatusMonitor.getRequestlatency().startTimer();
		StatusMonitor.incrementCounter();

		mapper.upVote(hanesst_id);
		
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		checkSpeed(requestTimer.observeDuration(), methodName, hanesst_id);
		return true;
	}
	
	@RequestMapping(path = "/downVote", method = RequestMethod.POST)
	public boolean downVote(@RequestParam(value = "hanesst_id") Integer hanesst_id) {
		Summary.Timer requestTimer = StatusMonitor.getRequestlatency().startTimer();
		StatusMonitor.incrementCounter();

		mapper.downVote(hanesst_id);
		
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		checkSpeed(requestTimer.observeDuration(), methodName, hanesst_id);
		return true;
	}

	@RequestMapping(path = "/from", method = RequestMethod.GET)
	public List<PostBody> getPostsBySite(@RequestParam(value = "site") String site) {
		Summary.Timer requestTimer = StatusMonitor.getRequestlatency().startTimer();
		StatusMonitor.incrementCounter();

		List<PostBody> posts = mapper.getPostsBySite(site);

		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		checkSpeed(requestTimer.observeDuration(), methodName, site);
		return posts;
	}

	@RequestMapping(path = "/getComments", method = RequestMethod.GET)
	public WholeStoryDTO getComments(@RequestParam(value = "hanesst_id") Integer hanesst_id) {
		Summary.Timer requestTimer = StatusMonitor.getRequestlatency().startTimer();
		StatusMonitor.incrementCounter();

		WholeStoryDTO posts = mapper.getComments(hanesst_id);

		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		checkSpeed(requestTimer.observeDuration(), methodName, hanesst_id);
		return posts;
	}

	@RequestMapping(path = "/addUser", method = RequestMethod.POST)
	public ResponseEntity<User> addUser(@RequestBody String json) {
		if (StatusMonitor.isOperational()) {
			Summary.Timer requestTimer = StatusMonitor.getRequestlatency().startTimer();

			StatusMonitor.incrementCounter();

			User u = jsonmap.jsonToType(User.class, json);

			if (!util.validateUser(u)) {
				requestTimer.observeDuration();
				return ResponseEntity.status(400).body(u);
			}

			User addedUser = mapper.addUser(u);
			if (addedUser == null) {
				return ResponseEntity.status(400).body(u);
			}

			String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
			checkSpeed(requestTimer.observeDuration(), methodName, json);

			logger.info("Created user with " + addedUser.getUser_name() + " username");

			return ResponseEntity.status(201).body(u);
		}
		return ResponseEntity.status(500).body(null);
	}

	@RequestMapping(path = "/editUser", method = RequestMethod.PUT)
	public User changeUser(@RequestBody String json) {
		Summary.Timer requestTimer = StatusMonitor.getRequestlatency().startTimer();

		StatusMonitor.incrementCounter();

		EditUserDTO user = jsonmap.jsonToType(EditUserDTO.class, json);
		User loggedUser = mapper.editUser(user.getUser_name(), user.getOldUser_pwd(), user.getNewUser_pwd());

		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		checkSpeed(requestTimer.observeDuration(), methodName, json);

		return loggedUser;
	}

	@RequestMapping(path = "/logIn", method = RequestMethod.POST)
	public User logIn(@RequestBody String json) {
		Summary.Timer requestTimer = StatusMonitor.getRequestlatency().startTimer();

		StatusMonitor.incrementCounter();

		User u = jsonmap.jsonToType(User.class, json);
		User loggedUser = mapper.logIn(u);

		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		checkSpeed(requestTimer.observeDuration(), methodName, json);

		return loggedUser;
	}

	@RequestMapping(path = "/status", method = RequestMethod.GET)
	public String status() {
		StatusMonitor.incrementCounter();

		return StatusMonitor.getStatus();
	}

	@RequestMapping(path = "/latest", method = RequestMethod.GET)
	public int latest() {
		StatusMonitor.incrementCounter();

		return StatusMonitor.getLastPostId();
	}

	@RequestMapping("/changeStatus/update")
	public boolean setUpdate() {

		StatusMonitor.setUpdate();
		logger.warn("Status has been changed to update. Somebody might be a dick.");
		return true;
	}

	@RequestMapping("/changeStatus/alive")
	public boolean setAlive() {

		StatusMonitor.setAlive();
		logger.warn("Status has been changed to alive. Somebody might be a dick");
		return true;
	}

	@Bean
	ServletRegistrationBean servletRegistrationBean() {
		DefaultExports.initialize();
		return new ServletRegistrationBean(new MetricsServlet(), "/metrics");
	}

	private void checkSpeed(Double speed, String operationName, Object input) {
		speed = speed * 1000;
		if (speed > 300)
			logger.warn("Speed of " + speed + " ms occured in " + operationName + " with following input: "
					+ input.toString() + ". This breaches the SLA of max. 300ms");
	}

}
