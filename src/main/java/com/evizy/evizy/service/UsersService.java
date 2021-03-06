package com.evizy.evizy.service;

import com.evizy.evizy.constant.Endpoints;
import com.evizy.evizy.constant.ResponseMessage;
import com.evizy.evizy.domain.dao.Users;
import com.evizy.evizy.domain.dto.CitizenResponse;
import com.evizy.evizy.domain.dto.UsersRequest;
import com.evizy.evizy.errors.BusinessFlowException;
import com.evizy.evizy.repository.AdminRepository;
import com.evizy.evizy.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UsersService implements UserDetailsService {
    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private RestTemplate restTemplate;

    public UserDetails loadUserByUsername(String str) throws UsernameNotFoundException {
        UserDetails ret;
        if (str.startsWith("admin_")) {
            String username = str.split("admin_", 2)[1];
            ret = adminRepository.getDistinctTopByUsername(username);
        } else {
            String username = str.split("user_", 2)[1];
            ret = usersRepository.getDistinctTopByNik(username);
        }
        if (ret == null)
            throw new UsernameNotFoundException("Credentials not found");
        return ret;
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
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
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
                    .phoneNumber(user.getPhoneNumber())
                    .gender(user.getGender())
                    .dateOfBirth(user.getDateOfBirth())
                    .build());
        }
        return usersRequests;
    }

    public List<CitizenResponse> getAllCitizen() {
        String url = Endpoints.API_CITIZEN_BASE_URL + "/api/v1/citizen";
        CitizenResponse[] lists = restTemplate.getForObject(url, CitizenResponse[].class);

        return Arrays.asList(lists);
    }
}
