package umastar.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umastar.entity.User;
import umastar.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Nap thong tin nguoi dung cho Spring Security va JwtAuthenticationFilter.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return UserPrincipal.from(user);
    }

    @Transactional(readOnly = true)
    public Optional<UserPrincipal> findById(UUID id) {
        return userRepository.findById(id).map(UserPrincipal::from);
    }
}

