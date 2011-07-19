package org.aklein.swap.security;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.DigestInputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.aklein.swap.examples.security.DefaultKeyWallet;
import org.aklein.swap.examples.security.DefaultUserManager;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CryptTool {
    private CryptoConfiguration config;

    private static Log log = LogFactory.getLog(CryptTool.class);

    private static final char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private List<File> wrongFiles = new ArrayList<File>();

    public static class ReaderInputStream extends InputStream {
        private Reader in;

        public ReaderInputStream(Reader in) {
            super();
            this.in = in;
        }

        @Override
        public int read() throws IOException {
            int t = in.read();
            return t;
        }

        @Override
        public void close() throws IOException {
            in.close();
        }

        @Override
        public synchronized void mark(int readlimit) {
            try {
                in.mark(readlimit);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean markSupported() {
            return in.markSupported();
        }

        @Override
        public synchronized void reset() throws IOException {
            in.reset();
        }

        @Override
        public long skip(long n) throws IOException {
            return in.skip(n);
        }

    }

    public static class WriterOutputStream extends OutputStream {
        private Writer in;

        public WriterOutputStream(Writer in) {
            super();
            this.in = in;
        }

        @Override
        public void write(int b) throws IOException {
            in.write(b);
        }

        @Override
        public void close() throws IOException {
            in.close();
        }

        @Override
        public void flush() throws IOException {
            in.flush();
        }
    }

    public static class NonClosingInputStream extends InputStream {
        private InputStream delegate;

        public NonClosingInputStream(InputStream delegate) {
            super();
            this.delegate = delegate;
        }

        @Override
        public int available() throws IOException {
            return delegate.available();
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public boolean equals(Object obj) {
            return delegate.equals(obj);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public void mark(int readlimit) {
            delegate.mark(readlimit);
        }

        @Override
        public boolean markSupported() {
            return delegate.markSupported();
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }

        @Override
        public int read(byte[] b) throws IOException {
            return delegate.read(b);
        }

        @Override
        public void reset() throws IOException {
            delegate.reset();
        }

        @Override
        public long skip(long n) throws IOException {
            return delegate.skip(n);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }

    public static class NonClosingOutputStream extends OutputStream {
        private OutputStream delegate;

        public NonClosingOutputStream(OutputStream delegate) {
            super();
            this.delegate = delegate;
        }

        @Override
        public void close() throws IOException {
            delegate.flush();
        }

        @Override
        public boolean equals(Object obj) {
            return delegate.equals(obj);
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            delegate.write(b, off, len);
        }

        @Override
        public void write(byte[] b) throws IOException {
            delegate.write(b);
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
        }
    }

    public CryptTool() {
        this(new CryptoConfiguration());
    }

    public CryptTool(CryptoConfiguration config) {
        this.config = config;
    }

    // public List<Key> getEncryptionKeys()
    // {
    // return config.getKeys();
    // }

    public int getKeyCount() {
        return config.getKeyCount();
    }

    public void addKey(Key key) {
        config.addKey(key);
    }

    public void removeKey(Key key) {
        config.removeKey(key);
    }

    public boolean containsKey(Key key) {
        return config.containsKey(key);
    }

    public Key getDefaultEncryptionKey() {
        return config.getDefaultEncryptionKey();
    }

    public SecretKey createSymetricKey() throws NoSuchAlgorithmException, IOException, ConfigurationException, NoSuchProviderException {
        return createSymetricKey(config.getSymetricKeyAlgorithm(), config.getSymetricKeyProvider(), config.getRandomAlgorithm(), config.getRandomProvider(), config.getSymetricKeySize());
    }

    public SecretKey createSymetricKey(String algorithm, String provider, String randomAlgorithm, String randomProvider, int keySize) throws NoSuchAlgorithmException, IOException,
            ConfigurationException, NoSuchProviderException {
        KeyGenerator kgen = null;
        if (provider == null)
            kgen = KeyGenerator.getInstance(algorithm);
        else
            kgen = KeyGenerator.getInstance(algorithm, provider);
        SecureRandom random = null;
        if (randomProvider == null)
            random = SecureRandom.getInstance(randomAlgorithm);
        else
            random = SecureRandom.getInstance(randomAlgorithm, randomProvider);
        kgen.init(keySize, random);
        return kgen.generateKey();
    }

    public SecretKey createSymetricKey(byte[] data, String algorithm) {
        return new SecretKeySpec(data, algorithm);
    }

    public static String pad(String k, String pattern, char placeholder) {
        char[] pat = pattern.toCharArray();
        char[] key = k.toCharArray();
        int idx = 0;
        StringBuilder out = new StringBuilder();
        for (char p : pat) {
            if (p == placeholder) {
                out.append(key[idx]);
                idx++;
            } else {
                out.append(p);
            }
        }
        if (k.length() > idx)
            throw new PatternSyntaxException("Pattern incorrect", pattern, idx);
        return out.toString();
    }

    public static String unpad(String source, String pattern, char placeholder) throws PatternSyntaxException {
        if (source.length() != pattern.length())
            throw new PatternSyntaxException("Pattern incorrect", pattern, -1);
        char[] pat = pattern.toCharArray();
        char[] src = source.toCharArray();
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < pat.length; i++) {
            char p = pat[i];
            char s = src[i];
            if (p == placeholder) {
                out.append(s);
            } else {
                if (s != p)
                    throw new PatternSyntaxException("Pattern incorrect", pattern, i);
            }
        }
        return out.toString();
    }

    public String encode(SecretKey skey) {
        try {
            return encode(skey, null);
        } catch (InvalidKeyException e) {
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchProviderException e) {
        } catch (NoSuchPaddingException e) {
        } catch (IllegalBlockSizeException e) {
        } catch (BadPaddingException e) {
        }
        return null;
    }

    public String encode(SecretKey skey, String password) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException {
        byte[] enc = externalize(skey);
        String k = Base32.encode(enc);
        String result = pad(k, config.getSymetricKeyExternalizePattern(), 'x');
        if (password != null)
            return encryptPBE(skey, result, password);
        return result;
    }

    public String encode(PrivateKey skey) {
        try {
            return encode(skey, null);
        } catch (InvalidKeyException e) {
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchProviderException e) {
        } catch (NoSuchPaddingException e) {
        } catch (IllegalBlockSizeException e) {
        } catch (BadPaddingException e) {
        }
        return null;
    }

    public String encode(PrivateKey key, String password) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException {
        byte[] kpriv = externalize(key);
        String result = Base64.encodeBytes(kpriv, Base64.DONT_BREAK_LINES);
        if (password != null)
            return encryptPBE(key, result, password);
        return result;
    }

    public String encode(PublicKey skey) {
        try {
            return encode(skey, null);
        } catch (InvalidKeyException e) {
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchProviderException e) {
        } catch (NoSuchPaddingException e) {
        } catch (IllegalBlockSizeException e) {
        } catch (BadPaddingException e) {
        }
        return null;
    }

    public String encode(PublicKey key, String password) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException {
        byte[] kpub = externalize(key);
        String result = Base64.encodeBytes(kpub, Base64.DONT_BREAK_LINES);
        if (password != null)
            return encryptPBE(key, result, password);
        return result;
    }

    public String encode(Key skey) {
        try {
            return encode(skey, null);
        } catch (InvalidKeyException e) {
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchProviderException e) {
        } catch (NoSuchPaddingException e) {
        } catch (IllegalBlockSizeException e) {
        } catch (BadPaddingException e) {
        }
        return null;
    }

    public String encode(Key key, String password) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException {
        if (key instanceof SecretKey)
            return encode((SecretKey) key, password);
        else if (key instanceof PrivateKey)
            return encode((PrivateKey) key, password);
        else if (key instanceof PublicKey)
            return encode((PublicKey) key, password);
        return "";
    }

    public SecretKey decode(String skey) throws PatternSyntaxException, GeneralSecurityException {
        String key = unpad(skey, config.getSymetricKeyExternalizePattern(), 'x');
        byte[] dec = Base32.decode(key);
        return new SecretKeySpec(dec, config.getSymetricKeyAlgorithm());
    }

    public byte[] externalize(SecretKey key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        return key.getEncoded();
    }

    public byte[] externalize(PublicKey key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        return key.getEncoded();
    }

    public byte[] externalize(PrivateKey key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        return key.getEncoded();
    }

    public byte[] externalize(Key key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        if (key instanceof SecretKey)
            return externalize((SecretKey) key);
        else if (key instanceof PublicKey)
            return externalize((PrivateKey) key);
        else if (key instanceof PublicKey)
            return externalize((PublicKey) key);
        return new byte[0];
    }

    public void generateSymetricKey(File file, boolean append) throws IOException, NoSuchAlgorithmException, ConfigurationException, NoSuchProviderException {
        List<String> keys = new ArrayList<String>();
        if (file.exists())
            keys = readLines(new FileReader(file));
        SecretKey skey = createSymetricKey();
        String key = encode(skey);
        if (append)
            keys.add(key);
        else
            keys.add(0, key);
        FileWriter fw = new FileWriter(file);
        for (String k : keys) {
            fw.write(k);
            fw.write("\n");
        }
        fw.flush();
        fw.close();
    }

    public KeyPair createAsymetricKeys() throws NoSuchAlgorithmException, IOException, ConfigurationException, NoSuchProviderException {
        return createAsymetricKeys(config.getAsymetricKeyAlgorithm(), config.getAsymetricKeyProvider(), config.getRandomAlgorithm(), config.getRandomProvider(), config.getAsymetricKeySize());
    }

    public KeyPair createAsymetricKeys(String algorithm, String provider, String randomAlgorithm, String randomProvider, int keySize) throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator kgen = null;
        if (provider == null)
            kgen = KeyPairGenerator.getInstance(algorithm);
        else
            kgen = KeyPairGenerator.getInstance(algorithm, provider);
        SecureRandom random = null;
        if (randomProvider == null)
            random = SecureRandom.getInstance(randomAlgorithm);
        else
            random = SecureRandom.getInstance(randomAlgorithm, randomProvider);
        kgen.initialize(keySize, random);
        return kgen.generateKeyPair();
    }

    public Key createPBEKey(String password) throws NoSuchAlgorithmException, NoSuchProviderException {
        MessageDigest md = null;
        if (config.getEncryptedKeyDigestProvider() == null)
            md = MessageDigest.getInstance(config.getEncryptedKeyDigestAlgorithm());
        else
            md = MessageDigest.getInstance(config.getEncryptedKeyDigestAlgorithm(), config.getEncryptedKeyDigestProvider());
        byte[] prkey = md.digest(password.getBytes());
        return createSymetricKey(prkey, config.getEncryptedKeyAlgorithm());
    }

    public String encryptPBE(String data, String password) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        return encryptPBE(null, data, data);
    }

    private String encryptPBE(Key key, String data, String password) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        if (password == null)
            password = config.getKeyEncryptionPassword(key);
        if (password != null) {
            Key skey = createPBEKey(password);
            Cipher cipher = getCipher(config.getEncryptedKeyAlgorithm(), config.getEncryptedKeyProvider());
            cipher.init(Cipher.ENCRYPT_MODE, skey);
            String eValue = data;
            if (config.isUseSaltWithPBE()) {
                String salt = createSalt(getSaltSize(password));
                for (int i = 0; i < config.getSaltIterations(); i++) {
                    String valueToEnc = salt + eValue;
                    eValue = Base64.encodeBytes(cipher.doFinal(valueToEnc.getBytes()), Base64.DONT_BREAK_LINES);
                }
            } else
                eValue = Base64.encodeBytes(cipher.doFinal(data.getBytes()), Base64.DONT_BREAK_LINES);
            return config.getEncryptedPrefix() + eValue;
        }
        return data;
    }

    private int getSaltSize(String password) {
        int size = config.getSaltSize();
        if (size < 0)
            size = (password.charAt(0)) & -size;
        return size;
    }

    private String createSalt(int size) {
        if (size == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++)
            sb.append((char) (Math.random() * 95) + 32);
        return sb.toString();
    }

    public String decryptPBE(String data, String password) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        if (!isEncrypted(data))
            return data;
        if (password == null)
            password = config.getKeyDecryptionPassword(data);
        if (password != null) {
            Key skey = createPBEKey(password);
            Cipher cipher = getCipher(config.getEncryptedKeyAlgorithm(), config.getEncryptedKeyProvider());
            cipher.init(Cipher.DECRYPT_MODE, skey);
            String dValue;
            if (config.isUseSaltWithPBE()) {
                int saltSize = getSaltSize(password);
                dValue = data.substring(config.getEncryptedPrefix().length());
                for (int i = 0; i < config.getSaltIterations(); i++)
                    dValue = new String(cipher.doFinal(Base64.decode(dValue))).substring(saltSize);
            } else
                dValue = new String(cipher.doFinal(Base64.decode(data.substring(config.getEncryptedPrefix().length()))));
            return dValue;
        }
        return data;
    }

    public Cipher getCipher(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
        if (provider == null)
            return Cipher.getInstance(algorithm);
        else
            return Cipher.getInstance(algorithm, provider);
    }

    public void generateAsymetricKeys(File pub, File priv, String password, boolean append) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, ConfigurationException, NoSuchProviderException {
        List<String> keys = new ArrayList<String>();
        if (pub.exists())
            keys = readLines(new FileReader(pub));
        KeyPair pair = createAsymetricKeys();
        if (append)
            keys.add(encode(pair.getPublic()));
        else
            keys.add(0, encode(pair.getPublic()));
        String epriv = encode(pair.getPrivate(), password);
        String epub = encode(pair.getPublic());

        FileWriter fw = new FileWriter(pub);
        for (String key : keys) {
            fw.write(key);
            fw.write("\n");
        }
        fw.flush();
        fw.close();

        fw = new FileWriter(priv);
        fw.write(epriv);
        fw.flush();
        fw.close();

        String name = priv.getName();
        int pos = name.lastIndexOf(".");
        if ((pos >= 0) && !(name.substring(pos + 1).equals("priv")))
            name = name.substring(0, pos);

        fw = new FileWriter(new File(priv.getParentFile(), name + ".priv"));
        fw.write(epub);
        fw.flush();
        fw.close();
    }

    public CryptoConfiguration getConfig() {
        return config;
    }

    // public void loadKeys(InputStream keyWallet) throws ConfigurationException, GeneralSecurityException, IOException
    // {
    // loadKeys(keyWallet, null);
    // }
    //
    // public void loadKeys(InputStream keyWallet, Key key) throws GeneralSecurityException, ConfigurationException,
    // IOException
    // {
    // if(config.getWalletType() == null)
    // throw new UnsupportedOperationException("CryptoConfiguration does not contain a walletType");
    // KeyWallet wallet = getKeyWallet(config.getWalletType(), keyWallet, key);
    // for(String keyName : wallet.getEntries().keySet())
    // {
    // String value = wallet.getEntries().get(keyName);
    // addKey(wallet.parseKey(value));
    // }
    // }

    public Key parseKey(String key) throws GeneralSecurityException {
        try {
            return parseKey(key, null);
        } catch (InvalidKeyException e) {
        } catch (NoSuchProviderException e) {
        } catch (ConfigurationException e) {
        } catch (NoSuchPaddingException e) {
        } catch (IllegalBlockSizeException e) {
        } catch (BadPaddingException e) {
        } catch (IOException e) {
        }
        // Exceptions should never occur without a password
        return null;
    }

    public Key parseKey(String key, String password) throws GeneralSecurityException, ConfigurationException, IOException {
        key = decryptPBE(key, password);
        try {
            return decode(key);
        } catch (PatternSyntaxException e) {
            KeyFactory keyFactory = null;
            if (config.getAsymetricKeyProvider() == null)
                keyFactory = KeyFactory.getInstance(config.getAsymetricKeyAlgorithm());
            else
                keyFactory = KeyFactory.getInstance(config.getAsymetricKeyAlgorithm(), config.getAsymetricKeyProvider());
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.decode(key, Base64.DONT_BREAK_LINES));
            try {
                return keyFactory.generatePublic(publicKeySpec);
            } catch (InvalidKeySpecException e1) {
                byte[] kpriv = Base64.decode(key, Base64.DONT_BREAK_LINES);
                EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(kpriv);
                return keyFactory.generatePrivate(privateKeySpec);
            }
        }
    }

    public KeyPair readKeyPair(Reader privateKey, Reader publicKey, String password) throws IOException, ConfigurationException, GeneralSecurityException {
        String prk = readComplete(privateKey);
        PrivateKey pKey = (PrivateKey) parseKey(prk, password);
        privateKey.close();
        String pk = readComplete(publicKey);
        PublicKey pubKey = (PublicKey) parseKey(prk, password);
        publicKey.close();
        return new KeyPair(pubKey, pKey);
    }

    public static List<String> readLines(Reader in) throws IOException {
        List<String> lines = new ArrayList<String>();
        BufferedReader br = new BufferedReader(in);
        String line = null;
        while ((line = br.readLine()) != null)
            lines.add(line);
        return lines;
    }

    public static byte[] readComplete(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for (int len; (len = in.read(buffer)) != -1;)
            baos.write(buffer, 0, len);
        baos.flush();
        return baos.toByteArray();
    }

    public static String readComplete(Reader in) throws IOException {
        StringWriter out = new StringWriter();
        char[] buffer = new char[0xFFFF];
        for (int len; (len = in.read(buffer)) != -1;)
            out.write(buffer, 0, len);
        out.flush();
        return out.toString();
    }

    public boolean isEncrypted(String data) {
        return data.startsWith(config.getEncryptedPrefix());
    }

    public boolean isEncrypted(InputStream in) {
        if (!in.markSupported())
            throw new RuntimeException("InputStream has to support mark");
        byte[] b = new byte[config.getEncryptedPrefix().length()];
        try {
            in.mark(b.length);
            in.read(b, 0, b.length);
            in.reset();
            return new String(b).equals(config.getEncryptedPrefix());
        } catch (IOException e) {
            try {
                in.reset();
            } catch (IOException e1) {
            }
            return false;
        }
    }

    public CipherOutputStream getEncryptionStream(Key key, OutputStream target, boolean withPrefix) throws ConfigurationException {
        try {
            // Daten verschlüsseln
            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, key);
            if (withPrefix)
                target.write(config.getEncryptedPrefix().getBytes());
            if (key.getAlgorithm().equals(config.getAsymetricKeyAlgorithm())) {
                // SessionKey erstellen
                SecretKey sessionkey = createSymetricKey();
                try {
                    // SessionKey verschlüsseln
                    target.write(Base64.encodeBytes(cipher.doFinal(encode(sessionkey).getBytes())).getBytes());
                    target.write(config.getAsymetricKeyDelimiter());
                    target.flush();
                    cipher = getCipher(config.getSymetricKeyAlgorithm(), config.getSymetricKeyProvider());
                    cipher.init(Cipher.ENCRYPT_MODE, sessionkey);
                } catch (Exception e) {
                    log.error("Error encrypting config file", e);
                    throw new ConfigurationException(e);
                }
            }
            return new CipherOutputStream(new Base64.OutputStream(new NonClosingOutputStream(target)), cipher);
        } catch (Exception e) {
            log.error("Error encrypting config file", e);
            throw new ConfigurationException(e);
        }
    }

    public void encrypt(InputStream source, Key key, OutputStream target, boolean withPrefix) throws ConfigurationException {
        try {
            // Daten verschlüsseln
            OutputStream t = getEncryptionStream(key, target, withPrefix);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = source.read(buffer)) > 0)
                t.write(buffer, 0, len);
            t.flush();
            t.close();
            target.flush();
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error encrypting config file", e);
            throw new ConfigurationException(e);
        }
    }

    public Key decrypt(InputStream in, OutputStream target) throws ConfigurationException {
        return decrypt(in, target, config.getKeysForDecoding());
    }

    public Key decrypt(InputStream in, OutputStream target, List<Key> keys) throws ConfigurationException {
        if (!in.markSupported())
            throw new RuntimeException("InputStream has to support mark");
        in.mark(1024 * 1024 * 1024);
        for (Key key : keys) {
            try {
                in.reset();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                decrypt(in, bos, key);
                target.write(bos.toByteArray());
                target.close();
                return key;
            } catch (ConfigurationException e) {
            } catch (IOException e) {
                log.error("Error reading encrypted config file", e);
                throw new ConfigurationException(e);
            }
        }
        return null;
    }

    public void decrypt(InputStream in, OutputStream target, Key skey) throws ConfigurationException {
        try {
            CipherInputStream cis = getDecryptionStream(in, skey);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = cis.read(buffer)) > 0)
                target.write(buffer, 0, len);
            target.flush();
            cis.close();
            if (cis.getExceptions().size() > 0) {
                for (Exception ex : cis.getExceptions())
                    log.debug("Internal error:", ex);
                throw new ConfigurationException("Key does not match to decrypt config file");
            }
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
    }

    public CipherInputStream getDecryptionStream(InputStream in, Key skey) throws ConfigurationException {
        if (!in.markSupported())
            throw new RuntimeException("InputStream has to support mark");
        try {
            in.mark(config.getEncryptedPrefix().length() + 10);
            byte[] prefix = new byte[config.getEncryptedPrefix().length()];
            in.read(prefix);
            if (!config.getEncryptedPrefix().equals(new String(prefix)))
                in.reset();
            if (skey.getAlgorithm().equals(config.getAsymetricKeyAlgorithm())) {
                // SessionKey einlesen
                String enckey = "";
                int c = 0;
                while ((c = in.read()) >= 0) {
                    if (c == config.getAsymetricKeyDelimiter())
                        break;
                    else
                        enckey += (char) c;
                }
                if (c < 0)
                    throw new ConfigurationException("Fileformat does not match");
                // SessionKey entschlüsseln
                Cipher sessionCipher = getCipher(config.getAsymetricKeyAlgorithm(), config.getAsymetricKeyProvider());;
                sessionCipher.init(Cipher.DECRYPT_MODE, skey);
                skey = decode(new String(sessionCipher.doFinal(Base64.decode(enckey))));
            }
            Cipher cipher = Cipher.getInstance(skey.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, skey);
            return new CipherInputStream(new Base64.InputStream(new NonClosingInputStream(in)), cipher);
        } catch (IOException e) {
            throw new ConfigurationException(e);
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    public boolean interactiveEncryption(InputStream source, OutputStream target, Key key) throws ConfigurationException {
        if (key instanceof PublicKey) {
            JFileChooser chooser = new JFileChooser();
            javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.getName().endsWith(".priv");
                }

                @Override
                public String getDescription() {
                    return "Private Keys (*.priv)";
                }
            };
            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal != JFileChooser.APPROVE_OPTION)
                return false;
            File priv = chooser.getSelectedFile();
            if (!priv.exists())
                return false;
            try {
                FileInputStream fis = new FileInputStream(priv);
                int count = 0;
                do {
                    count++;
                    String password = JOptionPane.showInputDialog(null, "Please enter the Password for the private key", "Loading the private key", JOptionPane.QUESTION_MESSAGE);
                    if (password == null)
                        return false;
                    try {
                        String pkey = new String(readComplete(fis));
                        key = parseKey(pkey, password);
                        try {
                            encrypt(source, key, target, true);
                            return true;
                        } catch (ConfigurationException e) {
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Password or key wrong", "Encryption failed", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                } while (count < config.getInteractiveEncryptionMaxTries());
            } catch (FileNotFoundException e) {
                throw new ConfigurationException("Error reading private keyfile " + priv.getAbsolutePath());
            }
            return false;
        } else {
            encrypt(source, key, target, true);
            return true;
        }
    }

    public static <T extends User>T authenticateUser(Class<? extends UserManager<T>> userManagerType, String name, InputStream userData, Key key) {
        try {
            UserManager<T> userManager = userManagerType.newInstance();
            T user = userManager.authenticateUser(name, userData, key);
            return user == null ? null : user;
        } catch (InstantiationException e) {
            log.error("Error loading user", e);
        } catch (IllegalAccessException e) {
            log.error("Error loading user", e);
        }
        return null;
    }

    public static <T extends KeyWallet>T getKeyWallet(Class<T> walletType, InputStream walletData, Key key) {
        try {
            T wallet = walletType.newInstance();
            wallet.setEncrypted(key);
            wallet.loadWallet(walletData);
            return wallet.isLoaded() ? wallet : null;
        } catch (InstantiationException e) {
            log.error("Error loading wallet", e);
        } catch (IllegalAccessException e) {
            log.error("Error loading wallet", e);
        }
        return null;
    }

    public String createIntegrityDigest(InputStream data) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
        MessageDigest md = null;
        if (config.getIntegrityDigestProvider() == null)
            md = MessageDigest.getInstance(config.getIntegrityDigestAlgorithm());
        else
            md = MessageDigest.getInstance(config.getIntegrityDigestAlgorithm(), config.getIntegrityDigestProvider());
        DigestInputStream dis = new DigestInputStream(data, md);
        dis.on(true);
        byte[] buffer = new byte[1024 * 1024];
        int len;
        while ((len = dis.read(buffer)) >= 0) {
        }
        return encodeToHex(md.digest());
    }

    public static String encodeToHex(byte[] b) {
        StringBuilder s = new StringBuilder(2 * b.length);
        for (byte element : b) {
            int v = element & 0xff;
            s.append(hexChars[v >> 4]);
            s.append(hexChars[v & 0xf]);
        }
        return s.toString();
    }

    public boolean checkIntegrity(File file, Key key, String... excludes) {
        wrongFiles.clear();
        checkIntegrity(file, key, null, true, excludes);
        return wrongFiles.isEmpty();
    }

    private void checkIntegrity(File file, Key key, Properties properties, boolean noReferenceIsOk, String... excludes) {
        if (!file.exists())
            return;
        String path = file.getAbsolutePath().replaceAll("\\\\", "/");
        for (String exclude : excludes) {
            if (path.matches(exclude))
                return;
        }

        if (file.isFile()) {
            if (properties == null) {
                if (!noReferenceIsOk) {
                    wrongFiles.add(file);
                    log.info("No properties: " + file.getAbsolutePath());
                }
                return;
            }
            String reference = properties.getProperty(file.getName());
            if (reference == null) {
                if (!noReferenceIsOk) {
                    wrongFiles.add(file);
                    log.info("No entry in digest for : " + file.getAbsolutePath());
                }
                return;
            }

            try {
                String digest = createIntegrityDigest(new FileInputStream(file));
                if (!reference.equals(digest)) {
                    wrongFiles.add(file);
                    log.info("File modified: " + file.getAbsolutePath());
                }
            } catch (NoSuchAlgorithmException e) {
                log.fatal("Error creating digest from file: " + file.getAbsolutePath(), e);
                wrongFiles.add(file);
            } catch (NoSuchProviderException e) {
                log.fatal("Error creating digest from file: " + file.getAbsolutePath(), e);
                wrongFiles.add(file);
            } catch (FileNotFoundException e) {
                log.fatal("Error creating digest from file: " + file.getAbsolutePath(), e);
                wrongFiles.add(file);
            } catch (IOException e) {
                log.fatal("Error creating digest from file: " + file.getAbsolutePath(), e);
                wrongFiles.add(file);
            }
            properties.remove(file.getName());
        } else {
            File digestFile = new File(file, ".digest");
            if (digestFile.exists()) {
                try {
                    Properties props = loadDigestFile(key, digestFile);
                    File[] files = file.listFiles(new FileFilter() {
                        public boolean accept(File pathname) {
                            if (pathname.getName().equals(".digest"))
                                return false;
                            if (pathname.isDirectory())
                                return false;
                            return true;
                        }
                    });
                    for (File f : files)
                        checkIntegrity(f, key, props, noReferenceIsOk, excludes);
                    if (props.size() > 0) {
                        Enumeration<?> names = props.propertyNames();
                        while (names.hasMoreElements()) {
                            String name = (String) names.nextElement();
                            wrongFiles.add(new File(file, name));
                            log.info("File deleted: " + new File(file, name).getAbsolutePath());
                        }
                        log.fatal(props.size() + " files have been deleted");
                    }
                } catch (ConfigurationException e) {
                    log.fatal("Error decrypting digestFile " + digestFile.getAbsolutePath(), e);
                    wrongFiles.add(digestFile);
                } catch (FileNotFoundException e) {
                    log.fatal("Error decrypting digestFile " + digestFile.getAbsolutePath(), e);
                    wrongFiles.add(digestFile);
                } catch (IOException e) {
                    log.fatal("Error loading digestFile " + digestFile.getAbsolutePath(), e);
                    wrongFiles.add(digestFile);
                }
            } else {
                if (!config.isIntegrityResultWithoutDigest()) {
                    log.fatal("DigestFile does not exist: " + digestFile.getAbsolutePath());
                    wrongFiles.add(digestFile);
                }
            }
            File[] files = file.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            if (file != null) {
                for (File f : files)
                    checkIntegrity(f, key, null, noReferenceIsOk, excludes);
            }
        }
    }

    private Properties loadDigestFile(Key key, File digestFile) throws ConfigurationException, IOException {
        BufferedInputStream fis = new BufferedInputStream(new FileInputStream(digestFile));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Properties props;
        if (key != null && isEncrypted(fis)) {
            decrypt(fis, bos, key);
            byte[] content = bos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(content);
            bos = null;
            props = new Properties();
            props.load(bis);
            bis = new ByteArrayInputStream(content);
            BufferedReader br = new BufferedReader(new InputStreamReader(bis));
            String firstLine = br.readLine();
            if (!firstLine.matches("#\\s*" + digestFile.getParentFile().getName())) {
                boolean ex = false;
                for (String exclude : config.getIntegrityDirExcludes()) {
                    if (firstLine.matches("#\\s*" + exclude)) {
                        ex = true;
                        break;
                    }
                }
                if (!ex)
                    throw new ConfigurationException("This is a digest file for a diffreent directory: " + digestFile.getAbsolutePath());
            }
            content = null;
            bis = null;
        } else {
            props = new Properties();
            props.load(fis);
        }
        return props;
    }

    public List<File> getWrongFiles() {
        return wrongFiles;
    }

    public void createDigestFile(File dir, Key key) throws IOException {
        createDigestFile(dir, true, key);
    }

    public void createDigestFile(File dir, boolean recursive, Key key) throws IOException {
        createDigestFile(dir, new HashSet<File>(), recursive, key);
    }

    public void createDigestFile(File dir, File[] excludes, boolean recursive, Key key) throws IOException {
        createDigestFile(dir, new HashSet<File>(Arrays.asList(excludes)), recursive, key);
    }

    private void createDigestFile(File dir, Set<File> excludes, boolean recursive, Key key) throws IOException {
        if (dir == null || !dir.exists())
            throw new IOException("File " + dir.getAbsolutePath() + " does not exist.");
        if (!dir.isDirectory())
            throw new IOException("File " + dir.getAbsolutePath() + " has to be a directory.");
        File digestFile = new File(dir, ".digest");
        if (digestFile.exists())
            digestFile.delete();
        Properties props = new Properties();
        File[] files = dir.listFiles();
        List<File> dirs = new ArrayList<File>();
        for (File file : files) {
            if (file.isFile()) {
                try {
                    if (!excludes.contains(file))
                        props.setProperty(file.getName(), createIntegrityDigest(new FileInputStream(file)));
                } catch (NoSuchAlgorithmException e) {
                    log.fatal("Error creating digest", e);
                } catch (NoSuchProviderException e) {
                    log.fatal("Error creating digest", e);
                } catch (FileNotFoundException e) {
                    log.fatal("Error creating digest", e);
                } catch (IOException e) {
                    log.fatal("Error creating digest", e);
                }
            } else if (recursive) {
                dirs.add(file);
            }
        }
        saveDigestFile(key, digestFile, props);

        for (File subDir : dirs)
            createDigestFile(subDir, excludes, recursive, key);
    }

    private void saveDigestFile(Key key, File digestFile, Properties props) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(digestFile);
        if (key == null)
            props.store(fos, null);
        else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            props.store(bos, digestFile.getParentFile().getName());
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            bos = null;
            try {
                encrypt(bis, key, fos, true);
            } catch (ConfigurationException e) {
                log.fatal("Error encrypting file: " + digestFile.getAbsolutePath(), e);
            }
        }
        fos.flush();
        fos.close();
    }

    public void recreateDigestFile(File digestFile, Key readKey, Key writeKey) throws IOException {
        if (digestFile == null || !digestFile.exists())
            throw new IOException("File " + digestFile.getAbsolutePath() + " does not exist.");
        if (!digestFile.isFile())
            throw new IOException("File " + digestFile.getAbsolutePath() + " has to be a file.");
        Properties props;
        try {
            props = loadDigestFile(readKey, digestFile);
            File dir = digestFile.getParentFile();
            Set<Object> keys = new HashSet<Object>(props.keySet());
            for (Object f : keys) {
                String name = f.toString();
                File file = new File(dir, name);
                if (file.exists())
                    props.setProperty(name, createIntegrityDigest(new FileInputStream(file)));
                else
                    props.remove(name);
            }
            saveDigestFile(writeKey, digestFile, props);
        } catch (ConfigurationException e) {
            throw new IOException("Error loading " + digestFile.getAbsolutePath());
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Error creating digest file: " + digestFile.getAbsolutePath());
        } catch (NoSuchProviderException e) {
            throw new IOException("Error creating digest file: " + digestFile.getAbsolutePath());
        }
    }

    public Key readKey(File keyFile) throws ConfigurationException, GeneralSecurityException, IOException {
        return readKey(keyFile, null);
    }

    public Key readKey(File keyFile, String password) throws ConfigurationException, GeneralSecurityException, IOException {
        String data = new String(readComplete(new FileInputStream(keyFile)));
        return parseKey(data, password);
    }

    public static void main(String[] args) {
        CryptTool ct = new CryptTool();
        try {
            DefaultUserManager.User user = CryptTool.authenticateUser(DefaultUserManager.class, "Sascha", new BufferedInputStream(new FileInputStream(new File("conf/security/users/Sascha"))), ct
                    .createPBEKey("111"));
            if (user == null)
                System.out.println("Login failed");
            else
                System.out.println("Login successfull");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            if (CryptTool.getKeyWallet(DefaultKeyWallet.class, new BufferedInputStream(new FileInputStream(new File("conf/security/keys/licence.keys"))), null) == null)
                System.out.println("Wallet loading failed");
            else
                System.out.println("Wallet loading successfull");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        long start = System.currentTimeMillis();
        try {
            Key priv = ct.readKey(new File("conf/security/keys/digest.priv"));
            File dir = new File("C:/hamstersimulator-v25-02");
            ct.createDigestFile(dir, priv);
            log.fatal("Create: " + (System.currentTimeMillis() - start));
        } catch (Exception e) {
            e.printStackTrace();
        }
        start = System.currentTimeMillis();
        try {
            Key pub = ct.readKey(new File("conf/security/keys/digest.pub"));
            File dir = new File("C:/hamstersimulator-v25-02");
            if (ct.checkIntegrity(dir, pub))
                log.fatal("ok");
            else
                log.fatal("error");
            log.fatal("Check: " + (System.currentTimeMillis() - start));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
