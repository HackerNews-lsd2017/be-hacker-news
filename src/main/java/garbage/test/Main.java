package garbage.test;

import java.util.Random;

import datastructures.post.PostBody;

public class Main {

	
	public static void main(String[] args) throws InterruptedException {
		Long timeInterval = (1000L*60*60*24*7*5);
		Long oneMonthBack=(System.currentTimeMillis()-timeInterval);
		System.out.println(System.currentTimeMillis());
		System.out.println(oneMonthBack);
		System.out.println(timeInterval);

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
