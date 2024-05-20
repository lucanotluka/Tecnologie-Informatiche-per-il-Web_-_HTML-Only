package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.beans.Group;

public class GroupDAO {

	private Connection con;

	public GroupDAO(Connection connection) {
		this.con = connection;
	}

	
	
	public List<Group> findMyGroups(String user) throws SQLException  {
		
		List<Group> myGroups = new ArrayList<Group>();
		
		String query = "SELECT * from groupTable where creator = ?";
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
					group.setParticipants(findUsersByGroupID(result.getInt("ID")));
					
					myGroups.add(group);
				}
			}
		}
		return myGroups;
	}

	private List<String> findUsersByGroupID(int ID) throws SQLException {
		
		List<String> users = new ArrayList<String>();
		
		String query = "SELECT * from user2group where IDgroup = ?";
		try (PreparedStatement pStatement = con.prepareStatement(query);) {
			
			pStatement.setInt(1, ID);
			
			try (ResultSet result = pStatement.executeQuery();) {
				while (result.next()) {
					String invitedUser = result.getString("username");
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
		String query = "SELECT * from groupTable INNER JOIN user2group ON groupTable.ID = user2group.IDgroup "
				+ "WHERE groupTable.creator != ? AND user2group.username = ?";
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
					group.setParticipants(findUsersByGroupID(result.getInt("ID")));
					
					othersGroups.add(group);	
				}
			}
		}
		return othersGroups;
	}

	
	
	public void createGroup(String title, Date startDate, Integer duration, Integer minParts, Integer maxParts, String creator, List<String> invitatedUsers) throws SQLException {
		
		int groupID = -1;
		
		// disable autocommit
		con.setAutoCommit(false);
		
		// prepare all the queries
		String query1 = "SELECT MAX(ID) AS last_group_id FROM groupTable";
		PreparedStatement statement1 = null;
        ResultSet resultSet1 = null;
		
		String query2 = "INSERT into groupTable (title, creationDate, howManyDays, minParts, maxParts, creator)   VALUES(?, ?, ?, ?, ?, ?)";
		PreparedStatement pStatement2 = null;
        
        String query3 = "INSERT into user2group (IDgroup, username)   VALUES(?, ?)";
		PreparedStatement pStatement3 = null;
        
		try {
			
			// 1st step: save group info and retrieve its ID (auto-generated)
			System.out.println("Entered 1st step");

			statement1 = con.prepareStatement(query1);
        	resultSet1 = statement1.executeQuery();
			if (resultSet1.next()) {
				groupID = resultSet1.getInt("last_group_id");
                System.out.println("Last group ID: " + groupID);
            } else {
                System.out.println("No groups found.");
                throw new SQLException();
            }
			
			// the new actual groupID
			groupID++;
			System.out.println("New group ID: " + groupID);
			
			pStatement2 = con.prepareStatement(query2);
			pStatement2.setString(1, title);
			pStatement2.setDate(2, startDate);
			pStatement2.setInt(3, duration);
			pStatement2.setInt(4, minParts);
			pStatement2.setInt(5, maxParts);
			pStatement2.setString(6, creator);
			pStatement2.executeUpdate();
			
			System.out.println("1st statement OK");
			
			
			// if Creator alone, doesnt need to invite
			if(invitatedUsers != null) {
				
				// 2nd step: save invited Users into user2group
				System.out.println("Entered 2nd step");
				pStatement3 = con.prepareStatement(query3);
				System.out.println("OK preparedStatement 3");
				pStatement3.setInt(1, groupID);
				System.out.println("OK groupID set");
				for(String username : invitatedUsers) {
					pStatement3.setString(2, username);
					System.out.println("Trying adding " + username);
					pStatement3.executeUpdate();
					System.out.println("[Group "+ groupID +"]: inserted " + username);
				}
			}

			// commit if everything is ok
			con.commit();
			
		} catch (SQLException e) {			
			con.rollback();
			throw e;
			
		} finally {
			
			// enable autocommit again
			con.setAutoCommit(true);
			
			if(statement1 != null) {
				try {
					statement1.close();
				}catch(Exception e) {
					throw e;
				}
			}
			if(pStatement2 != null) {
				try {
					pStatement2.close();
				}catch(Exception e) {
					throw e;
				}
			}
			if(pStatement3 != null) {
				try {
					pStatement3.close();
				}catch(Exception e) {
					throw e;
				}
			}
		}
		
		
					
	}
	


	public Group getGroupByID(Integer groupID) throws SQLException {
		Group group = null;
		
		String query = "SELECT * FROM groupTable WHERE ID = ?";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			
			pstatement.setInt(1, groupID);
			
			try (ResultSet result = pstatement.executeQuery();) {
				
				if (result.next()) {
					
					group = new Group();
					group.setCreator(result.getString("creator"));
					group.setID(groupID);
					group.setTitle(result.getString("title"));
					group.setCreationDate(result.getDate("creationDate"));
					group.setHowManyDays(result.getInt("howManyDays"));
					group.setMinParts(result.getInt("minParts"));
					group.setMaxParts(result.getInt("maxParts"));
					group.setParticipants(findUsersByGroupID(groupID));
				}
			}
		}
		
		return group;
	}

}

