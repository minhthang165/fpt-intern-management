package com.fsoft.fintern;

import com.fsoft.fintern.services.CloudinaryService;
import com.fsoft.fintern.services.MessageService;
import com.fsoft.fintern.services.RecruitmentService;
import com.fsoft.fintern.services.UserService;
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
	private CloudinaryService cloudinaryService;

	@Autowired
	private RecruitmentService recruitmentService;

	//Test upload file to cloudinary
	@Test
	public void testUploadFile() throws BadRequestException, FileNotFoundException {
		Path path = Paths.get("D:\\test.jpg");
		String name = "file.txt";
		String originalFileName = "file.txt";
		String contentType = "text/plain";
		byte[] content = null;
		try {
			content = Files.readAllBytes(path);
		} catch (final IOException e) {
		}
		MultipartFile result = new MockMultipartFile(name,
				originalFileName, contentType, content);
		Map data = cloudinaryService.upload(result);
		System.out.println((String) data.get("url"));
	}

	@Test
	public void testGetRecruitment() throws BadRequestException {
		Pageable pageable = PageRequest.of(0, 10);
		recruitmentService.findAll(pageable);
	}
}