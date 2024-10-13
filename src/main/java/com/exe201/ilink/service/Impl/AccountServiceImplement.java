package com.exe201.ilink.service.Impl;

import com.exe201.ilink.Util.AccountSpecification;
import com.exe201.ilink.config.converter.GenericConverter;
import com.exe201.ilink.model.entity.Account;
import com.exe201.ilink.model.entity.Role;
import com.exe201.ilink.model.entity.Shop;
import com.exe201.ilink.model.enums.ProductSort;
import com.exe201.ilink.model.enums.RoleName;
import com.exe201.ilink.model.exception.ILinkException;
import com.exe201.ilink.model.payload.request.AccountProfile;
import com.exe201.ilink.model.payload.request.ChangePasswordRequest;
import com.exe201.ilink.model.payload.response.AccountInfoResponse;
import com.exe201.ilink.model.payload.response.ListAccountInfo;
import com.exe201.ilink.model.payload.response.UpdateAccountResponse;
import com.exe201.ilink.repository.AccountRepository;
import com.exe201.ilink.repository.RoleRepository;
import com.exe201.ilink.repository.ShopRepository;
import com.exe201.ilink.sercurity.JwtTokenProvider;
import com.exe201.ilink.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImplement implements AccountService {

    private final AccountRepository accountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final GenericConverter<Account> genericConverter;
    private final ShopRepository shopRepository;
    private final RoleRepository roleRepository;

    @Override
    public Account getCurrentAccountInfo(HttpServletRequest request) {

        String token = extractTokenFormJWT(request);

        //Extract Account Info
        String userEmail = jwtTokenProvider.getUsername(token);
        Account account = accountRepository.findByEmail(userEmail)
            .orElse(null);

        if (account == null && !jwtTokenProvider.validateToken(token)) {
            throw new ILinkException(HttpStatus.BAD_REQUEST, "No account found with this token");
        }

        if (!jwtTokenProvider.isTokenValid(token, account.getEmail())) {
            throw new ILinkException(HttpStatus.UNAUTHORIZED, "Token is invalid or is expired");
        }

        return account;
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest, HttpServletRequest request) {
        Account account = getCurrentAccountInfo(request);

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), account.getPassword())) {
            throw new ILinkException(HttpStatus.BAD_REQUEST, "Your old password is incorrect");
        }
        account.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        accountRepository.save(account);
    }

    @Override
    public void updateAccountProfilePicture(UUID id, String imageURLMain) {
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "No account found with this id"));

        if (imageURLMain == null) {
            throw new ILinkException(HttpStatus.BAD_REQUEST, "Image URL is null");
        }
        account.setAvatar(imageURLMain);
        accountRepository.save(account);

    }

    @Override
    public void updateAccountInfo(UUID id, AccountProfile accountProfile) {

        if (accountProfile == null) {
            throw new ILinkException(HttpStatus.BAD_REQUEST, "Account profile is null");
        }

        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Account not exists or not found "));

        account.setFirstName(accountProfile.getFirstName());
        account.setLastName(accountProfile.getLastName());
        account.setPhone(accountProfile.getPhone());
        account.setGender(accountProfile.getGender());
        account.setDob(accountProfile.getDob());
        accountRepository.save(account);


    }

    @Override
    public ListAccountInfo getAllAccount(int pageNo, int pageSize, ProductSort sortBy, String keyword, String role) {
        Sort sort = Sort.by(sortBy.getDirection(), sortBy.getField());
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Specification<Account> spec = Specification.where(AccountSpecification.notAdmin().and(AccountSpecification.hasRole(role.toUpperCase())))
            .and(
                Specification.where(AccountSpecification.hasEmail(keyword))
                    .or(AccountSpecification.hasPhone(keyword))
                    .or(AccountSpecification.hasName(keyword))
            );

        Page<Account> accounts = accountRepository.findAll(spec, pageable);
        List<Account> accountList = accounts.getContent();
        List<AccountInfoResponse> accountInfoResponse = new ArrayList<>();

        accountList.stream().forEach(account -> {
            AccountInfoResponse accountInfo = this.getAccountByAdmin(account.getAccountId());
            accountInfoResponse.add(accountInfo);
        });

        return ListAccountInfo.builder()
            .content(accountInfoResponse)
            .totalPages(accounts.getTotalPages())
            .totalElements(accounts.getTotalElements())
            .pageSize(accounts.getSize())
            .pageNo(accounts.getNumber())
            .last(accounts.isLast())
            .build();
    }

    @Override
    public AccountInfoResponse getAccountByAdmin(UUID accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "No account found with this id"));

        AccountInfoResponse accountInfoResponse = modelMapper.map(account, AccountInfoResponse.class);

        if (RoleName.contains(accountInfoResponse.getRoleName()) && accountInfoResponse.getRoleName().equals(RoleName.SELLER.getRoleName())){
            Shop shop = shopRepository.findByAccountId(accountInfoResponse.getAccountId())
                .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "No shop found with this account"));
            accountInfoResponse.setShopId(shop.getShopId());
            accountInfoResponse.setShopName(shop.getShopName());
        }

        return accountInfoResponse;
    }

    @Override
    public void editAccountByAdmin(UUID accountId, UpdateAccountResponse updateAccountResponse) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "No account found with this id"));

        Role role = roleRepository.findById(updateAccountResponse.getRoleId())
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "No role found with this name"));
        account.setRole(role);
        account.setFirstName(updateAccountResponse.getFirstName());
        account.setLastName(updateAccountResponse.getLastName());
        account.setAddress(updateAccountResponse.getAddress());
        account.setGender(updateAccountResponse.getGender());
        account.setDob(updateAccountResponse.getDob());
        account.setPhone(updateAccountResponse.getPhone());
        account.setAvatar(updateAccountResponse.getAvatar());
        account.setLocked(updateAccountResponse.isLocked());
        account.setEnable(updateAccountResponse.isEnable());

        accountRepository.save(account);
    }

    private String extractTokenFormJWT(HttpServletRequest request) {
        //Extract Token From Header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token == null) {
            throw new ILinkException(HttpStatus.UNAUTHORIZED, "No JWT found in request header");
        }
        return token;
    }
}
