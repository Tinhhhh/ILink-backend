package com.exe201.ilink.sercurity;

import com.exe201.ilink.model.entity.Account;
import com.exe201.ilink.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
@Service
@RequiredArgsConstructor
public class CustomeUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

        @Override
        public UserDetails loadUserByUsername(String Email) throws UsernameNotFoundException {
            Account account = accountRepository.findByEmail(Email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + Email));

            Set<GrantedAuthority> authorities = (Set<GrantedAuthority>) account.getAuthorities();

            return new org.springframework.security.core.userdetails.User(account.getEmail(),
                    account.getPassword(),
                    authorities);
        }

}
