package com.ens.repository;

import com.ens.models.Group;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@AllArgsConstructor
public class GroupRepository {

    private final JdbcTemplate jdbcTemplate;

    public void save(Group group) {
        String sql = "INSERT INTO groups (group_id, name) VALUES (?, ?)";

        try {
            jdbcTemplate.update(sql, group.getGroupId(), group.getGroupName());
        } catch (DataAccessException e) {
            log.error("Error in saving group: {}", e.getMessage(), e);
        }

    }

    public Optional<Group> findById(Long groupId) {
        String sql = "SELECT * FROM groups WHERE group_id = ?";

        try {
            Group group = jdbcTemplate.queryForObject(sql, new GroupRowMapper(), groupId);
            return Optional.of(group);
        } catch (DataAccessException e) {
            log.error("Error in finding group: {}", e.getMessage());
            return Optional.empty();
        }

    }

    public List<Group> findAll() {
        String sql = "SELECT * FROM groups";
        return jdbcTemplate.query(sql, new GroupRowMapper());
    }

    public void saveUserGroup(Long chatId, Long groupId) {
        String sql = "INSERT INTO user_groups (group_id, chat_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(sql, groupId, chatId);
        } catch (DataAccessException e) {
            log.error("Error in saving user group: {}", e.getMessage(), e);
        }
    }

    public static class GroupRowMapper implements RowMapper<Group> {

        @Override
        public Group mapRow(ResultSet rs, int rowNum) throws SQLException {
            Group group = new Group();
            group.setGroupId(rs.getLong("group_id"));
            group.setGroupName(rs.getString("name"));
            return group;
        }
    }
}
