package com.fsoft.fintern;

import com.fsoft.fintern.dtos.CreateUserDTO;
import com.fsoft.fintern.dtos.MessageDTO;
import com.fsoft.fintern.enums.Gender;
import com.fsoft.fintern.enums.MessageStatus;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.services.MessageService;
import com.fsoft.fintern.services.UserService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FinternApplicationTests {

	@Autowired
	private UserService userService;

	@Autowired
	private MessageService messageService;

	@Test
	public void testCreateIntern() throws BadRequestException {
		CreateUserDTO userDTO = new CreateUserDTO();
		userDTO.setFirst_name("John");
		userDTO.setLast_name("Doe");
		userDTO.setEmail("john.doe@gmail.com");
		userDTO.setPicture("/home/john.doe");
		userDTO.setGender(Gender.MALE);
		userDTO.setPhone_number("1251251");
		userDTO.setClassId(1); // Make sure to set a valid class_id
		userService.createIntern(userDTO);
	}

	@Test
	public void testDeleteMessage() throws BadRequestException {
		MessageDTO messageDTO = new MessageDTO();
		messageDTO.setMessageContent("Test Message");
		messageDTO.setMessageType("Text");
		messageDTO.setSenderId(1);
		messageDTO.setConversationId(1);
		messageService.addMessage(messageDTO);
	}
}