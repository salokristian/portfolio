package mcc.aalto.fi.chatapp.Models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Chat {
	public Long createdAt;
	public String title;
	public String id;

	// A hashmap of <member_id, joined_at> values from Firebase
	public HashMap<String, Long> members;

	// A list of User profiles fetched after fetching this chat
	// Note that these profiles might contain null fields if fetching fails or data is not present in DB
	public List<User> memberProfiles;

	public Chat() {
		memberProfiles = new ArrayList<>();
	}

	public Chat(Long createdAt, String title, HashMap<String, Long> members) {
		this.createdAt = createdAt;
		this.title = title;
		this.members = members;
	}
}
