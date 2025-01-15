package com.ens.repository;

import com.ens.models.Users;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

@Slf4j
@Repository
@AllArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void save(Users users) {
        log.info("Method save called in UserRepository with user: {}", users);

        String sql = "INSERT INTO users_data (chat_Id, first_name, last_name, user_name, date_of_birth, registered_at)"
                + " VALUES (?, ?, ?, ?, ?, ?)";

        try {
            jdbcTemplate.update(sql,
                    users.getChatId(),
                    users.getFirstName(),
                    users.getLastName(),
                    users.getUserName(),
                    users.getDateOfBirth(),
                    users.getRegisteredAt());
        } catch (DataAccessException e) {
            log.error("Error trying to save user: {}",e.getMessage(), e);
        }

        log.info("Inserted users into database");
    }

    public Optional<Users> findById(Long chatId) {
        log.info("Method findById called in UserRepository with chatId: {}", chatId);

        String sql = "SELECT * FROM users_data WHERE chat_Id = ?";

        try {
            Users users = jdbcTemplate.queryForObject(sql, new Object[]{chatId}, new UserRowMapper());
            return Optional.of(users);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Empty result when searching by chatId: {}", e.getMessage());
            return Optional.empty();
        } catch (DataAccessException e) {
            log.error("Error finding user by id: {}",e.getMessage());
            throw e;
        }
    }

    public List<Long> findUsersByGroupId(Long groupId) {
        log.info("Method findUsersByGroupId called in UserRepository with groupId: {}", groupId);

        String sql = "SELECT chat_id FROM user_groups WHERE group_id = ?";

        try {
            return jdbcTemplate.query(sql, new Object[]{groupId}, (rs, rowNum) -> rs.getLong("chat_id"));
        } catch (EmptyResultDataAccessException e) {
            log.debug("Empty result when searching by groupId: {}", e.getMessage());
            return Collections.emptyList();
        } catch (DataAccessException e) {
            log.error("Error finding user by groupId: {}",e.getMessage());
            throw e;
        }
    }

    public List<Users> findAll() {
        log.info("Method findAll called in UserRepository");

        String sql = "SELECT * FROM users_data";

        log.info("SQL query: {}", sql);
        try {
            return jdbcTemplate.query(sql, new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            log.debug("Empty result when searching by users: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error finding users: {}", e.getMessage());
            throw e;
        }
    }

    public void deleteUser(Long chatId) {
        log.info("Method deleteUser called in UserRepository with chatId: {}", chatId);

        String sql = "DELETE FROM users_data WHERE chat_Id = ?";
        try {
            jdbcTemplate.update(sql, chatId);
            log.info("Deleted user: {}", chatId);
        } catch (DataAccessException e) {
            log.error("Error when deleting user by id: {}", e.getMessage(), e);
        }
    }

    public static class UserRowMapper implements RowMapper<Users> {
        @Override
        public Users mapRow(ResultSet rs, int rowNum) throws SQLException {
            Users users = new Users();
            users.setChatId(rs.getLong("chat_id"));
            users.setFirstName(rs.getString("first_name"));
            users.setLastName(rs.getString("last_name"));
            users.setUserName(rs.getString("user_name"));
            users.setDateOfBirth(rs.getDate("date_of_birth"));
            users.setRegisteredAt(rs.getTimestamp("registered_at"));
            return users;
        }
    }
}
