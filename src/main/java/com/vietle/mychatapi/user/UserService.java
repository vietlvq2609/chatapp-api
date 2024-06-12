package com.vietle.mychatapi.user;

import com.vietle.mychatapi.user.dto.UserCreateDTO;
import com.vietle.mychatapi.user.dto.UserLoginDTO;
import com.vietle.mychatapi.user.dto.UserResponseDTO;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.vietle.mychatapi.exception.ApiException;
import com.vietle.mychatapi.user.dto.UserUpdateDTO;

import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // @Value("${spring.cloud.azure.storage.blob.account-name}")
    // private String accountName;

    @Value("${spring.cloud.azure.storage.blob.account-key}")
    private String accountKey;

    @Value("${spring.cloud.azure.storage.blob.endpoint}")
    private String endpoint;

    @Value("${azure.storage.container-name}")
    private String containerName;

    private BlobContainerClient getBlobContainerClient() {
        return new BlobContainerClientBuilder()
                .connectionString(String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net",
                        "bellchat", accountKey))
                .containerName(containerName)
                .buildClient();
    }

    public UserResponseDTO getUserById(Long userId) {
        Optional<User> query = userRepository.findById(userId);

        if (query.isEmpty()) {
            throw new ApiException("User not found!", HttpStatus.NO_CONTENT);
        }

        User user = query.get();

        return new UserResponseDTO(user);
    }

    public UserResponseDTO getUserByEmail(String email) {
        Optional<User> query = userRepository.findByEmail(email);

        if (query.isEmpty()) {
            throw new ApiException("User not found!", HttpStatus.NO_CONTENT);
        }

        User user = query.get();

        return new UserResponseDTO(user);
    }

    public UserResponseDTO updateUser(UserUpdateDTO dto) throws IOException, java.io.IOException {
        Optional<User> query = userRepository.findById(dto.getUserId());
        if (query.isEmpty()) {
            throw new ApiException("User not found!", HttpStatus.NO_CONTENT);
        }
        User user = query.get();
        user.setUsername(dto.getUsername());

        MultipartFile file = dto.getProfilePhoto();
        if (file != null && !file.isEmpty()) {
            String filename = dto.getEmail() + "-profile";

            BlobClient blobClient = getBlobContainerClient().getBlobClient(filename);
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType("image/jpeg"));

            String blobUrl = String.format("%s/%s/%s", endpoint, containerName, filename);
            user.setProfilePhotoUrl(blobUrl);
        }

        userRepository.save(user);

        UserResponseDTO response = new UserResponseDTO(user);
        return response;
    }

    public UserResponseDTO validateAndReturnUser(UserLoginDTO userDto) {
        Optional<User> query = userRepository.findByEmail(userDto.getUsername());
        if (query.isEmpty()) {
            throw new ApiException("Username not found!", HttpStatus.NO_CONTENT);
        }
        User user = query.get();

        String userInDbPassword = user.getPassword();
        String userDtoPassword = userDto.getPassword();

        if (!passwordEncoder.matches(userDtoPassword, userInDbPassword)) {
            throw new ApiException("Password is not correct!", HttpStatus.NO_CONTENT);
        }

        return new UserResponseDTO(user);
    }

    public Collection<UserResponseDTO> getAll() {
        List<User> query = userRepository.findAll();

        List<UserResponseDTO> responseList = new ArrayList<>();

        query.forEach(user -> {
            responseList.add(new UserResponseDTO(user));
        });

        return responseList;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Optional<User> query = userRepository.findByEmail(username);

        if (query.isEmpty()) {
            throw new ApiException("User not found!", HttpStatus.NO_CONTENT);
        }

        return query.get();
    }

    public UserResponseDTO createUser(UserCreateDTO user) throws ApiException {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ApiException("Email is already used! Please use another email address", HttpStatus.BAD_REQUEST);
        }

        User userModel = new User();

        userModel.setUsername(user.getUsername());
        userModel.setEmail(user.getEmail());
        userModel.setPhoneNumber(user.getPhoneNumber());
        userModel.setDateOfBirth(user.getDateOfBirth());
        userModel.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(userModel);

        return new UserResponseDTO(userModel);
    }

    public void updateUserPassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> query = userRepository.findById(userId);
        if (query.isEmpty()) {
            throw new ApiException("Entity not found! User does not exist!", HttpStatus.BAD_REQUEST);
        }

        User user = query.get();
        String userPassword = user.getPassword();
        if (!passwordEncoder.matches(oldPassword, userPassword)) {
            throw new ApiException("Current password is not correct!", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void deleteUserById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ApiException("Entity not found! User does not exist!", HttpStatus.BAD_REQUEST);
        }

        userRepository.deleteById(userId);
    }
}