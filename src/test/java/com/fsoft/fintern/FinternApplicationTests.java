package com.fsoft.fintern;

import com.fsoft.fintern.models.Conversation;
import com.fsoft.fintern.services.*;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Map;

@SpringBootTest
public class FinternApplicationTests {
	@Autowired
	ClassroomService classroomService;

	@Test
	public void Test() {
		Conversation conv = classroomService.getClassroomsByMentorId(4).getFirst().getConversation();
	}
}