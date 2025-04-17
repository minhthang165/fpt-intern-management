package com.fsoft.fintern.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.internal.Logger;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map upload(MultipartFile file) {
        System.out.println("data sout: " + file.getSize());
        try {
            return cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto")
            );
        } catch (IOException ex) {
            throw new RuntimeException("Upload failed: " + ex.getMessage(), ex);
        }
    }
    public Map upload2(MultipartFile file) {
        System.out.println("data sout: " + file.getSize());
        try {
            // Lấy tên file gốc mà không bao gồm phần mở rộng
            String originalFilename = file.getOriginalFilename();
            String fileNameWithoutExtension = originalFilename != null
                    ? originalFilename.substring(0, originalFilename.lastIndexOf('.'))
                    : "unnamed";

            // Upload với public_id để giữ tên file gốc
            return cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto",
                            "public_id", fileNameWithoutExtension
                    )
            );
        } catch (IOException ex) {
            throw new RuntimeException("Upload failed: " + ex.getMessage(), ex);
        }
    }
}


