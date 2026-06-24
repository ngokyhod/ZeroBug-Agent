package com.zerobug_agent.controller;

import com.zerobug_agent.dto.ForgotPasswordRequest;
import com.zerobug_agent.dto.LoginRequest;
import com.zerobug_agent.dto.RegisterRequest;
import com.zerobug_agent.dto.ResetPasswordRequest;
import com.zerobug_agent.dto.UserDto;
import com.zerobug_agent.entity.User;
import com.zerobug_agent.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public AuthApiController(AuthenticationManager authenticationManager, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().trim().toLowerCase(),
                            request.getPassword()));

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            User user = userService.findByEmail(request.getEmail());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "user", UserDto.from(user)));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "Email hoặc mật khẩu không đúng"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đăng ký thành công! Vui lòng đăng nhập.",
                    "user", UserDto.from(user)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã đăng xuất"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(UserDto.from(user));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        // Luôn trả về thành công để tránh lộ thông tin email có tồn tại trong hệ thống hay không.
        userService.requestPasswordReset(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Nếu email tồn tại, link đặt lại mật khẩu đã được gửi (hoặc in ra log server ở chế độ dev)."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            userService.resetPassword(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
