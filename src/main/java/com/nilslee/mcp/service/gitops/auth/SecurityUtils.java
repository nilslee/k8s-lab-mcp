package com.nilslee.mcp.service.gitops.auth;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/** Generates random passwords for lab use; {@link SecureRandom} is thread-safe. */
@Service
public class SecurityUtils {
  // Define the character pool
  private static final String ALPHA_CAPS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String ALPHA = "abcdefghijklmnopqrstuvwxyz";
  private static final String NUMERIC = "0123456789";
  private static final String SPECIAL_CHARS = "!@#$%^&*_=+-/";
  private static final String DATA_FOR_RANDOM = ALPHA_CAPS + ALPHA + NUMERIC + SPECIAL_CHARS;

  // Use a single instance of SecureRandom (it is thread-safe)
  private static final SecureRandom random = new SecureRandom();

  public String generatePassword(int len) {
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      // Get a random index from the pool
      int rndCharAt = random.nextInt(DATA_FOR_RANDOM.length());
      char rndChar = DATA_FOR_RANDOM.charAt(rndCharAt);
      sb.append(rndChar);
    }
    return sb.toString();
  }
}