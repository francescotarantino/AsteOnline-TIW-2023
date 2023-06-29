package com.asteonline.js.dao;

import java.sql.*;

import com.asteonline.js.beans.User;
import com.asteonline.js.utils.Hash;

public class UserDAO {
	private final Connection con;

	public UserDAO(Connection con) {
		this.con = con;
	}

	/**
	 * This method returns the user with the given id.
	 * @param id the id of the user to retrieve
	 * @return a User object representing the user with the given id if a match is found in the database, null otherwise
	 */
	public User getUserByID(int id) throws SQLException {
		// Prepare the query
		String query = "SELECT id, username, email, firstname, lastname, shipping_address "
				+ "FROM user "
				+ "WHERE id = ?";

		PreparedStatement stmt = con.prepareStatement(query);
		stmt.setInt(1, id);

		ResultSet result = stmt.executeQuery();

		User user = null;
		// Check if a match is found
		if(result.isBeforeFirst()) {
			result.next();

			// Populate the User bean
			user = new User();
			user.setId(result.getInt("id"));
			user.setUsername(result.getString("username"));
			user.setEmail(result.getString("email"));
			user.setFirstname(result.getString("firstname"));
			user.setLastname(result.getString("lastname"));
			user.setShippingAddress(result.getString("shipping_address"));
		}

		stmt.close();
		result.close();
		return user;
	}

	/**
	 * This method tries to authenticate the user.
	 * @param usernameOrEmail either the email or the username of the user
	 * @param password the password of the user
	 * @return the {@link User} if the authentication is successful, null otherwise
	 */
	public User authenticateUser(String usernameOrEmail, String password) throws SQLException {
		// Hash the given password
		String hashedPassword = Hash.hashPassword(password);

		// Prepare the query
		String query = "SELECT id, username, email, firstname, lastname, shipping_address "
				+ "FROM user "
				+ "WHERE (username = ? AND password = ?) OR (email = ? AND password = ? AND email IS NOT NULL)";

		PreparedStatement stmt = con.prepareStatement(query);
		stmt.setString(1, usernameOrEmail);
		stmt.setString(2, hashedPassword);
		stmt.setString(3, usernameOrEmail);
		stmt.setString(4, hashedPassword);

		ResultSet result = stmt.executeQuery();

		User user = null;
		// Check if a match is found
		if(result.isBeforeFirst()) {
			result.next();

			// Populate the User bean
			user = new User();
			user.setId(result.getInt("id"));
			user.setUsername(result.getString("username"));
			user.setEmail(result.getString("email"));
			user.setFirstname(result.getString("firstname"));
			user.setLastname(result.getString("lastname"));
			user.setShippingAddress(result.getString("shipping_address"));
		}

		stmt.close();
		result.close();
		return user;
	}

	/**
	 * This method creates a new user.
	 * @param user the user to register
	 * @param password the password of the user
	 * @return the id of the newly created user
	 */
	public int createUser(User user, String password) throws SQLException {
		String hashedPassword = Hash.hashPassword(password);

		String query = "INSERT INTO user (username, email, firstname, lastname, password, shipping_address) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";

		PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		stmt.setString(1, user.getUsername());
		stmt.setString(2, user.getEmail());
		stmt.setString(3, user.getFirstname());
		stmt.setString(4, user.getLastname());
		stmt.setString(5, hashedPassword);
		stmt.setString(6, user.getShippingAddress());

		stmt.executeUpdate();

		// Get the id of the newly created user
		int id;
		ResultSet result = stmt.getGeneratedKeys();
		result.next();
		id = result.getInt(1);

		stmt.close();
		result.close();
		return id;
	}

	/**
	 * This method checks if the given username is already taken.
	 * @param username the username to check
	 * @return true if the username is already taken, false otherwise
	 */
	public boolean isUsernameTaken(String username) throws SQLException {
		String query = "SELECT * "
				+ "FROM user "
				+ "WHERE username = ?";


		PreparedStatement stmt = con.prepareStatement(query);
		stmt.setString(1, username);
		ResultSet result = stmt.executeQuery();

		// Check if a match is found
		boolean taken = result.isBeforeFirst();

		stmt.close();
		result.close();
		return taken;
	}
}
