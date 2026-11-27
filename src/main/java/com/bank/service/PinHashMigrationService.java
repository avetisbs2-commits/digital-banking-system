package com.bank.service;

import com.bank.model.AccountEntity;
import com.bank.repository.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
public class PinHashMigrationService {

    private static final Pattern LEGACY_PIN_FORMAT = Pattern.compile("^\\d{4}$");
    private static final Pattern BCRYPT_FORMAT =
            Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    private final AccountRepository accountRepository;
    private final PasswordEncoder pinPasswordEncoder;

    public PinHashMigrationService(
            AccountRepository accountRepository,
            PasswordEncoder pinPasswordEncoder
    ) {
        this.accountRepository = accountRepository;
        this.pinPasswordEncoder = pinPasswordEncoder;
    }

    @Transactional
    public int hashLegacyPins() {
        int migratedCount = 0;

        for (AccountEntity account : accountRepository.findAll()) {
            String storedPinValue = account.getPinHash();

            if (storedPinValue != null && BCRYPT_FORMAT.matcher(storedPinValue).matches()) {
                continue;
            }

            if (storedPinValue == null || !LEGACY_PIN_FORMAT.matcher(storedPinValue).matches()) {
                throw new IllegalStateException(
                        "PIN migration stopped because an account has an unsupported PIN value. No PIN values were changed."
                );
            }

            account.setPinHash(pinPasswordEncoder.encode(storedPinValue));
            accountRepository.save(account);
            migratedCount++;
        }

        return migratedCount;
    }
}