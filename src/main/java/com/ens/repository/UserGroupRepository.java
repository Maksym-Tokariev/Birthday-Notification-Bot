package com.ens.repository;

import com.ens.models.UserGroup;
import com.ens.models.UserGroups;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@AllArgsConstructor
public class UserGroupRepository {

    private final JdbcTemplate jdbcTemplate;

    public Optional<UserGroup> findUserAndGroup(Long chatId, Long groupId) {
        log.info("Method findUserAndGroup called in UserGroupRepository with chatId: {} and groupId: {}", chatId, groupId);

        String sql = "SELECT * FROM user_groups WHERE chat_id=? AND group_id=?";
        try {
            UserGroup userGroup = jdbcTemplate.queryForObject(sql, new Object[]{chatId, groupId},
                    (rs, rowNum) -> new UserGroup(
                            rs.getLong("chat_id"),
                            rs.getLong("group_id")
                    ));
            log.info("UserGroup found: {}", userGroup);
            return Optional.ofNullable(userGroup);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Empty result when searching by chatId: {}, message: {}", chatId, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error in findUserAndGroup: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public List<UserGroups> findGroupByChatId(Long chatId) {
        log.info("Method findGroupByChatId called in UserGroupRepository with chatId: {}", chatId);

        String sql = "SELECT users_data.user_name, groups.name AS group_name\n" +
                "FROM users_data\n" +
                "INNER JOIN user_groups ON users_data.chat_id = user_groups.chat_id\n" +
                "INNER JOIN  groups ON user_groups.group_id = groups.group_id\n" +
                "WHERE users_data.chat_id = ?;";
        try {
            List<UserGroups> groupsList = jdbcTemplate.query(sql, new Object[]{chatId}, new UserGroupsRowMapper());

            log.info("User groups found: {}", groupsList);

            return groupsList;
        } catch (EmptyResultDataAccessException e) {
            log.debug("Empty result in findGroupByChatId when searching by chatId: {}, message: {}", chatId, e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error in findGroupByChatId: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public void deleteUserAndGroup(Long chatId) {
        log.info("Method deleteUserAndGroup called in UserGroupRepository with chatId: {}", chatId);
        String sql = "DELETE FROM user_groups WHERE chat_id=?";
        try {
            jdbcTemplate.update(sql, chatId);
            log.info("Deleted user {} and group", chatId);
        } catch (Exception e) {
            log.error("Error in deleteUserAndGroup: {}", e.getMessage());
        }
    }

    public void deleteGroup(String groupName, Long chatId) {
        log.info("Method deleteGroup called in UserGroupRepository with chatId: {}, and group: {} ", chatId, groupName);

        String sql = "DELETE FROM user_groups WHERE chat_id=? AND group_id IN (" +
                "SELECT group_id FROM groups WHERE name=?)";

        try {
            jdbcTemplate.update(sql, chatId, groupName);
            log.info("Deleted group {}, for user {}", groupName, chatId);
        } catch (Exception e) {
            log.error("Error in deleteGroup: {}", e.getMessage());
        }
    }

    public static class UserGroupsRowMapper implements RowMapper<UserGroups> {

        @Override
        public UserGroups mapRow(ResultSet rs, int rowNum) throws SQLException {
            String userName = rs.getString("user_name");
            String groupName = rs.getString("group_name");
            System.out.println("Fetched user_name: " + userName + ", group_name: " + groupName);
            if (userName == null || groupName == null) {
                System.out.println("Warning: Null value found in ResultSet.");
            }
            return new UserGroups(userName, groupName);
        }
    }
}
