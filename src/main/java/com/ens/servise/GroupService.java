package com.ens.servise;

import com.ens.models.Group;
import com.ens.models.UserGroups;
import com.ens.repository.GroupRepository;
import com.ens.repository.UserGroupRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final CacheService cacheService;

    @Transactional
    public void registerGroup(Long groupId, String groupName) {
        log.info("Method registerGroup called in GroupService with groupId: {}", groupId);
        Group newGroup = new Group();

        newGroup.setGroupId(groupId);
        newGroup.setGroupName(groupName);

        groupRepository.save(newGroup);
        log.info("Group has been registered: {}", newGroup);
    }

    public List<UserGroups> getUserGroups(Long chatId) {
        log.info("Method listOfGroups called in GroupService with chatId: {}", chatId);

        List<UserGroups> groupsList = userGroupRepository.findGroupByChatId(chatId);

        if (groupsList.isEmpty()) {
            log.info("No groups found for chatId: {}", chatId);
        } else {
            log.info("Found {} groups", groupsList.size());
        }

        return groupsList;
    }

    public List<UserGroups> getCachedUserGroups(Long chatId) {
        var groupsCache = cacheService.getCachedGroups(chatId);
        if (groupsCache.isEmpty()) {
            var groups = getUserGroups(chatId);
            if (!groups.isEmpty()) {
                cacheService.cacheUserGroups(chatId, groups);
            }
            return groups;
        }
        return groupsCache;
    }

    public boolean groupExists(Long groupId) {
        log.info("Method groupExists called in GroupService with groupId: {}", groupId);
        return groupRepository.findById(groupId).isPresent();
    }

    public boolean isUserInGroup(Long chatId, Long groupId) {
        log.info("Method isUserInGroup called in GroupService with groupId: {}", groupId);
        return userGroupRepository.findUserAndGroup(chatId, groupId).isPresent();
    }

    public void addUserToGroup(Long chatId, Long groupId) {
        log.info("Method addUserToGroup called in GroupService with groupId: {}", groupId);
        groupRepository.saveUserGroup(chatId, groupId);
    }

    public void removeUserFromAllGroups(Long chatId) {
        log.info("Method removeUserFromAllGroups called in GroupService with chatId: {}", chatId);
        userGroupRepository.deleteUserAndGroup(chatId);
    }

    public void deleteUserGroup(String groupName, Long chatId) {
        log.info("Method deleteGroup called in GroupService with groupName: {}", groupName);
        userGroupRepository.deleteGroup(groupName, chatId);
    }
}
