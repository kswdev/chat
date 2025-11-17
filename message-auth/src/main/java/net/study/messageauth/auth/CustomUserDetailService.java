package net.study.messageauth.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageauth.entity.user.UserEntity;
import net.study.messageauth.repository.UserRepository;
import net.study.messagecommon.auth.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username).orElseThrow(() -> {
            log.info("User not found: {}", username);
            return new UsernameNotFoundException("");
        });

        return new CustomUserDetails(userEntity.getUserId(), userEntity.getUsername(), userEntity.getPassword());
    }
}
