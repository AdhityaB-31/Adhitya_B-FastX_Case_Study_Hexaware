package com.hexaware.fastx.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hexaware.fastx.entity.BusOperator;
import com.hexaware.fastx.entity.User;
import com.hexaware.fastx.repository.BusOperatorRepository;
import com.hexaware.fastx.repository.UserRepository;

@Service
public class FastXUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusOperatorRepository busOperatorRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getIsActive() == null || !user.getIsActive()) {
                throw new UsernameNotFoundException("Your ID is deactivated. Contact admin for further details: admin@fastx.com");
            }

            String roleName = (user.getRole() != null) ? user.getRole().getRoleName() : "USER";

            return new FastXUserDetails(user.getEmail(), user.getPassword(), roleName, user.getUserId(), user.getFullName());
        }

        Optional<BusOperator> operatorOpt = busOperatorRepository.findByEmail(email);
        if (operatorOpt.isPresent()) {
            BusOperator operator = operatorOpt.get();
            if (operator.getIsActive() == null || !operator.getIsActive()) {
                throw new UsernameNotFoundException("Your ID is deactivated. Contact admin for further details: admin@fastx.com");
            }
            return new FastXUserDetails(
                    operator.getEmail(),
                    operator.getPassword(),
                    "BUS_OPERATOR",
                    operator.getOperatorId(),
                    operator.getCompanyName()
            );
        }

        throw new UsernameNotFoundException("No account found with email: " + email);
    }
}
