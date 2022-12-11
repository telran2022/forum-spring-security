package telran.java2022;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import telran.java2022.accounting.dao.UserAccountRepository;
import telran.java2022.accounting.dto.RolesResponseDto;
import telran.java2022.accounting.dto.UserAccountResponseDto;
import telran.java2022.accounting.dto.UserRegisterDto;
import telran.java2022.accounting.model.UserAccount;
import telran.java2022.accounting.service.UserAccountService;

@SpringBootTest
class ForumServiceSpringSecurityApplicationTests {

	@Autowired
	UserAccountService userAccountService;

	@Autowired
	UserAccountRepository repository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	ModelMapper modelMapper;

	UserAccount userAccount;

	@BeforeEach
	void setUp() throws Exception {
		repository.deleteAll();
		userAccount = new UserAccount("JavaFan", "1234", "John", "Smith");
		String password = passwordEncoder.encode(userAccount.getPassword());
		userAccount.setPassword(password);
		userAccount.setPasswordExpDate(LocalDate.now().plusDays(30));
		repository.save(userAccount);
	}

	@Test
	void testRegister() {
		UserAccount user = new UserAccount("Stranger", "4321", "Peter", "Jackson");
		String password = passwordEncoder.encode(user.getPassword());
		user.setPassword(password);
		user.setPasswordExpDate(LocalDate.now().plusDays(30));

		UserRegisterDto userRegisterDto = modelMapper.map(user, UserRegisterDto.class);
		UserAccountResponseDto userDto = userAccountService.addUser(userRegisterDto);

		assertEquals("Stranger", userDto.getLogin());
		assertTrue(userDto.getRoles().contains("USER"));
	}

	@Test
	void testGetUser() {
		UserAccountResponseDto userDto = userAccountService.getUser(userAccount.getLogin());
		assertEquals(userAccount.getLogin(), userDto.getLogin());
		assertTrue(userDto.getRoles().contains("USER"));
	}

	@Test
	void testDeleteUser() {
		UserAccountResponseDto userDto = userAccountService.removeUser(userAccount.getLogin());
		assertEquals("JavaFan", userDto.getLogin());
		assertTrue(userDto.getRoles().contains("USER"));
		assertFalse(repository.existsById(userAccount.getLogin()));
	}

	@Test
	void testUpdateUser() {
		//TODO
	}

	@Test
	void testAddRole() {
		RolesResponseDto roleDto = userAccountService.changeRolesList(userAccount.getLogin(), "MODERATOR", true);
		assertTrue(roleDto.getRoles().contains("MODERATOR"));
	}

	@Test
	void testDeleteRole() {
		RolesResponseDto roleDto = userAccountService.changeRolesList(userAccount.getLogin(), "USER", false);
		assertFalse(roleDto.getRoles().contains("USER"));
	}

	@Test
	void testChangePassword() {
		userAccountService.changePassword(userAccount.getLogin(), "4321");
		UserAccount user = repository.findById(userAccount.getLogin()).orElse(null);
		assertTrue(passwordEncoder.matches("4321", user.getPassword()));
	}

}
