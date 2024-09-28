package com.exe201.ilink.service.Impl;

import com.cloudinary.Cloudinary;
import com.exe201.ilink.model.exception.ILinkException;
import com.exe201.ilink.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImplement implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile multipartFile) throws IOException {

        if (multipartFile.isEmpty()) {
            throw new ILinkException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        return cloudinary.uploader()
                .upload(multipartFile.getBytes(),
                    Map.of("public_id", UUID.randomUUID().toString()))
                .get("url")
                .toString();
    }
}
