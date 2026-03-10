package com.buy01.user.domain.port.in;

import org.springframework.web.multipart.MultipartFile;

public interface AvatarService {

    public void init();

    public String saveAvatar(MultipartFile avatar);

    public void deleteAvatar(String avatarUrl);
}
