package com.example.demo.controller;

import com.example.demo.UserRepository;
import com.example.demo.dto.*;
import com.example.demo.repository.SentOtpRepository;
import com.example.demo.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/v1")
@Transactional
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SentOtpRepository sentOtpRepository;

    @Autowired
    private JwtTokenPayload jwtTokenPayload;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping(path = "loginWithMobile", consumes = "application/json")
    public ApiResponse loginWithMobile(@Valid @RequestBody LoginWithMobileDto request) {
        try {
            User oldUser = userRepository.findByMobileNo(request.getMobileNo());

            // Through error is user already exist
            if (oldUser == null) {
                return new ApiResponse(true, "USER_NOT_FOUND",
                        HttpStatus.PRECONDITION_FAILED, null);
            }

            // Through Error is user Account is deactivated
            if (!oldUser.getStatus().equals(User.UserStatus.ACTIVE)) {
                return new ApiResponse(true, "THIS_ACCOUNT_HAS_BEEN_DEACTIVATED",
                        HttpStatus.PRECONDITION_FAILED, null);
            }

            SentOtp lastSentOtp = sentOtpRepository.findTopBySentOnOrderByCreatedAtDesc(request.getMobileNo(), SentOtp.OtpType.LOGIN.toString());
            if (lastSentOtp == null) {
                return new ApiResponse(true, "PLEASE_SENT_OTP_FIRST_TO_REGISTER_WITH_MOBILE",
                        HttpStatus.PRECONDITION_FAILED, null);
            }

            // check if user otp is valid
            LocalDateTime localTime1 = lastSentOtp.getCreatedAt();
            LocalDateTime localTime2 = LocalDateTime.now();
            Duration duration = Duration.between(localTime1, localTime2);
            Integer minutesDifference = (int) duration.toMinutes();

            // Otp is only valid for 20 minutes
            if (minutesDifference > 20) {
                return new ApiResponse(true, "OTP_EXPIRED", HttpStatus.PRECONDITION_FAILED, null);
            }

            // Through error is otp did not matched
            if (!String.valueOf(lastSentOtp.getOtp()).equalsIgnoreCase(request.getOtp())) {
                return new ApiResponse(true, "INVALID_OTP", HttpStatus.PRECONDITION_FAILED, null);
            }

            // Create User Token
            final UserDetails userDetails = userDetailsService.loadUserByUsername(oldUser.getMobileNo());
            Map<String, Object> claims = new HashMap<>();
            Map<String, Object> res = new HashMap<>();

            jwtTokenPayload.setUserId(oldUser.getUuid());
            jwtTokenPayload.setMobileNo(oldUser.getMobileNo());
            jwtTokenPayload.setEmail(oldUser.getEmail());
            jwtTokenPayload.setUserType(oldUser.getUserType());

            claims.put("jti", jwtTokenPayload);

            final String token = jwtTokenUtil.generateToken(userDetails, claims, 6000000);
            res.put("token", token);
            res.put("userType", oldUser.getUserType());
            res.put("user_id", oldUser.getUuid());
            return new ApiResponse(false, "LOGIN_SUCCESSFULLY", HttpStatus.OK, res);

        } catch (Exception e) {
            log.error("Error :: {}", e.getLocalizedMessage());
            return new ApiResponse(true, e.getMessage(), HttpStatus.EXPECTATION_FAILED, null);
        }
    }

    @PostMapping(path = "loginWithEmail", consumes = "application/json")
    public ApiResponse loginWithEmail(@Valid @RequestBody LoginWithEmailDto request) {
        try {
            User oldUser = userRepository.findByEmail(request.getEmail());

            if (oldUser != null && oldUser.getStatus() == User.UserStatus.ACTIVE) {
                if (new BCryptPasswordEncoder().matches(request.getPassword(), oldUser.getPassword())) {

                    // Create user Token
                    final UserDetails userDetails = userDetailsService.loadUserByUsername(oldUser.getMobileNo());
                    Map<String, Object> claims = new HashMap<>();
                    Map<String, Object> res = new HashMap<>();

                    jwtTokenPayload.setUserId(oldUser.getUuid());
                    jwtTokenPayload.setMobileNo(oldUser.getMobileNo());
                    jwtTokenPayload.setEmail(oldUser.getEmail());
                    jwtTokenPayload.setUserType(oldUser.getUserType());

                    claims.put("jti", jwtTokenPayload);

                    final String token = jwtTokenUtil.generateToken(userDetails, claims, 6000000);
                    res.put("token", token);
                    res.put("userType", oldUser.getUserType());
                    res.put("user_id", oldUser.getUuid());
                    res.put("mobileNo", oldUser.getMobileNo());
                    return new ApiResponse(false, "LOGIN_SUCCESSFULLY", HttpStatus.OK, res);
                } else {
                    return new ApiResponse(true, "INVALID_CREDIENTIALS",
                            HttpStatus.UNAUTHORIZED, null);
                }

            } else {
                return new ApiResponse(true, "USER_NOT_FOUND",
                        HttpStatus.UNAUTHORIZED, null);
            }

        } catch (Exception e) {
            log.error("Error :: {}", e.getLocalizedMessage());
            return new ApiResponse(true, e.getMessage(), HttpStatus.EXPECTATION_FAILED, null);
        }
    }

    @PostMapping(path = "sendOtp")
    public ApiResponse sendOtp(@Valid @RequestBody SendOtpDto request, HttpServletRequest header) {
        try {
            log.info("sendOtp api called");
            String clientIp = getUserIP(header);
            User oldUser = userRepository.findByMobileNo(request.getMobileNo());
            //LOGIN OTP
            if (oldUser != null && request.getOtpType().equals(SentOtp.OtpType.LOGIN)) {
                SentOtp sentOtp = SentOtp.builder().otp(4444l).attempts(0).user(oldUser).sentOn(request.getMobileNo())
                        .IP(clientIp).otpType(request.getOtpType()).build();
                sentOtpRepository.save(sentOtp);
                return new ApiResponse(false, "OTP_SENT_CONFIRM", HttpStatus.OK, null);
            }
            //UPDATE MOBILE OTP
            if (oldUser == null && request.getOtpType().equals(SentOtp.OtpType.UPDATE_MOBILE)) {
                Optional<User> userOptional = userRepository.findById(Integer.valueOf(request.getUserId()));
                User existingUser = userOptional.get();
                SentOtp sentOtp = SentOtp.builder().otp(4444l).attempts(0).user(existingUser)
                        .sentOn(request.getMobileNo())
                        .IP(clientIp).otpType(request.getOtpType()).build();
                sentOtpRepository.save(sentOtp);
                return new ApiResponse(false, "OTP_SENT_CONFIRM", HttpStatus.OK, null);
            } else if (oldUser != null && request.getOtpType().equals(SentOtp.OtpType.UPDATE_MOBILE)) {
                return new ApiResponse(true, "MOBILE_NO_IS_ALREADY_REGISTERED",
                        HttpStatus.PRECONDITION_FAILED, null);
            }
            //REGISTRATION OTP
            if (oldUser == null && request.getOtpType().equals(SentOtp.OtpType.NEW_REGISTRATION)) {
                SentOtp sentOtp = SentOtp.builder().otp(4444l).attempts(0).IP(clientIp)
                        .sentOn(request.getMobileNo()).IP(clientIp)
                        .otpType(request.getOtpType()).build();
                sentOtpRepository.save(sentOtp);
                return new ApiResponse(false, "OTP_SENT_CONFIRM", HttpStatus.OK, null);
            } else if (oldUser != null && request.getOtpType().equals(SentOtp.OtpType.NEW_REGISTRATION)) {
                return new ApiResponse(true, "MOBILE_NO_IS_ALREADY_REGISTERED",
                        HttpStatus.PRECONDITION_FAILED, null);
            } 
            //UPDATE EMAIL OTP
            if (request.getOtpType().equals(SentOtp.OtpType.UPDATE_EMAIL)) {
                User checkUser = userRepository.findByEmail(request.getEmail());
                if (checkUser != null) {
                    return new ApiResponse(false, "EMAIL_IS_ALREADY_REGISTERED", HttpStatus.INTERNAL_SERVER_ERROR, null);
                } else {
                    // String subject = "My24X7Doctor e-mail verification otp";
                    // String emailBody = AppUtils.fetchOtpTemplate();
                    // emailBody = emailBody.replace("[$EMAIL_OTP$]", "4444");
                    // EmailDto emailDto = new EmailDto(request.getEmail(), emailBody, subject);
                    // emailService.sendEmail(emailDto);
                    SentOtp sentOtp = SentOtp.builder().otp(4444l).attempts(0).user(oldUser).sentOn(request.getEmail())
                            .IP(clientIp).otpType(request.getOtpType()).build();
                    sentOtpRepository.save(sentOtp);
                    return new ApiResponse(false, "OTP_SENT_CONFIRM", HttpStatus.OK, null);
                }
            }
            return new ApiResponse(false, "OTP_SENT_CONFIRM", HttpStatus.OK, null);
        } catch (Exception e) {
            log.error("Error :: {}", e.getLocalizedMessage());
            return new ApiResponse(true, e.getMessage(), HttpStatus.EXPECTATION_FAILED, null);
        }
    }
    public static String getUserIP(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        String unknown = "unknown";

        if (ipAddress == null || ipAddress.isEmpty() || unknown.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || unknown.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || unknown.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }


    @PostMapping(path = "registrationWithEmail")
    public ApiResponse registrationWithEmail(@Valid @RequestBody RegisterWithEmailDto request) {
        try {
            log.info("registrationWithEmail api called");
            User checkUserByEmail = userRepository.findByEmail(request.getEmail());
            if (checkUserByEmail != null) {
                return new ApiResponse(true, "EMAIL_IS_ALREADY_REGISTERED",
                        HttpStatus.PRECONDITION_FAILED, null);
            }

            User checkUserByMobile = userRepository.findByMobileNo(request.getMobileNo());
            if (checkUserByMobile != null) {
                return new ApiResponse(true, "MOBILE_NO_IS_ALREADY_REGISTERED",
                        HttpStatus.PRECONDITION_FAILED, null);
            }

            if (!request.getPassword().equals(request.getComfirmPassword())) {
                return new ApiResponse(true, "CONFIRM_PASSWORD_DID_NOT_MATCH",
                        HttpStatus.PRECONDITION_FAILED, null);
            }

            User user = new User();
            String userId = String.valueOf(UUID.randomUUID());

                if (request.getName() == null) {
                    return new ApiResponse(true, "NAME_SHOULD_NOT_BE_BLANK",
                            HttpStatus.PRECONDITION_FAILED,
                            null);
                }
                user.setName(request.getName());

            user.setUuid(userId);
            user.setMobileNo(request.getMobileNo());
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setStatus(User.UserStatus.ACTIVE);
            user.setPassword(new BCryptPasswordEncoder().encode(request.getPassword()));
            userRepository.save(user);
            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getMobileNo());
            Map<String, Object> claims = new HashMap<>();
            Map<String, Object> res = new HashMap<>();

            jwtTokenPayload.setUserId(user.getUuid());
            jwtTokenPayload.setMobileNo(user.getMobileNo());
            jwtTokenPayload.setEmail(user.getEmail());
            jwtTokenPayload.setUserType(user.getUserType());

            claims.put("jti", jwtTokenPayload);

            final String token = jwtTokenUtil.generateToken(userDetails, claims, 6000000);
            res.put("token", token);
            res.put("userType", user.getUserType());
            res.put("user_id", user.getUuid());
            res.put("mobileNo", user.getMobileNo());

            return new ApiResponse(false, "REGISTRATION_SUCCESSFULLY", HttpStatus.OK, res);

        } catch (Exception e) {
            log.error("Error :: {}", e.getLocalizedMessage());
            return new ApiResponse(true, e.getMessage(), HttpStatus.EXPECTATION_FAILED, null);
        }
    }
}
