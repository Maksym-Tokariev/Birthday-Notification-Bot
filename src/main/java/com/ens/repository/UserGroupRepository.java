package com.ens.repository;

import com.ens.models.UserGroup;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
@AllArgsConstructor
public class UserGroupRepository {

    private final JdbcTemplate jdbcTemplate;

    public Optional<UserGroup> findUserAndGroup(Long chatId, Long groupId) {
        String sql = "SELECT * FROM user_groups WHERE chat_id=? AND group_id=?";
        try {
            UserGroup userGroup = jdbcTemplate.queryForObject(sql, new Object[]{chatId, groupId},
                    (rs, rowNum) -> new UserGroup(
                            rs.getLong("chat_id"),
                            rs.getLong("group_id")
                    ));
            return Optional.ofNullable(userGroup);
        } catch (Exception e) {
            log.error("Error in findUserAndGroup: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void deleteUserAndGroup(Long chatId) {
        String sql = "DELETE FROM user_groups WHERE chat_id=?";
        try {
            jdbcTemplate.update(sql, chatId);
            log.info("Deleted user {} and group", chatId);
        } catch (Exception e) {
            log.error("Error in deleteUserAndGroup: {}", e.getMessage());
        }
    }

}
