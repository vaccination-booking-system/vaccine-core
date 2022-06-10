package com.evizy.evizy.service;

import com.evizy.evizy.constant.ResponseMessage;
import com.evizy.evizy.domain.dao.Users;
import com.evizy.evizy.domain.dto.UsersRequest;
import com.evizy.evizy.errors.BusinessFlowException;
import com.evizy.evizy.repository.UsersRepository;
import com.evizy.evizy.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UsersService implements UserDetailsService {
    @Autowired
    private UsersRepository usersRepository;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = usersRepository.getDistinctTopByNik(username);
        if (user == null)
            throw new UsernameNotFoundException("Username not found");
        return user;
    }

    public UsersRequest find(Long id) {
        Optional<Users> optionalUsers = usersRepository.findById(id);
        if (optionalUsers.isEmpty())
            throw new BusinessFlowException(HttpStatus.BAD_REQUEST, ResponseMessage.BAD_REQUEST, "User not found!");
        Users user = optionalUsers.get();
        return UsersRequest
                .builder()
                .id(user.getId())
                .nik(user.getNik())
                .name(user.getName())
                .build();
    }

    public List<UsersRequest> find() {
        List<Users> usersList = usersRepository.findAll();
        List<UsersRequest> usersRequests = new ArrayList<>();
        for(Users user : usersList) {
            usersRequests.add(UsersRequest
                    .builder()
                    .id(user.getId())
                    .nik(user.getNik())
                    .name(user.getName())
                    .build());
        }
        return usersRequests;
    }

    public void deleteUser(Long id) {
        Optional<Users> optionalUsers = usersRepository.findById(id);
        if (optionalUsers.isEmpty())
            throw new BusinessFlowException(HttpStatus.BAD_REQUEST, ResponseMessage.BAD_REQUEST, "User not found!");
        usersRepository.delete(optionalUsers.get());
    }
}