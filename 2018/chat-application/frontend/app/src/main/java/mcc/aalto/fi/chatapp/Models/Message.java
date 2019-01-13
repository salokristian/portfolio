package mcc.aalto.fi.chatapp.Models;

import android.support.annotation.NonNull;

// A class for storing Messages that are fetched and sent to the firebase data
public class Message implements Comparable<Message> {
	// These are set automatically by firebase when fetching a message from Firebase
	public String sender;
	public String text;
	public Long createdAt;
	public Image image;

	// These are set manually after fetching the message
	public String id;
	public User senderData;

	// An empty constructor is needed by the firebase queries
	public Message() { }

	public Message(String sender, String text, Long createdAt) {
		this.sender = sender;
		this.text = text;
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "Message{" + "sender='" + sender + '\'' + ", text='" + text + '\'' + ", createdAt=" + createdAt + ", id='" + id + '\'' + ", senderData=" + senderData + '}';
	}

	@Override
	public int compareTo(@NonNull Message message) {
		return id.compareTo(message.id);
	}
}
