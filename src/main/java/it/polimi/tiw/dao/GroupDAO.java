package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.tiw.beans.Group;

public class GroupDAO {

	private Connection con;
	private Integer ID;

	public GroupDAO(Connection connection) {
		this.con = connection;
	}

	public List<Group> findMyGroups(String user) throws SQLException  {
		
		List<Group> myGroups = new ArrayList<Group>();
		
		String query = "SELECT * from group where creator = ?";
		try (PreparedStatement pStatement = con.prepareStatement(query);) {
			
			pStatement.setString(1, user);	// the user is the creator we wanna find
			
			try (ResultSet result = pStatement.executeQuery();) {
				while (result.next()) {
					
					Group group = new Group();
			
					group.setCreator(user);
					group.setID(result.getInt("ID"));
					group.setTitle(result.getString("title"));
					group.setCreationDate(result.getDate("creationDate"));
					group.setHowManyDays(result.getInt("howManyDays"));
					group.setMinParts(result.getInt("minParts"));
					group.setMaxParts(result.getInt("maxParts"));
					group.setParticipants(findUsersByGroup(group));
					
					myGroups.add(group);
				}
			}
		}
		return myGroups;
	}

	private List<String> findUsersByGroup(Group group) throws SQLException {
		
		List<String> users = new ArrayList<String>();
		
		String query = "SELECT * from invitations where groupID = ?";
		try (PreparedStatement pStatement = con.prepareStatement(query);) {
			
			pStatement.setInt(1, group.getID());
			
			try (ResultSet result = pStatement.executeQuery();) {
				while (result.next()) {
					String invitedUser = result.getString("user");
					users.add(invitedUser);
				}
			}
		}
		return users;
	}
	
	public List<Group> findOthersGroup(String user) throws SQLException {
		
		List<Group> othersGroups = new ArrayList<Group>();
					// gotta retrieve groups where user isnt the creator 
					// and where invitations contains user
					// and group.ID = invitations.groupID 
		String query = "SELECT * from group INNER JOIN invitations ON group.ID = invitations.groupID "
				+ "WHERE group.creator != ? AND invitations.user = ?";
		try (PreparedStatement pStatement = con.prepareStatement(query);) {
			pStatement.setString(1, user);
			pStatement.setString(2, user);
			
			try (ResultSet result = pStatement.executeQuery();) {
				while (result.next()) {
					
					Group group = new Group();
					
					group.setCreator(result.getString("creator"));
					group.setID(result.getInt("ID"));
					group.setTitle(result.getString("title"));
					group.setCreationDate(result.getDate("creationDate"));
					group.setHowManyDays(result.getInt("howManyDays"));
					group.setMinParts(result.getInt("minParts"));
					group.setMaxParts(result.getInt("maxParts"));
					group.setParticipants(findUsersByGroup(group));
					
					othersGroups.add(group);	
				}
			}
		}
		return othersGroups;
	}

	
	
//	public void createGroup(String title, Date startDate, Integer duration, Integer minParts, Integer maxParts, String creator, List<String> invitatedUsers) throws SQLException {
//		
//	}

}

