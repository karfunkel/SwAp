package org.aklein.swap.security;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class CryptoConfiguration {
    private String encryptedPrefix = "ENCRYPTED";

    private char asymetricKeyDelimiter = '|';

    private String symetricKeyAlgorithm = "AES";

    private String symetricKeyProvider = null;

    private int symetricKeySize = 128;

    private String symetricKeyExternalizePattern = "xx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx";

    private String randomAlgorithm = "SHA1PRNG";

    private String randomProvider = null;

    private String asymetricKeyAlgorithm = "RSA";

    private String asymetricKeyProvider = null;

    private int asymetricKeySize = 512;

    private String encryptedKeyDigestAlgorithm = "MD5";

    private String encryptedKeyDigestProvider = null;

    private String encryptedKeyAlgorithm = "AES";

    private String encryptedKeyProvider = null;

    private int privateKeySize = 128;

    private int interactiveEncryptionMaxTries = 3;

    private List<Key> keys = new ArrayList<Key>();

    private String integrityDigestAlgorithm = "MD5";

    private String integrityDigestProvider = null;

    private Class<? extends KeyWallet> walletType = BasicKeyWallet.class;

    private boolean integrityResultWithoutDigest = false;

    private boolean useSaltWithPBE = false;

    private int saltSize = -51;

    private int saltIterations = 3;

    private List<String> integrityDirExcludes = new ArrayList<String>();

    private String digestFileName = ".digest";

    public String getEncryptedPrefix() {
        return encryptedPrefix;
    }

    public void setEncryptedPrefix(String encryptedPrefix) {
        this.encryptedPrefix = encryptedPrefix;
    }

    public char getAsymetricKeyDelimiter() {
        return asymetricKeyDelimiter;
    }

    public void setAsymetricKeyDelimiter(char asymetricKeyDelimiter) {
        this.asymetricKeyDelimiter = asymetricKeyDelimiter;
    }

    public String getSymetricKeyAlgorithm() {
        return symetricKeyAlgorithm;
    }

    public void setSymetricKeyAlgorithm(String symetricKeyAlgorithm) {
        this.symetricKeyAlgorithm = symetricKeyAlgorithm;
    }

    public String getSymetricKeyProvider() {
        return symetricKeyProvider;
    }

    public void setSymetricKeyProvider(String symetricKeyProvider) {
        this.symetricKeyProvider = symetricKeyProvider;
    }

    public int getSymetricKeySize() {
        return symetricKeySize;
    }

    public void setSymetricKeySize(int symetricKeySize) {
        this.symetricKeySize = symetricKeySize;
    }

    public String getSymetricKeyExternalizePattern() {
        return symetricKeyExternalizePattern;
    }

    public void setSymetricKeyExternalizePattern(String symetricKeyExternalizePattern) {
        this.symetricKeyExternalizePattern = symetricKeyExternalizePattern;
    }

    public String getRandomAlgorithm() {
        return randomAlgorithm;
    }

    public void setRandomAlgorithm(String randomAlgorithm) {
        this.randomAlgorithm = randomAlgorithm;
    }

    public String getRandomProvider() {
        return randomProvider;
    }

    public void setRandomProvider(String randomProvider) {
        this.randomProvider = randomProvider;
    }

    public String getAsymetricKeyAlgorithm() {
        return asymetricKeyAlgorithm;
    }

    public void setAsymetricKeyAlgorithm(String asymetricKeyAlgorithm) {
        this.asymetricKeyAlgorithm = asymetricKeyAlgorithm;
    }

    public String getAsymetricKeyProvider() {
        return asymetricKeyProvider;
    }

    public void setAsymetricKeyProvider(String asymetricKeyProvider) {
        this.asymetricKeyProvider = asymetricKeyProvider;
    }

    public int getAsymetricKeySize() {
        return asymetricKeySize;
    }

    public void setAsymetricKeySize(int asymetricKeySize) {
        this.asymetricKeySize = asymetricKeySize;
    }

    public String getEncryptedKeyDigestAlgorithm() {
        return encryptedKeyDigestAlgorithm;
    }

    public void setEncryptedKeyDigestAlgorithm(String encryptedKeyDigestAlgorithm) {
        this.encryptedKeyDigestAlgorithm = encryptedKeyDigestAlgorithm;
    }

    public String getEncryptedKeyDigestProvider() {
        return encryptedKeyDigestProvider;
    }

    public void setEncryptedKeyDigestProvider(String encryptedKeyDigestProvider) {
        this.encryptedKeyDigestProvider = encryptedKeyDigestProvider;
    }

    public String getEncryptedKeyAlgorithm() {
        return encryptedKeyAlgorithm;
    }

    public void setEncryptedKeyAlgorithm(String encryptedKeyAlgorithm) {
        this.encryptedKeyAlgorithm = encryptedKeyAlgorithm;
    }

    public String getEncryptedKeyProvider() {
        return encryptedKeyProvider;
    }

    public void setEncryptedKeyProvider(String encryptedKeyProvider) {
        this.encryptedKeyProvider = encryptedKeyProvider;
    }

    public int getPrivateKeySize() {
        return privateKeySize;
    }

    public void setPrivateKeySize(int privateKeySize) {
        this.privateKeySize = privateKeySize;
    }

    public int getInteractiveEncryptionMaxTries() {
        return interactiveEncryptionMaxTries;
    }

    public void setInteractiveEncryptionMaxTries(int interactiveEncryptionMaxTries) {
        this.interactiveEncryptionMaxTries = interactiveEncryptionMaxTries;
    }

    public List<Key> getKeys() {
        return keys;
    }

    public void addKey(Key key) {
        keys.add(key);
    }

    public void removeKey(Key key) {
        keys.remove(key);
    }

    public boolean containsKey(Key key) {
        return keys.contains(key);
    }

    public int getKeyCount() {
        return keys.size();
    }

    public Key getDefaultEncryptionKey() {
        return keys.size() > 0 ? keys.get(0) : null;
    }

    public List<Key> getKeysForDecoding() {
        return keys;
    }

    public void setKeys(List<Key> keys) {
        this.keys = keys;
    }

    public String getKeyDecryptionPassword(String externalizedKey) {
        return null;
    }

    public String getKeyEncryptionPassword(Key key) {
        return null;
    }

    public String getIntegrityDigestAlgorithm() {
        return integrityDigestAlgorithm;
    }

    public void setIntegrityDigestAlgorithm(String integrityDigestAlgorithm) {
        this.integrityDigestAlgorithm = integrityDigestAlgorithm;
    }

    public String getIntegrityDigestProvider() {
        return integrityDigestProvider;
    }

    public void setIntegrityDigestProvider(String integrityDigestProvider) {
        this.integrityDigestProvider = integrityDigestProvider;
    }

    public Class<? extends KeyWallet> getWalletType() {
        return walletType;
    }

    public void setWalletType(Class<? extends KeyWallet> walletType) {
        this.walletType = walletType;
    }

    /**
     * ignore IntegrityCheck for directories without digest file
     * @return the integrityResultWithoutDigest
     */
    public boolean isIntegrityResultWithoutDigest() {
        return integrityResultWithoutDigest;
    }

    /**
     * @param integrityResultWithoutDigest ignore IntegrityCheck for directories
     *            without digest file
     */
    public void setIntegrityResultWithoutDigest(boolean integrityResultWithoutDigest) {
        this.integrityResultWithoutDigest = integrityResultWithoutDigest;
    }

    /**
     * @return the useSaltWithPBE
     */
    public boolean isUseSaltWithPBE() {
        return useSaltWithPBE;
    }

    /**
     * @param useSaltWithPBE the useSaltWithPBE to set
     */
    public void setUseSaltWithPBE(boolean useSaltWithPBE) {
        this.useSaltWithPBE = useSaltWithPBE;
    }

    /**
     * @return the saltSize
     */
    public int getSaltSize() {
        return saltSize;
    }

    /**
     * If saltSize is positive, it is taken as the size of the randomly
     * generated salt. If saltSize is negative, it represents a bit-pattern to
     * the first character of the password.
     * @param saltSize the saltSize to set
     */
    public void setSaltSize(int saltSize) {
        this.saltSize = saltSize;
    }

    /**
     * @return the saltIterations
     */
    public int getSaltIterations() {
        return Math.max(1, saltIterations);
    }

    /**
     * @param saltIterations the saltIterations to set
     */
    public void setSaltIterations(int saltIterations) {
        this.saltIterations = saltIterations;
    }

    /**
     * @return the integrityDirExcludes
     */
    public List<String> getIntegrityDirExcludes() {
        return integrityDirExcludes;
    }

    /**
     * @param integrityDirExcludes the integrityDirExcludes to set
     */
    public void setIntegrityDirExcludes(List<String> integrityDirExcludes) {
        this.integrityDirExcludes = integrityDirExcludes;
    }

    /**
     * @return the digestFileName
     */
    public String getDigestFileName() {
        return digestFileName;
    }

    /**
     * @param digestFileName the digestFileName to set
     */
    public void setDigestFileName(String digestFileName) {
        this.digestFileName = digestFileName;
    }
}
