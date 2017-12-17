package garbage.test;

import java.util.Random;

import datastructures.post.PostBody;
import db.neo4j.MyNeo4jMapper;

public class Main {

	
	public static void main(String[] args) throws InterruptedException {
		MyNeo4jMapper mapper = new MyNeo4jMapper();

		//mapper.getComments(445);
		for (int i=-10000;i<=12000000;i=i+40000) {
			mapper.updateRetroKarma(i, i+40000);
			Thread.sleep(2000);
			System.out.println("Current batch: "+i+"->"+i+40000);
		}
		

		//System.out.println(mapper.editUser("pg", "lol6", "lol7"));
		

	}
	
	public static int random(){
	    
	    //note a single Random object is reused here
	    Random randomGenerator = new Random();
	    int randomInt = randomGenerator.nextInt(3000);
	    return randomInt;
	    
	  }

	private static PostBody body() {
		PostBody pb = new PostBody();
		pb.setPost_title("Woz Interview: the early days of Apple");
		pb.setPost_text("");
		pb.setHanesst_id(3);
		pb.setPost_parent(-1);
		pb.setUsername("phyllis");
		pb.setPwd_hash("fyQgkcLMD1");
		pb.setPost_url("http://www.foundersatwork.com/stevewozniak.html");
		
		
		pb.setPost_type("story");
		
		return pb;
	}
	

}
