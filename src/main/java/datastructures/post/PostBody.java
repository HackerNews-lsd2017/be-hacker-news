package datastructures.post;

import java.util.Map;

import org.neo4j.driver.v1.types.Node;

public class PostBody {

	private String username;
	private String post_type;
	private String pwd_hash;
	private String post_title;
	private String post_url;
	private Integer post_parent;
	private Integer hanesst_id;
	private String post_text;
	private Long timestamp;
	private Long karma;

	public PostBody(Node n) {
		
		Map resultMap = n.asMap();
		
		setPost_title((String) resultMap.get("post_title"));
		System.out.println(this.post_title);

		setPost_text((String) resultMap.get("post_text"));

		setHanesst_id(Math.toIntExact((Long) resultMap.get("hanesst_id")));

		setPost_type((String) resultMap.get("post_type"));

		setPost_parent(Math.toIntExact((Long) resultMap.get("post_parent")));

		setUsername((String) resultMap.get("username"));

		setPwd_hash((String) resultMap.get("pwd_hash"));

		setPost_url((String) resultMap.get("post_url"));
		
		setKarma((Long)resultMap.get("karma"));
		
		
		timestamp = Math.round((((Double) resultMap.get("timestamp"))));
	}

	public PostBody() {

	}
	

	public Long getKarma() {
		return karma;
	}

	public void setKarma(Long karma) {
		this.karma = karma;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPost_type() {
		return post_type;
	}

	public void setPost_type(String post_type) {
		this.post_type = post_type;
	}

	public String getPwd_hash() {
		return pwd_hash;
	}

	public void setPwd_hash(String pwd_hash) {
		this.pwd_hash = pwd_hash;
	}

	public String getPost_title() {
		return post_title;
	}

	public void setPost_title(String post_title) {
		this.post_title = post_title;
	}

	public String getPost_url() {
		return post_url;
	}

	public void setPost_url(String post_url) {
		this.post_url = post_url;
	}

	public Integer getPost_parent() {
		return post_parent;
	}

	public void setPost_parent(Integer post_parent) {
		this.post_parent = post_parent;
	}

	public Integer getHanesst_id() {
		return hanesst_id;
	}

	public void setHanesst_id(Integer hanesst_id) {
		this.hanesst_id = hanesst_id;
	}

	public String getPost_text() {
		return post_text;
	}

	public void setPost_text(String post_text) {
		this.post_text = post_text;
	}
	
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	
	public Long getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return "PostBody [username=" + username + ", post_type=" + post_type + ", pwd_hash=" + pwd_hash
				+ ", post_title=" + post_title + ", post_url=" + post_url + ", post_parent=" + post_parent
				+ ", hanesst_id=" + hanesst_id + ", post_text=" + post_text + ", timestamp=" + timestamp + "]";
	}
	
	

}
