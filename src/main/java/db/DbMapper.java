package db;

import java.util.List;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import datastructures.User;
import datastructures.WholeStoryDTO;
import datastructures.post.CompletePostDTO;
import datastructures.post.PostBody;

public interface DbMapper {

	public boolean persistPost(PostBody pb);

	public WholeStoryDTO getComments(int hanesst_id);

	public List<CompletePostDTO> getPostsLimitHeavy(int skip, int limit);

	public void upVote(Integer hanesst_id);

	public void downVote(Integer hanesst_id);

	public List<PostBody> getPostsLimit(int skip, int limit);

	public List<PostBody> getPostsBySite(String site);

	public User addUser(User u);

	public User editUser(String username, String oldPassword, String newPassword);

	public User logIn(User u);

	public void updateRetroKarma(Integer from, Integer to);
}
