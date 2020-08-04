package org.kitchen.booting.service;

import jdk.nashorn.internal.objects.annotations.Constructor;
import org.kitchen.booting.domain.Profile;
import org.kitchen.booting.domain.UserRegistrationDTO;
import org.kitchen.booting.domain.userauth.EmailVerificationToken;
import org.kitchen.booting.domain.userauth.Role;
import org.kitchen.booting.domain.userauth.User;
import org.kitchen.booting.exception.ResourceNotFoundException;
import org.kitchen.booting.repository.ProfileRepository;
import org.kitchen.booting.repository.userauth.RoleRepository;
import org.kitchen.booting.repository.userauth.UserRepository;
import org.kitchen.booting.service.userauth.EmailVerificationTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenService emailVerificationTokenService;

    private Role userRole;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       ProfileRepository profileRepository, PasswordEncoder passwordEncoder, EmailVerificationTokenService emailVerificationTokenService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationTokenService = emailVerificationTokenService;
    }

    @PostConstruct
    public void init() {
        userRole = roleRepository.findByName("ROLE_USER");
    }

    public boolean isValidNewUserId(String userId) {
        return userRepository.findById(userId).isPresent()?false:true;
    }

    public boolean isvalidNewEmail(String email) {
        return userRepository.findByEmail(email)==null?true:false;
    }

    public Optional<User> registerNewUser(UserRegistrationDTO userRegistrationDTO) {
        if(isValidNewUserId(userRegistrationDTO.getUserId()) && isvalidNewEmail(userRegistrationDTO.getEmail())) {
            User newUser = userRegistrationDTO.getUser();
            logger.info("@@@@"+userRole);
            newUser.setRole(userRole);
            newUser.setCreatedAt(new Date());
            newUser.setProfile(userRegistrationDTO.getProfile());
            newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
            User user = userRepository.save(newUser);
            return Optional.of(user);
//            profileRepository.save(userRegistrationDTO.getProfile());
        }
        return Optional.empty();
    }
    public User save(User user) {
        userRepository.save(user);
        return user;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public void delete(String userId) {
        userRepository.deleteByUserId(userId);
    }


    private final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private java.sql.Timestamp parseTimestamp(String timestamp) {
        try {
            return new Timestamp(DATE_TIME_FORMAT.parse(timestamp).getTime());
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Optional<User> confirmEmailRegistration(String emailToken) {
        EmailVerificationToken emailVerificationToken = emailVerificationTokenService.findByToken(emailToken)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "Email verification", emailToken));

        User registeredUser = emailVerificationToken.getUser();
        if (registeredUser.getEnabled()) {
            logger.info("User [" + emailToken + "] already registered.");
            return Optional.of(registeredUser);
        }

        emailVerificationTokenService.verifyExpiration(emailVerificationToken);
        emailVerificationToken.setConfirmedStatus();
        emailVerificationTokenService.save(emailVerificationToken);

        registeredUser.markVerificationConfirmed();
        this.save(registeredUser);
        return Optional.of(registeredUser);
    }

    public Optional<EmailVerificationToken> recreateRegistrationToken(String existingToken) {
        EmailVerificationToken emailVerificationToken = emailVerificationTokenService.findByToken(existingToken)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "Existing email verification", existingToken));

        if (emailVerificationToken.getUser().getEnabled()) {
            return Optional.empty();
        }
        return Optional.ofNullable(emailVerificationTokenService.updateExistingTokenWithNameAndExpiry(emailVerificationToken));
    }

}

