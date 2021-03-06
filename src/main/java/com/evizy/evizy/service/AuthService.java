package com.evizy.evizy.service;

import com.evizy.evizy.constant.ResponseMessage;
import com.evizy.evizy.domain.dao.Users;
import com.evizy.evizy.domain.dto.CitizenResponse;
import com.evizy.evizy.domain.dto.TokenResponse;
import com.evizy.evizy.domain.dto.UsersRequest;
import com.evizy.evizy.errors.BusinessFlowException;
import com.evizy.evizy.repository.AdminRepository;
import com.evizy.evizy.repository.UsersRepository;
import com.evizy.evizy.config.JwtTokenProvider;
import com.evizy.evizy.util.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsersRepository usersRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    private final UsersService usersService;

    private final AdminRepository adminRepository;

    public UserDetails getInfoByPrincipal(String str) throws BusinessFlowException {
        UserDetails ret;
        if (str.startsWith("admin_")) {
            String username = str.split("admin_", 2)[1];
            ret = adminRepository.getDistinctTopByUsername(username);
        } else {
            String username = str.split("user_", 2)[1];
            ret = usersRepository.getDistinctTopByNik(username);
        }
        if (ret == null)
            throw new BusinessFlowException(HttpStatus.UNAUTHORIZED, ResponseMessage.UNAUTHORIZED, "Info not found!");
        return ret;
    }

    public Users register(UsersRequest usersRequest) throws BusinessFlowException {
        Users exists = usersRepository.getDistinctTopByNik(usersRequest.getNik());
        if (exists != null) {
            throw new BusinessFlowException(HttpStatus.BAD_REQUEST, ResponseMessage.ALREADY_EXIST, "User already exist!");
        }

        List<CitizenResponse> citizens = usersService.getAllCitizen();

        CitizenResponse citizen = null;
        for (CitizenResponse e : citizens) {
            if (e.getNik().equalsIgnoreCase(usersRequest.getNik()))
                citizen = e;
        }

        if (citizen == null) {
            log.error("Nik not found!");
            throw new BusinessFlowException(HttpStatus.BAD_REQUEST, ResponseMessage.INVALID_NIK, "Nik not found!");
        }

        Users users = Users.builder()
                .nik(usersRequest.getNik())
                .name(usersRequest.getName())
                .phoneNumber(usersRequest.getPhoneNumber())
                .gender(citizen.getGender())
                .dateOfBirth(citizen.getDateOfBirth())
                .active(true)
                .password(passwordEncoder.encode(usersRequest.getPassword())).build();
        return usersRepository.save(users);
    }

    public ResponseEntity<?> authenticatedAndGenerateToken(UsersRequest usersRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        usersRequest.getNik(),
                        usersRequest.getPassword()
                )
        );
        String jwt = jwtTokenProvider.generateToken(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return Response.build(
                ResponseMessage.SUCCESS,
                HttpStatus.OK,
                TokenResponse.builder().accessToken(jwt).build()
        );
    }
}
