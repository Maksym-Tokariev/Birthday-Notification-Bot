package com.ens;
//
//import com.ens.models.UserGroups;
//import com.ens.repository.UserGroupRepository;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//@Slf4j
//@SpringBootTest
//@AllArgsConstructor
//class EnsApplicationTests {
//
//    @Mock
//    private final UserGroupRepository userGroupRepository;
//
//    @Test
//    public void testFindGroupsByChatId() {
//        Long chatId = 1248555095L;
//        // Замените на действительный chatId из вашей базы данных
//        List<UserGroups> userGroupsList = userGroupRepository.findGroupByChatId(chatId);
//        assertNotNull(userGroupsList);
//        assertFalse(userGroupsList.isEmpty());
//        for (UserGroups userGroup : userGroupsList) {
//            assertNotNull(userGroup.getUserName());
//            assertNotNull(userGroup.getGroupName());
//        }
//        log.info("Test passed: {}", userGroupsList);
//    }
//
//}
