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
		String query = "SELECT * from group INNER JOIN user2group ON group.ID = user2group.groupID "
				+ "WHERE group.creator != ? AND user2group.username = ?";
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

	
	
	public void createGroup(String title, Date startDate, Integer duration, Integer minParts, Integer maxParts, String creator, List<String> invitatedUsers) throws SQLException {
		
		
		// disable autocommit
		con.setAutoCommit(false);
		
		try {
			
			// 1st step: save group info and retrieve its ID (auto-generated)
			
			int groupID = insertGroupOnly(title, startDate, duration, minParts, maxParts, creator);
			if(groupID == -1) throw new SQLException("GroupID is null");
		
			// 2nd step: save invited Users into user2group
			for(String username : invitatedUsers) {
				insertUser2Group(groupID, username);
			}
		
			// commit if everything is ok
			con.commit();
			
		} catch (SQLException e) {			
			con.rollback();
			throw e;
			
		} 
		
		// enable autocommit again
		con.setAutoCommit(true);
					
	}
	
	private void insertUser2Group(int groupID, String username) throws SQLException {
		
		String query = "INSERT into user2group (ID, username)   VALUES(?, ?)";
		PreparedStatement pStatement = null;
		
		try {
			pStatement = con.prepareStatement(query);
			
			pStatement.setInt(1, groupID);
			pStatement.setString(2, username);
			
			pStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw e;
		}
		
	}
	
	private int insertGroupOnly(String title, Date startDate, Integer duration, Integer minParts, Integer maxParts, String creator) throws SQLException {
		
		String query = "INSERT into group (title, startDate, duration, minParts, maxParts, creator)   VALUES(?, ?, ?, ?, ?, ?)";
		PreparedStatement pStatement = null;
		
		int generatedGroupID = -1;
		
		try {
			
			pStatement = con.prepareStatement(query);
			
			pStatement.setString(1, title);
			pStatement.setDate(2, (java.sql.Date) startDate);
			pStatement.setInt(3, duration);
			pStatement.setInt(4, minParts);
			pStatement.setInt(5, maxParts);
			pStatement.setString(6, creator);
			
			

			int rowsAffected = pStatement.executeUpdate();
			
	        if (rowsAffected > 0) {
	            try (ResultSet generatedKeys = pStatement.getGeneratedKeys()) {
	                if (generatedKeys.next()) {
	                    generatedGroupID = generatedKeys.getInt(1); // Retrieve the auto-generated GroupID
	                    // Use generatedGroupID as needed
	                    if(generatedGroupID == 0) generatedGroupID = -1;
	                }
	            } catch (SQLException e) {
	    			throw e;
	    		}	
	        }
			
		} catch (SQLException e) {
			throw e;
		}		

		return generatedGroupID; 
	}

}

