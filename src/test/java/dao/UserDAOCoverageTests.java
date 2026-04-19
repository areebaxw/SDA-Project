package dao;

import models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("UserDAO - Real Code Coverage Tests")
public class UserDAOCoverageTests {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @InjectMocks
    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        userDAO = new UserDAO(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    }

    @Test
    @DisplayName("UserDAO: Find user by username")
    void testFindUserByUsername() throws SQLException {
        System.out.println("\n=== UserDAO: Find By Username ===");
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("user_id")).thenReturn(1);
        when(mockResultSet.getString("username")).thenReturn("testuser");
        
        User user = userDAO.authenticateUser("testuser", "password");
        
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        System.out.println("✓ PASS: Find user by username logic executed");
    }

    @Test
    @DisplayName("UserDAO: Create a new user")
    void testCreateUser() throws SQLException {
        System.out.println("\n=== UserDAO: Create User ===");
        
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("password123");
        user.setEmail("new@user.com");
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        
        boolean result = userDAO.createUser(user);
        
        assertTrue(result);
        System.out.println("✓ PASS: Create user logic executed");
    }

    @Test
    @DisplayName("UserDAO: Authenticate user with correct credentials")
    void testAuthenticateUserSuccess() throws SQLException {
        System.out.println("\n=== UserDAO: Auth Success ===");
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("username")).thenReturn("testuser");

        User user = userDAO.authenticateUser("testuser", "password");
        
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        System.out.println("✓ PASS: Successful authentication logic executed");
    }

    @Test
    @DisplayName("UserDAO: Fail authentication with incorrect credentials")
    void testAuthenticateUserFailure() throws SQLException {
        System.out.println("\n=== UserDAO: Auth Failure ===");
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        User user = userDAO.authenticateUser("wronguser", "wrongpassword");
        
        assertNull(user);
        System.out.println("✓ PASS: Failed authentication logic executed");
    }
    
    @Test
    @DisplayName("UserDAO: Update user details")
    void testUpdateUser() throws SQLException {
        System.out.println("\n=== UserDAO: Update User ===");
        
        User user = new User();
        user.setUserId(1);
        user.setUsername("testuser");
        user.setPassword("newpassword");
        user.setEmail("new@email.com");
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        
        boolean result = userDAO.updateUser(user);
        
        assertTrue(result);
        System.out.println("✓ PASS: Update user logic executed");
    }

    @Test
    @DisplayName("UserDAO: Delete a user")
    void testDeleteUser() throws SQLException {
        System.out.println("\n=== UserDAO: Delete User ===");
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        
        boolean result = userDAO.deleteUser(1);
        
        assertTrue(result);
        System.out.println("✓ PASS: Delete user logic executed");
    }
}