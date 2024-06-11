package it.polimi.tiw.beans;


import java.sql.Date;
import java.util.List;

public class Group {

	private int ID;
	private String title;
	private Date creationDate;
	private int howManyDays;
	private int minParts;
	private int maxParts;
	private String creator;
	private List<String> participants;

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date startDate) {
		this.creationDate = startDate;
	}
	public int getHowManyDays() {
		return howManyDays;
	}
	public void setHowManyDays(int howManyDays) {
		this.howManyDays = howManyDays;
	}
	public int getMinParts() {
		return minParts;
	}
	public void setMinParts(int minParts) {
		this.minParts = minParts;
	}
	public int getMaxParts() {
		return maxParts;
	}
	public void setMaxParts(int maxParts) {
		this.maxParts = maxParts;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public List<String> getParticipants() {
		return participants;
	}
	public void setParticipants(List<String> participants) {
		this.participants = participants;
	}
	public int getID() {
		return this.ID;
	}
	public void setID(int id) {
		this.ID = id;
	}


}
