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
		
		try {
			
			// 1st step: save group info and retrieve its ID (auto-generated)
			System.out.println("Entered 1st step");
			
			int ID = getLastGroupID() + 1;
			
			insertGroupOnly(title, startDate, duration, minParts, maxParts, creator);
		
			System.out.println("GroupID is " + ID);
			
			groupID = ID;

		
			// commit if everything is ok
			con.commit();
			
		} catch (SQLException e) {			
			con.rollback();
			throw e;
			
		} finally {
			// enable autocommit again
			con.setAutoCommit(true);
		}
		
		
		// disable autocommit
		con.setAutoCommit(false);
		
		try {
			

			System.out.println("Entered 2nd step");
			
			// 2nd step: save invited Users into user2group
			
			if(invitatedUsers != null) {
			for(String username : invitatedUsers) {
				
				insertUser2Group(groupID, username);

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
		}
		

					
	}
	
	private int getLastGroupID() throws SQLException{
		
		int lastGroupId = -1;

        String query = "SELECT MAX(ID) AS last_group_id FROM groupTable";
        
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
        	
        	statement = con.prepareStatement(query);
        	resultSet = statement.executeQuery();
        	
            if (resultSet.next()) {
                lastGroupId = resultSet.getInt("last_group_id");
                System.out.println("Last group ID: " + lastGroupId);
            } else {
                System.out.println("No groups found.");
            }
        } catch(SQLException e) {
        	throw e;
        }
                  

		return lastGroupId;
	}



	private void insertUser2Group(int groupID, String username) throws SQLException {
		
		String query = "INSERT into user2group (IDgroup, username)   VALUES(?, ?)";
		PreparedStatement pStatement = null;
		
		try {
			pStatement = con.prepareStatement(query);
			
			pStatement.setInt(1, groupID);
			pStatement.setString(2, username);
			
			pStatement.executeUpdate();
			
		} catch (SQLException e) {
			System.out.println("Generic Problems in inserting " + username + " into group " + groupID );
			throw e;
		}
		
	}
	
	private void insertGroupOnly(String title, Date startDate, Integer duration, Integer minParts, Integer maxParts, String creator) throws SQLException {
		
		String query = "INSERT into groupTable (title, creationDate, howManyDays, minParts, maxParts, creator)   VALUES(?, ?, ?, ?, ?, ?)";
		PreparedStatement pStatement = null;
		
		
		try {
			
			pStatement = con.prepareStatement(query);
			
			pStatement.setString(1, title);
			pStatement.setDate(2, startDate);
			pStatement.setInt(3, duration);
			pStatement.setInt(4, minParts);
			pStatement.setInt(5, maxParts);
			pStatement.setString(6, creator);
			
			System.out.println("Trying 1st prepared statement");

			pStatement.executeUpdate();

			System.out.println("1st statement OK");
			
		} catch (SQLException e) {
			System.out.println("Generic Problems in making group only");
			throw e;
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

