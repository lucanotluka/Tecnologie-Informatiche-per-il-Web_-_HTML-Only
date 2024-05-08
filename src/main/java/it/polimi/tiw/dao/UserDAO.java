package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;

@WebServlet("/UserDAO")
public class UserDAO extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection con;

	public UserDAO(Connection connection) {
		this.con = connection;
	}

	public User checkCredentials(String usrn, String pwd) throws SQLException {
		String query = "SELECT  username, name, surname, email, password FROM user WHERE username = ? AND password = ?";
		
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, usrn);
			pstatement.setString(2, pwd);
			
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					result.next();
					
					User user = new User();
					user.setPassword(result.getString("password"));
					user.setEmail(result.getString("email"));
					user.setUsername(result.getString("username"));
					user.setName(result.getString("name"));
					user.setSurname(result.getString("surname"));
					
					return user;
				}
			}
		}
	}
	
	public boolean usernameEmailUnique(String username, String email) throws SQLException{
		String query = "SELECT  username, email FROM user WHERE username = ? AND email = ?";
		
		try (PreparedStatement pStatement = con.prepareStatement(query);){
			pStatement.setString(1, username);
			pStatement.setString(2, email);
			try (ResultSet result = pStatement.executeQuery();) {
				if(!result.isBeforeFirst()) {
					return true;		// found nothing, given credentials are unique!
				} else {
					return false;
				}
				
			}
		}
	}
	
	public boolean registerSuccess(User user) throws SQLException {
		
		String query = "INSERT into user (username, name, surname, email, password)   VALUES(?, ?, ?, ?, ?)";
		
		// disable autocommit
		con.setAutoCommit(false);
		PreparedStatement pStatement = null;
		
		try {
			pStatement = con.prepareStatement(query);
			
			pStatement.setString(1, user.getUsername());
			pStatement.setString(2, user.getName());
			pStatement.setString(3, user.getSurname());
			pStatement.setString(4, user.getEmail());
			pStatement.setString(5, user.getPassword());
			
			
			pStatement.executeUpdate();
		
			// commit if everything is ok
			con.commit();
			
		} catch (SQLException e) {			
			con.rollback();
			throw e;
			
		} finally {
			
			try {
				pStatement.close();
			} catch (SQLException e1) {
				throw e1;
			}
			
			// enable autocommit again
			con.setAutoCommit(true);
					
		}
		
		return true;
	}
	
    public List<User> findAllUsersExcept(String excludedUser) throws SQLException {
        
    	List<User> users = new ArrayList<>();
        
    	String query = "SELECT * FROM User WHERE username != ? ORDER BY surname";
        try (PreparedStatement pStatement = con.prepareStatement(query)) {
        	
            pStatement.setString(1, excludedUser);
            try (ResultSet result = pStatement.executeQuery()) {
                
            	while (result.next()) {
                    
            		User user = new User();
                    user.setUsername(result.getString("username"));
                    user.setName(result.getString("name"));
                    user.setSurname(result.getString("surname"));
                    user.setEmail(result.getString("email"));
                    user.setPassword(result.getString("password"));
                    
                    users.add(user);
                }
            }
        }
        return users;
    }
	
	
}
