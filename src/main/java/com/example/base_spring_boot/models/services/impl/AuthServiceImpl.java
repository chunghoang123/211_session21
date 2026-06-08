package com.example.base_spring_boot.models.services.impl;

import com.example.base_spring_boot.exceptions.HttpBadRequestException;
import com.example.base_spring_boot.models.constants.RoleName;
import com.example.base_spring_boot.models.dtos.req.LoginReq;
import com.example.base_spring_boot.models.dtos.req.RegisterReq;
import com.example.base_spring_boot.models.dtos.req.TokenRefreshRequest;
import com.example.base_spring_boot.models.dtos.res.JwtRes;
import com.example.base_spring_boot.models.dtos.res.TokenRefreshResponse;
import com.example.base_spring_boot.models.entities.RefreshToken;
import com.example.base_spring_boot.models.entities.Role;
import com.example.base_spring_boot.models.entities.User;
import com.example.base_spring_boot.models.repositories.IUserRepository;
import com.example.base_spring_boot.models.services.IAuthService;
import com.example.base_spring_boot.models.services.IRefreshTokenService;
import com.example.base_spring_boot.models.services.IRoleService;
import com.example.base_spring_boot.security.jwt.JwtUtils;
import com.example.base_spring_boot.security.principal.MyUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService
{
    private final IRoleService roleService;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final IRefreshTokenService refreshTokenService;

    @Override
    public void register(RegisterReq req)
    {
        Set<Role> roles = new HashSet<>();
        roles.add(roleService.findByRoleName(RoleName.ROLE_USER));
        User user = User.builder()
                .fullName(req.getFullName())
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(roles)
                .build();
        userRepository.save(user);
    }

    @Override
    public JwtRes login(LoginReq req)
    {
        Authentication authentication;
        try
        {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        }
        catch (AuthenticationException e)
        {
            throw new HttpBadRequestException("Username or password is incorrect");
        }

        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUser().getId());

        return JwtRes.builder()
                .fullName(userDetails.getUser().getFullName())
                .accessToken(jwtUtils.generateToken(userDetails.getUsername()))
                .refreshToken(refreshToken.getToken())
                .roles(userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()))
                .build();
    }

    @Override
    public TokenRefreshResponse refreshToken(TokenRefreshRequest req)
    {
        RefreshToken refreshToken = refreshTokenService.findByToken(req.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new HttpBadRequestException("Refresh token is not found"));

        String accessToken = jwtUtils.generateToken(refreshToken.getUser().getUsername());
        return TokenRefreshResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Override
    public void logout()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof MyUserDetails userDetails))
        {
            throw new HttpBadRequestException("User is not authenticated");
        }

        refreshTokenService.revokeAllByUserId(userDetails.getUser().getId());
    }
}
