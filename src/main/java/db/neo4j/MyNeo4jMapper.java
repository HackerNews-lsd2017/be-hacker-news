package db.neo4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import org.neo4j.driver.v1.types.Node;

import datastructures.User;
import datastructures.WholeStoryDTO;
import datastructures.post.CompletePostDTO;
import datastructures.post.Post;
import datastructures.post.PostBody;
import db.DbMapper;
import util.StatusMonitor;

public class MyNeo4jMapper implements DbMapper {

	private Neo4jConnector connector = new Neo4jConnector();
	private Neo4jUtil util = new Neo4jUtil();
	private final Neo4jQuery query = new Neo4jQuery();
	private static final Logger logger = LogManager.getLogger("logstash");

	public boolean persistPost(PostBody pb) {

		Session s = connector.getSession();

		Map<String, Object> map = new HashMap();

		map.put("post_text", pb.getPost_text());
		map.put("post_title", pb.getPost_title());

		if (pb.getHanesst_id() == 0 || pb.getHanesst_id() == null) {
			StatementResult result = s.run(query.generateHanesst_idQuery());
			Record record = result.single();

			pb.setHanesst_id(record.get("id").asInt());
		}
		System.out.println(pb.getTimestamp()+pb.getHanesst_id());
		map.put("hanesst_id", pb.getHanesst_id());
		map.put("post_type", pb.getPost_type());
		map.put("post_parent", pb.getPost_parent());
		map.put("username", pb.getUsername());
		map.put("pwd_hash", pb.getPwd_hash());
		map.put("post_url", pb.getPost_url());
		map.put("timestamp", pb.getTimestamp().doubleValue());
		

		if (pb.getPost_parent() == -1)
			s.run(query.addPostQuery(), map);
		else
			s.run(query.addCommentQuery(), map);

		s.close();

		StatusMonitor.setLastPostId(pb.getHanesst_id());
		logger.info("Created post with "+pb.getPost_title()+" title and "+pb.getHanesst_id()+" id");
		// logger.info("Post: "+pb.getPost_title()+" of user "+pb.getUsername()+" has
		// been created successfully");

		return true;
	}

	public WholeStoryDTO getComments(int hanesst_id) {
		List<PostBody> pbl = new ArrayList<>();
		Session s = connector.getSession();
		StatementResult result = s.run(query.getCommentsQuery(hanesst_id));

		Record record = result.next();

		Map<String, Object> map = record.asMap();

		Node storyNode = (Node) map.get("story");
		List<Node> comments = (List<Node>) map.get("comments");

		Long count = (Long) map.get("numberOfcomments");
		PostBody story = new PostBody(storyNode);
		for (Node node : comments) {

			pbl.add(new PostBody(node));

		}
		s.close();

		return new WholeStoryDTO(story, pbl, count);
	}

	// Latest version
	public List<CompletePostDTO> getPostsLimitHeavy(int skip, int limit) {
		Session s = connector.getSession();
		if (limit >= 9999)
			logger.warn("Very heavy request. Stop being a dick.");

		Long oneHourBack=System.currentTimeMillis()-(1000*60*60*60*24*7);
		StatementResult result = s.run(query.getPostsLimitQuery(skip, limit,oneHourBack));

		List<CompletePostDTO> completeList = new ArrayList<CompletePostDTO>();

		while (result.hasNext()) {

			Record record = result.next();
			Node n = record.get("post").asNode();
			Long count = record.get("numberOfcomments").asLong();

			CompletePostDTO completePost = new CompletePostDTO(new PostBody(n), count);

			completeList.add(completePost);
		}

		s.close();
		return completeList;
	}
	
	public void upVote(Integer hanesst_id) {
		Session s = connector.getSession();

		StatementResult result = s.run(query.upVote(hanesst_id));

		s.close();
	}
	
	public void downVote(Integer hanesst_id) {
		Session s = connector.getSession();

		StatementResult result = s.run(query.downVote(hanesst_id));

		s.close();
	}
	
	public List<PostBody> getPostsLimit(int skip, int limit) {
		Session s = connector.getSession();

		StatementResult result = s.run(query.getPostsLimit2Query(skip,limit));
		List<PostBody> list = util.castMultiplePostNodesToList(result);

		s.close();
		return list;
	}

	public List<PostBody> getPostsBySite(String site) {
		Session s = connector.getSession();

		StatementResult result = s.run(query.getPostsBySiteQuery(), Values.parameters("site", site));
		List<PostBody> list = util.castMultiplePostNodesToList(result);

		s.close();
		return list;
	}

	public User addUser(User u) {

		Session s = connector.getSession();
		Map<String, Object> map = new HashMap();

		map.put("user_name", u.getUser_name());
		map.put("user_pwd", u.getUser_pwd());

		try {
			s.run(query.addUserQuery(), map);
		} catch (ClientException e) {
			// handle it properly
			logger.error(e + " " + e.code());

			return null;
		} finally {
			s.close();
		}

		logger.info("User " + u.getUser_name() + " has been created");
		return u;
	}

	public User editUser(String username, String oldPassword, String newPassword) {

		Session s = connector.getSession();

		StatementResult result = s.run(query.editUser(username, oldPassword, newPassword));
		User u = null;
		if (result.hasNext()) {
			u = new User(username, newPassword);
			logger.info("User " + username + " changed password");
		}
		s.close();

		return u;
	}

	public User logIn(User u) {

		Session s = connector.getSession();
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("user_name", u.getUser_name());
		map.put("user_pwd", u.getUser_pwd());

		StatementResult result = s.run(query.logInQuery(), map);

		Record record = null;
		try {
			record = result.single();

		} catch (NoSuchRecordException e) {
			s.close();
			e.printStackTrace();
			return null;
		} 

		Node n = record.get("u").asNode();
		Map<?, ?> resultMap = n.asMap();

		u.setUser_name((String) resultMap.get("user_name"));

		u.setUser_pwd((String) resultMap.get("user_pwd"));

		if (!u.getUser_name().equals(null) && !u.getUser_pwd().equals(null)) {
			s.close();

			logger.info("User " + u.getUser_name() + " has been logged in!");
			return u;
		}

		s.close();
		return null;
	}
	
	public void updateRetroKarma(Integer from, Integer to) {
		Session s = connector.getSession();

		StatementResult result = s.run(query.updateKarma(from, to));

		s.close();
	}
}
