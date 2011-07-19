package org.aklein.swap.examples.security;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumnModel;

import org.aklein.swap.examples.security.resources.SecurityUserManager;
import org.aklein.swap.security.CryptTool;
import org.aklein.swap.security.KeyWallet;
import org.aklein.swap.security.User;
import org.aklein.swap.security.UserManager;
import org.aklein.swap.security.XXmlConfiguration;
import org.aklein.swap.ui.AbeilleViewControllerPanel;
import org.aklein.swap.util.TableUtil;
import org.aklein.swap.util.binding.Binder;
import org.aklein.swap.util.swing.ListBasedTableModel;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.ShadingColorHighlighter;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.jeta.forms.gui.common.FormException;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.value.AbstractConverter;
import com.jgoodies.binding.value.ValueModel;

public class SecurityManagerPanel extends AbeilleViewControllerPanel {
    private File							keyFolder			= null;
    private File							configFolder		= null;
    private File							walletFile			= null;
    private File							userFolder			= null;
    private TableColumnModel				keysColumnModel;
    private ListBasedTableModel<Object[]>	keysModel;
    private TableColumnModel				filesColumnModel;
    private ListBasedTableModel<Object[]>	filesModel;
    private TableColumnModel				walletColumnModel;
    private ListBasedTableModel<Object[]>	walletModel;
    private TableColumnModel				usersColumnModel;
    private ListBasedTableModel<Object[]>	usersModel;
    @Autowired
    @Qualifier("KeyWallet")
    private KeyWallet						wallet;
    @Autowired
    @Qualifier("UserManager")
    private UserManager<User>				userManager;
    private boolean							walletEncrypted		= false;
    private Key								walletEncryptionKey	= null;
    private Key								masterKey			= null;

    private static Log						log					= LogFactory.getLog(SecurityManagerPanel.class);

    public SecurityManagerPanel() throws FormException {
        super(SecurityManagerPanel.class.getResourceAsStream("resources/KeyManager.jfrm"));
    }

    @Override
    protected Component[] focusComponents() {
        return new Component[] { getBtnKeyFolder(), getBtnConfigFolder(), getTblKeys(), getBtnAddKey(), getBtnAddAsymetricKey(), getBtnRemoveKey(), getBtnAddWallet(), getBtnConfigFolder(),
                getTblFiles(), getBtnEncrypt(), getBtnDecrypt(), getBtnWalletFile(), getTblWallet(), getBtnAddEntry(), getBtnRemoveEntry(), getBtnUserFolder(), getTblUsers(), getBtnAddUser(),
                getBtnRemoveUser(), getBtnEditUser() };
    }

    private File loadFile(String key) {
        String name = getConfig().getConfiguration("user").getString(key);
        return name == null ? null : new File(name);
    }

    @Override
    protected void initComponents() {
        if (userManager instanceof SecurityUserManager)
            ((SecurityUserManager) userManager).setParent(Application.getInstance(SingleFrameApplication.class).getMainFrame());

        Binder.bindLabel(getLblKeyFolderData(), new FileToStringConverter(new PropertyAdapter<SecurityManagerPanel>(this, "keyFolder", true), getResourceMap().getString("keyFolder.unset")));
        Binder.bindLabel(getLblConfigFolderData(), new FileToStringConverter(new PropertyAdapter<SecurityManagerPanel>(this, "configFolder", true), getResourceMap().getString("configFolder.unset")));
        Binder.bindLabel(getLblWalletFileData(), new FileToStringConverter(new PropertyAdapter<SecurityManagerPanel>(this, "walletFile", true), getResourceMap().getString("walletFile.unset")));
        Binder.bindLabel(getLblUserFolderData(), new FileToStringConverter(new PropertyAdapter<SecurityManagerPanel>(this, "userFolder", true), getResourceMap().getString("userFolder.unset")));

        addPropertyChangeListener("walletEncrypted", new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if ((Boolean) evt.getNewValue())
                    getBtnWalletEncrypted().setText(getResourceMap().getString("wallet.decrypt"));
                else
                    getBtnWalletEncrypted().setText(getResourceMap().getString("wallet.encrypt"));
            }
        });
        setConfigFolder(loadFile("configFolder"));
        setKeyFolder(loadFile("keyFolder"));
        setWalletFile(loadFile("walletFile"));
        setUserFolder(loadFile("userFolder"));

        keysColumnModel = new DefaultTableColumnModelExt();
        TableUtil.fillTableColumns(getConfig(), keysColumnModel, "keys");
        keysModel = new ListBasedTableModel<Object[]>(keysColumnModel);
        getTblKeys().setAutoCreateRowSorter(false);
        getTblKeys().setRowSorter(null);
        getTblKeys().setModel(keysModel);
        getTblKeys().setColumnModel(keysColumnModel);
        getTblKeys().setAutoCreateRowSorter(true);
        fillKeys();
        getTblKeys().setColumnControlVisible(false);
        getTblKeys().setRolloverEnabled(true);
        getTblKeys().setShowGrid(true, false);
        getTblKeys().setSortable(true);
        getTblKeys().setHighlighters(new ShadingColorHighlighter(new HighlightPredicate() {

            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                return (adapter.row % 2) == 1;
            }
        }));

        filesColumnModel = new DefaultTableColumnModelExt();
        TableUtil.fillTableColumns(getConfig(), filesColumnModel, "files");
        filesModel = new ListBasedTableModel<Object[]>(filesColumnModel);
        getTblFiles().setAutoCreateRowSorter(false);
        getTblFiles().setRowSorter(null);
        getTblFiles().setModel(filesModel);
        getTblFiles().setColumnModel(filesColumnModel);
        getTblFiles().setAutoCreateRowSorter(true);
        fillFiles();
        getTblFiles().setColumnControlVisible(false);
        getTblFiles().setRolloverEnabled(true);
        getTblFiles().setShowGrid(true, false);
        getTblFiles().setSortable(true);
        getTblFiles().setHighlighters(new ShadingColorHighlighter(new HighlightPredicate() {

            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                return (adapter.row % 2) == 1;
            }
        }));

        walletColumnModel = new DefaultTableColumnModelExt();
        TableUtil.fillTableColumns(getConfig(), walletColumnModel, "wallet");
        walletModel = new ListBasedTableModel<Object[]>(walletColumnModel);
        getTblWallet().setAutoCreateRowSorter(false);
        getTblWallet().setRowSorter(null);
        getTblWallet().setModel(walletModel);
        getTblWallet().setColumnModel(walletColumnModel);
        getTblWallet().setAutoCreateRowSorter(true);
        fillWallet();
        getTblWallet().setColumnControlVisible(false);
        getTblWallet().setRolloverEnabled(true);
        getTblWallet().setShowGrid(true, false);
        getTblWallet().setSortable(true);
        getTblWallet().setHighlighters(new ShadingColorHighlighter(new HighlightPredicate() {

            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                return (adapter.row % 2) == 1;
            }
        }));

        usersColumnModel = new DefaultTableColumnModelExt();
        TableUtil.fillTableColumns(getConfig(), usersColumnModel, "users");
        usersModel = new ListBasedTableModel<Object[]>(usersColumnModel);
        getTblUsers().setAutoCreateRowSorter(false);
        getTblUsers().setRowSorter(null);
        getTblUsers().setModel(usersModel);
        getTblUsers().setColumnModel(usersColumnModel);
        getTblUsers().setAutoCreateRowSorter(true);
        fillUsers();
        getTblUsers().setColumnControlVisible(false);
        getTblUsers().setRolloverEnabled(true);
        getTblUsers().setShowGrid(true, false);
        getTblUsers().setSortable(true);
        getTblUsers().setHighlighters(new ShadingColorHighlighter(new HighlightPredicate() {

            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                return (adapter.row % 2) == 1;
            }
        }));

        getBtnConfigFolder().setAction(getAction("selectConfigFolder"));
        getBtnKeyFolder().setAction(getAction("selectKeyFolder"));
        getBtnWalletFile().setAction(getAction("selectWalletFile"));
        getBtnUserFolder().setAction(getAction("selectUserFolder"));
        getBtnMasterKey().setAction(getAction("selectMasterKey"));
        getBtnMasterKeyFromUser().setAction(getAction("selectMasterKeyFromUser"));

        getBtnAddKey().setAction(getAction("addSymetricKey"));
        getBtnAddAsymetricKey().setAction(getAction("addAsymetricKey"));
        getBtnRemoveKey().setAction(getAction("removeKey"));
        getBtnUseAsMasterKey().setAction(getAction("useAsMasterKey"));
        getBtnAddWallet().setAction(getAction("addWallet"));
        getBtnEncrypt().setAction(getAction("encryptFile"));
        getBtnDecrypt().setAction(getAction("decryptFile"));
        getBtnAuthorizeKey().setAction(getAction("authorizeKey"));
        getBtnRemoveEntry().setAction(getAction("removeEntry"));
        getBtnAddEntry().setAction(getAction("addEntry"));
        getBtnEditEntry().setAction(getAction("editEntry"));
        getBtnWalletEncrypted().setAction(getAction("walletEncrypted"));
        getBtnAddUser().setAction(getAction("addUser"));
        getBtnRemoveUser().setAction(getAction("removeUser"));
        getBtnEditUser().setAction(getAction("editUser"));
        getBtnRenameUser().setAction(getAction("renameUser"));
        getBtnMasterKeyFromWallet().setAction(getAction("selectMasterKeyFromWallet"));

        ListSelectionListener l = new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                getBtnEncrypt().setEnabled(getTblKeys().getSelectedRowCount() > 0);
                getBtnDecrypt().setEnabled(getTblFiles().getSelectedRowCount() > 0);
                getBtnAddWallet().setEnabled(getTblKeys().getSelectedRowCount() == 1);
                getBtnAuthorizeKey().setEnabled(getTblKeys().getSelectedRowCount() == 1);
                getBtnUseAsMasterKey().setEnabled(getTblKeys().getSelectedRowCount() == 1);
                getBtnRemoveKey().setEnabled(getTblKeys().getSelectedRowCount() > 0);
            }
        };
        getTblKeys().getSelectionModel().addListSelectionListener(l);
        getTblFiles().getSelectionModel().addListSelectionListener(l);
        getBtnEncrypt().setEnabled(false);
        getBtnDecrypt().setEnabled(false);
        getBtnAuthorizeKey().setEnabled(false);
        getBtnUseAsMasterKey().setEnabled(false);
        getBtnRemoveKey().setEnabled(false);
        getBtnAddWallet().setEnabled(false);

        ListSelectionListener l1 = new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                getBtnRemoveEntry().setEnabled(getTblWallet().getSelectedRowCount() > 0);
                getBtnEditEntry().setEnabled(getTblWallet().getSelectedRowCount() == 1);
            }
        };
        getTblWallet().getSelectionModel().addListSelectionListener(l1);
        getBtnRemoveEntry().setEnabled(false);
        getBtnEditEntry().setEnabled(false);

        ListSelectionListener l2 = new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                getBtnRemoveUser().setEnabled(getTblUsers().getSelectedRowCount() > 0);
                getBtnRenameUser().setEnabled(getTblUsers().getSelectedRowCount() == 1);
                getBtnEditUser().setEnabled(getMasterKey() != null && getTblUsers().getSelectedRowCount() == 1);
                getBtnMasterKeyFromUser().setEnabled(getTblUsers().getSelectedRowCount() == 1);
            }
        };
        getTblUsers().getSelectionModel().addListSelectionListener(l2);
        getBtnRemoveUser().setEnabled(false);
        getBtnRenameUser().setEnabled(false);
        getBtnEditUser().setEnabled(false);
        getBtnMasterKeyFromUser().setEnabled(false);
        getBtnAddUser().setEnabled(false);

        setMasterKey(null, null);

        getBtnMasterKeyFromWallet().setEnabled(getWalletEncryptionKey() != null);
    }

    @Override
    protected void afterFix() {
        if (wallet.isEncrypted())
            getBtnWalletEncrypted().setText(getResourceMap().getString("wallet.decrypt"));
        else
            getBtnWalletEncrypted().setText(getResourceMap().getString("wallet.encrypt"));
    }

    public boolean isWalletEncrypted() {
        return walletEncrypted;
    }

    public void setWalletEncrypted(boolean walletEncrypted) {
        Object old = this.walletEncrypted;
        this.walletEncrypted = walletEncrypted;
        firePropertyChange("walletEncrypted", old, walletEncrypted);
    }

    public Key getWalletEncryptionKey() {
        return walletEncryptionKey;
    }

    public void setWalletEncryptionKey(Key walletEncryptionKey) {
        Object old = this.walletEncryptionKey;
        this.walletEncryptionKey = walletEncryptionKey;
        firePropertyChange("walletEncryptionKey", old, walletEncryptionKey);
        getBtnMasterKeyFromWallet().setEnabled(getWalletEncryptionKey() != null);
        wallet.setEncrypted(walletEncryptionKey);
    }

    public Key getMasterKey() {
        return masterKey;
    }

    public void setMasterKey(Key masterKey, File user) {
        Object old = this.masterKey;
        this.masterKey = masterKey;
        firePropertyChange("masterKey", old, masterKey);
        if (masterKey == null)
            getLblMasterKeyData().setText(getResourceMap().getString("masterKey.unset"));
        else if (masterKey.equals(getWalletEncryptionKey()))
            getLblMasterKeyData().setText(getResourceMap().getString("masterKey.wallet"));
        else if (user != null)
            getLblMasterKeyData().setText(getResourceMap().getString("masterKey.byUser", user.getName()));
        else
            getLblMasterKeyData().setText(getResourceMap().getString("masterKey.manually"));
        fillUsers();
        getBtnAddUser().setEnabled(this.masterKey != null);
    }

    @Action
    public void addSymetricKey(ActionEvent e) {
        CryptTool cryptTool = XXmlConfiguration.getCryptTool();
        try {
            File parent = getKeyFolder();
            JFileChooser jfc = new JFileChooser(parent);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.addChoosableFileFilter(new FileFilter() {

                @Override
                public boolean accept(File f) {
                    return f.getName().endsWith(".key");
                }

                @Override
                public String getDescription() {
                    return getResourceMap().getString("filter.symetric");
                }
            });
            int result = jfc.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                SecretKey key = cryptTool.createSymetricKey();
                String password = PasswordDialog.getPassword(Application.getInstance(SingleFrameApplication.class).getMainFrame(), getResourceMap().getString("password.message"));
                if (password != null && password.length() == 0)
                    password = null;
                String ext = cryptTool.encode(key, password);
                File file = jfc.getSelectedFile();
                if (!file.getName().endsWith(".key"))
                    file = new File(file.getParentFile(), file.getName() + ".key");
                FileWriter fw = new FileWriter(file);
                fw.write(ext);
                fw.close();
                keysModel.addRow(new Object[] { false, getResourceMap().getString("type.symetric"), file.getName(), cryptTool.isEncrypted(ext), !cryptTool.isEncrypted(ext), ext, null });
            }
        }
        catch (Exception e1) {
            log.error("Error creating symetric key", e1);
            e1.printStackTrace();
        }
    }

    @Action
    public void addAsymetricKey(ActionEvent e) {
        CryptTool cryptTool = XXmlConfiguration.getCryptTool();
        try {
            File parent = getKeyFolder();
            JFileChooser jfc = new JFileChooser(parent);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.addChoosableFileFilter(new FileFilter() {

                @Override
                public boolean accept(File f) {
                    return f.getName().endsWith(".priv") || f.getName().endsWith(".pub");
                }

                @Override
                public String getDescription() {
                    return getResourceMap().getString("filter.asymetric");
                }
            });
            int result = jfc.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                KeyPair pair = cryptTool.createAsymetricKeys();
                String password = PasswordDialog.getPassword(Application.getInstance(SingleFrameApplication.class).getMainFrame(), getResourceMap().getString("password.message"));
                if (password != null && password.length() == 0)
                    password = null;
                String priv = cryptTool.encode(pair.getPrivate(), password);
                String pub = cryptTool.encode(pair.getPublic());
                File file = jfc.getSelectedFile();
                String name = file.getName();
                if (name.endsWith(".priv") || name.endsWith(".pub"))
                    name = name.substring(0, name.lastIndexOf('.'));
                File privFile = new File(file.getParentFile(), name + ".priv");
                File pubFile = new File(file.getParentFile(), name + ".pub");
                FileWriter fw = new FileWriter(pubFile);
                fw.write(pub);
                fw.close();
                keysModel.addRow(new Object[] { false, getResourceMap().getString("type.public"), pubFile.getName(), cryptTool.isEncrypted(pub), !cryptTool.isEncrypted(pub), pub, null });
                fw = new FileWriter(privFile);
                fw.write(priv);
                fw.close();
                keysModel.addRow(new Object[] { false, getResourceMap().getString("type.private"), privFile.getName(), cryptTool.isEncrypted(priv), !cryptTool.isEncrypted(priv), priv, null });
            }
        }
        catch (Exception e1) {
            log.error("Error creating symetric key", e1);
            e1.printStackTrace();
        }
    }

    @Action
    public void removeKey(ActionEvent e) {
        if (JOptionPane
                .showConfirmDialog(this, getResourceMap().getString("remove.text", getTblKeys().getSelectedRows().length), getResourceMap().getString("remove.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            List<Object[]> del = new ArrayList<Object[]>();
            for (int row : getTblKeys().getSelectedRows()) {
                int r = getTblKeys().convertRowIndexToModel(row);
                del.add(keysModel.getRow(r));
            }

            for (Object[] row : del) {
                File file = new File(getKeyFolder(), (String) row[2]);
                wallet.removeFromWallet((String) row[5]);
                saveWallet();
                file.delete();
                keysModel.removeRow(row);
            }
        }
    }

    @Action
    public void walletEncrypted(ActionEvent e) {
        if (wallet.isEncrypted()) {
            wallet.setEncrypted(null);
            setWalletEncryptionKey(null);
            setWalletEncrypted(false);
        }
        else {
            Key key = PasswordDialog.open(Application.getInstance(SingleFrameApplication.class).getMainFrame(), getResourceMap().getString("password2.message"), keyFolder, true);
            if (key == null)
                return;
            wallet.setEncrypted(key);

            setWalletEncryptionKey(key);
            setWalletEncrypted(true);
        }
        try {
            FileOutputStream fos = new FileOutputStream(getWalletFile());
            wallet.saveWallet(fos);
            fos.flush();
            fos.close();
        }
        catch (IOException e1) {
            log.error("Error saving wallet", e1);
        }

    }

    @Action
    public void addWallet(ActionEvent e) {
        int row = getTblKeys().getSelectedRow();
        int r = getTblKeys().convertRowIndexToModel(row);
        Object[] rowData = keysModel.getRow(r);
        String key = JOptionPane.showInputDialog(this, getResourceMap().getString("walletKey.message"), getResourceMap().getString("walletKey.title"), JOptionPane.QUESTION_MESSAGE);
        if (key == null || key.length() == 0)
            return;
        wallet.addToWallet(key, (String) rowData[5]);
        saveWallet();
        walletModel.addRow(new Object[] { key, (String) rowData[5] });
        keysModel.setValueAt(true, r, 0);
    }

    @Action
    public void removeEntry(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(this, getResourceMap().getString("removeEntry.text", getTblWallet().getSelectedRows().length), getResourceMap().getString("removeEntry.title"),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            List<Object[]> del = new ArrayList<Object[]>();
            for (int row : getTblWallet().getSelectedRows()) {
                int r = getTblWallet().convertRowIndexToModel(row);
                del.add(walletModel.getRow(r));
                keysModel.setValueAt(false, r, 0);
            }

            for (Object[] row : del) {
                wallet.removeFromWallet((String) row[0]);
                saveWallet();
                walletModel.removeRow(row);
            }
        }
    }

    private boolean saveWallet() {
        if (getWalletFile() == null)
            return false;
        try {
            OutputStream fos = new FileOutputStream(getWalletFile());
            wallet.saveWallet(fos);
            fos.flush();
            fos.close();
            return true;
        }
        catch (FileNotFoundException e1) {
            log.error("Error saving wallet", e1);
        }
        catch (IOException e1) {
            log.error("Error saving wallet", e1);
        }
        return false;
    }

    @Action
    public void addEntry(ActionEvent e) {
        String key = JOptionPane.showInputDialog(this, getResourceMap().getString("walletKey.message"), getResourceMap().getString("walletKey.title"), JOptionPane.QUESTION_MESSAGE);
        String value = JOptionPane.showInputDialog(this, getResourceMap().getString("walletValue.message"), getResourceMap().getString("walletValue.title"), JOptionPane.QUESTION_MESSAGE);
        wallet.addToWallet(key, value);
        saveWallet();
        walletModel.addRow(new Object[] { key, value });
    }

    @Action
    public void editEntry(ActionEvent e) {
        int row = getTblWallet().getSelectedRow();
        row = getTblWallet().convertRowIndexToModel(row);
        String key = (String) walletModel.getRow(row)[0];
        String value = JOptionPane.showInputDialog(this, getResourceMap().getString("walletValue.message"), walletModel.getRow(row)[1]);
        if (value != null) {
            wallet.setWalletValue(key, value);
            saveWallet();
            walletModel.setValueAt(value, row, 1);
        }
    }

    @Action
    public void authorizeKey(ActionEvent e) {
        int row = getTblKeys().getSelectedRow();
        row = getTblKeys().convertRowIndexToModel(row);
        Object[] rowData = keysModel.getRow(row);
        try {
            Key key = wallet.parseKey((String) rowData[5]);
            rowData[6] = key;
            keysModel.setValueAt(true, row, 4);
        }
        catch (GeneralSecurityException e1) {
            log.error("Error parsing key", e1);
        }
        catch (ConfigurationException e1) {
            log.error("Error parsing key", e1);
        }
        catch (IOException e1) {
            log.error("Error parsing key", e1);
        }
    }

    @Action
    public void useAsMasterKey(ActionEvent e) {
        int row = getTblKeys().getSelectedRow();
        row = getTblKeys().convertRowIndexToModel(row);
        Object[] rowData = keysModel.getRow(row);
        Key key = (Key) rowData[6];
        if (key == null) {
            authorizeKey(null);
            key = (Key) rowData[6];
        }
        setMasterKey(key, null);
    }

    @Action
    public void addUser(ActionEvent e) {
        User user = userManager.createUser(getMasterKey());
        if (user != null) {
            try {
                FileOutputStream fos = new FileOutputStream(new File(getUserFolder(), user.getName()));
                userManager.saveUser(user, fos, getMasterKey());
                fos.flush();
                fos.close();
                usersModel.addRow(new Object[] { user.getName(), userManager.getUserInfos(user, getMasterKey()), true });
            }
            catch (FileNotFoundException e1) {
                log.error("Error creating user", e1);
            }
            catch (IOException e1) {
                log.error("Error creating user", e1);
            }
        }
    }

    @Action
    public void editUser(ActionEvent e) {
        int row = getTblUsers().getSelectedRow();
        row = getTblUsers().convertRowIndexToModel(row);
        Object[] data = usersModel.getRow(row);
        File file = new File(getUserFolder(), (String) data[0]);
        try {
            if (userManager.editUser(file.getName(), new FileInputStream(file), getMasterKey())) {
                FileOutputStream fos = new FileOutputStream(file);
                try {
                    User user = userManager.getUser(file.getName(), new FileInputStream(file), getMasterKey());
                    userManager.saveUser(user, fos, getMasterKey());
                    fos.flush();
                    fos.close();
                }
                catch (IOException e1) {
                    log.error("Error saving user", e1);
                }
                usersModel.setValueAt(file.getName(), row, 0);
                usersModel.setValueAt(userManager.getUserInfos(file.getName(), new FileInputStream(file), getMasterKey()), row, 1);
                Boolean valid = userManager.isValid(file.getName(), new FileInputStream(file), getMasterKey(), false);
                usersModel.setValueAt(valid, row, 2);
            }
        }
        catch (FileNotFoundException e1) {
            log.error("Error editing user", e1);
        }
    }

    @Action
    public void renameUser(ActionEvent e) {
        int row = getTblUsers().getSelectedRow();
        row = getTblUsers().convertRowIndexToModel(row);
        Object[] data = usersModel.getRow(row);
        File user = new File(getUserFolder(), (String) data[0]);
        String username = JOptionPane.showInputDialog(this, getResourceMap().getString("rename.message"), getResourceMap().getString("rename.title"), JOptionPane.QUESTION_MESSAGE);
        username.replaceAll("/", "_");
        username.replaceAll("\\*", "_");
        username.replaceAll("\\\\", "_");
        username.replaceAll("\\?", "_");
        username.replaceAll("\\-", "_");
        username.replaceAll(">", "_");
        username.replaceAll("<", "_");
        username.replaceAll("\\|", "_");
        user.renameTo(new File(user.getParentFile(), username));
        usersModel.setValueAt(username, row, 0);
    }

    @Action
    public void removeUser(ActionEvent e) {
        List<Object[]> del = new ArrayList<Object[]>();
        for (int row : getTblUsers().getSelectedRows()) {
            int r = getTblUsers().convertRowIndexToModel(row);
            del.add(usersModel.getRow(r));
        }

        for (Object[] row : del) {
            File user = new File(getUserFolder(), (String) row[0]);
            if (userManager.removeUser(user.getName(), getMasterKey()))
                usersModel.removeRow(row);
        }
    }

    @Action
    public void selectConfigFolder(ActionEvent e) {
        File base = getConfigFolder() == null ? new File(".") : getConfigFolder();
        JFileChooser jfc = new JFileChooser(base);
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = jfc.showDialog(this, "Select");
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            setConfigFolder(file);
            fillFiles();
        }
    }

    @Action
    public void selectUserFolder(ActionEvent e) {
        File base = getUserFolder() == null ? new File(".") : getUserFolder();
        JFileChooser jfc = new JFileChooser(base);
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = jfc.showDialog(this, "Select");
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            setUserFolder(file);
            fillUsers();
        }
    }

    @Action
    public void selectMasterKey(ActionEvent e) {
        Key key = PasswordDialog.open(Application.getInstance(SingleFrameApplication.class).getMainFrame(), getResourceMap().getString("masterPassword.message"), keyFolder, true);
        if (key != null)
            setMasterKey(key, null);
    }

    @Action
    public void selectMasterKeyFromUser(ActionEvent e) {
        int row = getTblUsers().getSelectedRow();
        row = getTblUsers().convertRowIndexToModel(row);
        File user = new File(getUserFolder(), (String) usersModel.getRow(row)[0]);
        try {
            Key k = userManager.getMasterKey(user.getName(), new FileInputStream(user));
            if (k != null)
                setMasterKey(k, user);
        }
        catch (FileNotFoundException e1) {
            log.error("Error selecting masterkey from user", e1);
        }
    }

    @Action
    public void selectMasterKeyFromWallet(ActionEvent e) {
        setMasterKey(getWalletEncryptionKey(), null);
    }

    @Action
    public void selectKeyFolder(ActionEvent e) {
        File base = getKeyFolder() == null ? new File(".") : getKeyFolder();
        JFileChooser jfc = new JFileChooser(base);
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = jfc.showDialog(this, "Select");
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            setKeyFolder(file);
            fillFiles();
        }
    }

    @Action
    public void selectWalletFile(ActionEvent e) {
        File parent = getWalletFile();
        if (parent == null)
            parent = new File(".");
        else
            parent = parent.getParentFile();
        JFileChooser jfc = new JFileChooser(parent);
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int result = jfc.showDialog(this, "Select");
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            if (file.isDirectory())
                return;
            if (!file.exists()) {
                try {
                    file.createNewFile();
                }
                catch (IOException e1) {
                    log.error("Error creating walletfile", e1);
                }
            }
            setWalletFile(file);
            fillKeys();
        }
    }

    @Action
    public void encryptFile(ActionEvent e) {
        int keyRow = getTblKeys().getSelectedRow();
        keyRow = getTblKeys().convertRowIndexToModel(keyRow);
        Key key = (Key) keysModel.getRow(keyRow)[6];
        if (key == null)
            try {
                key = wallet.parseKey((String) keysModel.getRow(keyRow)[5]);
                keysModel.getRow(keyRow)[6] = key;
                keysModel.setValueAt(true, keyRow, 4);
            }
        catch (GeneralSecurityException e1) {
            log.error("Error parsing key", e1);
        }
        catch (ConfigurationException e1) {
            log.error("Error parsing key", e1);
        }
        catch (IOException e1) {
            log.error("Error parsing key", e1);
        }
        for (int row : getTblFiles().getSelectedRows()) {
            int r = getTblFiles().convertRowIndexToModel(row);
            Object[] rowData = filesModel.getRow(r);
            if ((Boolean) rowData[0])
                continue;
            File file = new File(getConfigFolder(), (String) rowData[1]);
            File newFile = new File(getConfigFolder(), (String) rowData[1] + ".temp");
            FileInputStream fis = null;
            FileOutputStream fos = null;
            try {
                fis = new FileInputStream(file);
                fos = new FileOutputStream(newFile, false);
                XXmlConfiguration.getCryptTool().encrypt(fis, key, fos, true);
                fis.close();
                fos.flush();
                fos.close();
                fis = new FileInputStream(newFile);
                fos = new FileOutputStream(file, false);
                int c = 0;
                while ((c = fis.read()) >= 0)
                    fos.write(c);
                fis.close();
                fos.flush();
                fos.close();
                filesModel.setValueAt(true, r, 0);
            }
            catch (Exception e1) {
                log.error("Error encrypting file", e1);
                if (newFile.exists())
                    newFile.delete();
            }
            finally {
                try {
                    fis.close();
                }
                catch (IOException e1) {}
                try {
                    fos.close();
                }
                catch (IOException e1) {}
                if (newFile.exists())
                    newFile.delete();
            }
        }
    }

    @Action
    public void decryptFile(ActionEvent e) {
        for (int r : getTblFiles().getSelectedRows()) {
            int row = getTblFiles().convertRowIndexToModel(r);
            Object[] rowData = filesModel.getRow(row);
            if (!(Boolean) rowData[0])
                return;
            List<Key> keys = new ArrayList<Key>();
            for (int i = 0; i < keysModel.getRowCount(); i++) {
                Object[] data = keysModel.getRow(i);
                Key key = (Key) data[6];
                if (key != null)
                    keys.add(key);
                else if ((Boolean) data[4]) {
                    try {
                        key = wallet.parseKey((String) data[5]);
                        keys.add(key);
                    }
                    catch (GeneralSecurityException e1) {
                        log.error("Error parsing key", e1);
                    }
                    catch (ConfigurationException e1) {
                        log.error("Error parsing key", e1);
                    }
                    catch (IOException e1) {
                        log.error("Error parsing key", e1);
                    }
                }
            }
            File file = new File(getConfigFolder(), (String) rowData[1]);
            File newFile = new File(getConfigFolder(), (String) rowData[1] + ".temp");
            FileInputStream fis = null;
            FileOutputStream fos = null;
            try {
                fis = new FileInputStream(file);
                fos = new FileOutputStream(newFile, false);
                Key key = XXmlConfiguration.getCryptTool().decrypt(new BufferedInputStream(new FileInputStream(file)), fos, keys);
                fis.close();
                fos.flush();
                fos.close();
                if (key == null) {
                    if (newFile.exists())
                        newFile.delete();
                    continue;
                }
                fis = new FileInputStream(newFile);
                fos = new FileOutputStream(file, false);
                int c = 0;
                while ((c = fis.read()) >= 0)
                    fos.write(c);
                fis.close();
                fos.flush();
                fos.close();
                filesModel.setValueAt(false, row, 0);
            }
            catch (Exception e1) {
                // log.error("Error decrypting file", e1);
                if (newFile.exists())
                    newFile.delete();
            }
            finally {
                try {
                    fis.close();
                }
                catch (IOException e1) {}
                try {
                    fos.close();
                }
                catch (IOException e1) {}
                if (newFile.exists())
                    newFile.delete();
            }
        }
    }

    private void fillKeys() {
        if (keysModel == null)
            return;

        int len = keysModel.getRowCount();
        for (int i = 0; i < len; i++) {
            keysModel.removeRow(0);
            keysModel.fireTableRowsDeleted(0, 0);
        }

        if (getKeyFolder() != null) {
            File[] keys = getKeyFolder().listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".key") || name.toLowerCase().endsWith(".priv") || name.toLowerCase().endsWith(".pub");
                }
            });
            for (File key : keys) {
                String value = null;
                try {
                    value = CryptTool.readComplete(new FileReader(key));
                }
                catch (FileNotFoundException e) {
                    log.error("Error reading Keyfile", e);
                }
                catch (IOException e) {
                    log.error("Error reading Keyfile", e);
                }
                String type = key.getName().substring(key.getName().lastIndexOf('.'));
                if (type.equals(".key"))
                    type = getResourceMap().getString("type.symetric");
                else if (type.equals(".priv"))
                    type = getResourceMap().getString("type.private");
                else if (type.equals(".pub"))
                    type = getResourceMap().getString("type.public");
                keysModel.addRow(new Object[] { isInWallet(value), type, key.getName(), XXmlConfiguration.getCryptTool().isEncrypted(value), !XXmlConfiguration.getCryptTool().isEncrypted(value),
                        value, null });
            }
        }
    }

    private boolean isInWallet(String data) {
        return wallet.exists(data);
    }

    private void fillFiles() {
        if (filesModel == null)
            return;

        int len = filesModel.getRowCount();
        for (int i = 0; i < len; i++) {
            filesModel.removeRow(0);
            filesModel.fireTableRowsDeleted(0, 0);
        }

        if (getConfigFolder() != null) {
            File[] files = getConfigFolder().listFiles(new java.io.FileFilter() {

                public boolean accept(File pathname) {
                    return pathname.isFile();
                }

            });
            for (File file : files) {
                Boolean encrypted = false;
                try {
                    encrypted = XXmlConfiguration.getCryptTool().isEncrypted(new BufferedInputStream(new FileInputStream(file)));
                }
                catch (FileNotFoundException e) {
                    log.error("Error reading file", e);
                }
                filesModel.addRow(new Object[] { encrypted, file.getName() });
            }
        }
    }

    private void fillUsers() {
        if (usersModel == null)
            return;

        int len = usersModel.getRowCount();
        for (int i = 0; i < len; i++) {
            usersModel.removeRow(0);
            usersModel.fireTableRowsDeleted(0, 0);
        }

        if (getUserFolder() != null) {
            File[] files = getUserFolder().listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.indexOf('.') < 0;
                }
            });
            if (files == null)
                return;
            for (File file : files) {
                try {
                    String infos = userManager.getUserInfos(file.getName(), new FileInputStream(file), getMasterKey());
                    Boolean valid = userManager.isValid(file.getName(), new FileInputStream(file), getMasterKey(), false);
                    usersModel.addRow(new Object[] { file.getName(), infos, valid });
                }
                catch (FileNotFoundException e1) {
                    log.error("Error filling user-table", e1);
                }
            }
        }
    }

    private void fillWallet() {
        if (walletModel == null)
            return;

        int len = walletModel.getRowCount();
        for (int i = 0; i < len; i++) {
            walletModel.removeRow(0);
            walletModel.fireTableRowsDeleted(0, 0);
        }

        if (getWalletFile() != null) {
            for (Map.Entry<String, String> entry : wallet.getEntries().entrySet())
                walletModel.addRow(new Object[] { entry.getKey(), entry.getValue() });
        }
    }

    public String getMessageKey() {
        return "SecurityManagerPanel";
    }

    public File getKeyFolder() {
        return keyFolder;
    }

    public void setKeyFolder(File keyFolder) {
        Object old = this.keyFolder;
        this.keyFolder = keyFolder;
        firePropertyChange("keyFolder", old, keyFolder);
        getConfig().getConfiguration("user").setProperty("keyFolder", keyFolder == null ? null : keyFolder.getAbsolutePath());
        if (userManager instanceof SecurityUserManager)
            ((SecurityUserManager) userManager).setKeyFolder(keyFolder);
    }

    public File getConfigFolder() {
        return configFolder;
    }

    public void setConfigFolder(File configFolder) {
        Object old = this.configFolder;
        this.configFolder = configFolder;
        firePropertyChange("configFolder", old, configFolder);
        getConfig().getConfiguration("user").setProperty("configFolder", configFolder == null ? null : configFolder.getAbsolutePath());
    }

    public File getUserFolder() {
        return userFolder;
    }

    public void setUserFolder(File userFolder) {
        Object old = this.userFolder;
        this.userFolder = userFolder;
        firePropertyChange("userFolder", old, userFolder);
        getConfig().getConfiguration("user").setProperty("userFolder", userFolder == null ? null : userFolder.getAbsolutePath());
        if (userManager instanceof SecurityUserManager)
            ((SecurityUserManager) userManager).setUserFolder(userFolder);
    }

    public File getWalletFile() {
        return walletFile;
    }

    public void setWalletFile(File walletFile) {
        if (walletFile == null)
            return;
        BufferedInputStream fis = null;
        try {
            if (!walletFile.exists())
                walletFile.createNewFile();
            fis = new BufferedInputStream(new FileInputStream(walletFile));
            if (XXmlConfiguration.getCryptTool().isEncrypted(fis)) {
                Key key = PasswordDialog.open(Application.getInstance(SingleFrameApplication.class).getMainFrame(), getResourceMap().getString("password2.message"), keyFolder, true);
                if (key == null)
                    return;
                else
                    setWalletEncryptionKey(key);

                XXmlConfiguration.getCryptTool().addKey(getWalletEncryptionKey());
                setWalletEncrypted(true);
            }
            else
                setWalletEncrypted(false);
        }
        catch (HeadlessException e) {
            log.error("Error creating key for wallet", e);
            return;
        }
        catch (FileNotFoundException e) {
            log.error("Error creating key for wallet", e);
            return;
        }
        catch (IOException e) {
            log.error("Error creating wallet file", e);
            return;
        }
        Object old = this.walletFile;
        this.walletFile = walletFile;
        firePropertyChange("walletFile", old, walletFile);
        if (!walletFile.exists()) {
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(walletFile);
                wallet.saveWallet(fos);
                fos.flush();
                fos.close();
            }
            catch (IOException e) {
                log.error("Error creating walletFile", e);
            }
        }
        wallet.loadWallet(fis);
        getConfig().getConfiguration("user").setProperty("walletFile", walletFile == null ? null : walletFile.getAbsolutePath());
    }

    public static class FileToStringConverter extends AbstractConverter {
        public String	defaultValue;

        public FileToStringConverter(ValueModel subject, String defaultValue) {
            super(subject);
            this.defaultValue = defaultValue;
        }

        @Override
        public Object convertFromSubject(Object obj) {
            return obj == null ? defaultValue : ((File) obj).getAbsolutePath();
        }

        public void setValue(Object obj) {
            if (obj.equals(defaultValue) || obj.equals(""))
                subject.setValue(null);
            else
                subject.setValue(new File((String) obj));

        }
    }

    // Generation 'KeyManager' START
    public org.jdesktop.swingx.JXTable getTblKeys() {
        return (org.jdesktop.swingx.JXTable) getView().getProperty("tblKeys");
    }

    public javax.swing.JButton getBtnAddKey() {
        return (javax.swing.JButton) getView().getProperty("btnAddKey");
    }

    public org.jdesktop.swingx.JXTable getTblFiles() {
        return (org.jdesktop.swingx.JXTable) getView().getProperty("tblFiles");
    }

    public com.jeta.forms.components.label.JETALabel getLblKeys() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("lblKeys");
    }

    public com.jeta.forms.components.label.JETALabel getLblFiles() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("lblFiles");
    }

    public com.jeta.forms.components.label.JETALabel getLblWalletFile() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("lblWalletFile");
    }

    public javax.swing.JButton getBtnWalletFile() {
        return (javax.swing.JButton) getView().getProperty("btnWalletFile");
    }

    public javax.swing.JButton getBtnAddAsymetricKey() {
        return (javax.swing.JButton) getView().getProperty("btnAddAsymetricKey");
    }

    public javax.swing.JButton getBtnRemoveKey() {
        return (javax.swing.JButton) getView().getProperty("btnRemoveKey");
    }

    public com.jeta.forms.components.label.JETALabel getLblWalletFileData() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("lblWalletFileData");
    }

    public com.jeta.forms.components.label.JETALabel getLblKeyFolder() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("lblKeyFolder");
    }

    public com.jeta.forms.components.label.JETALabel getLblConfigFolder() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("lblConfigFolder");
    }

    public com.jeta.forms.components.label.JETALabel getLblKeyFolderData() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("lblKeyFolderData");
    }

    public javax.swing.JButton getBtnConfigFolder() {
        return (javax.swing.JButton) getView().getProperty("btnConfigFolder");
    }

    public javax.swing.JButton getBtnKeyFolder() {
        return (javax.swing.JButton) getView().getProperty("btnKeyFolder");
    }

    public com.jeta.forms.components.label.JETALabel getLblMessages() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("lblMessages");
    }

    public javax.swing.JButton getBtnAuthorizeKey() {
        return (javax.swing.JButton) getView().getProperty("btnAuthorizeKey");
    }

    public javax.swing.JButton getBtnEncrypt() {
        return (javax.swing.JButton) getView().getProperty("btnEncrypt");
    }

    public javax.swing.JButton getBtnDecrypt() {
        return (javax.swing.JButton) getView().getProperty("btnDecrypt");
    }

    public javax.swing.JButton getBtnAddWallet() {
        return (javax.swing.JButton) getView().getProperty("btnAddWallet");
    }

    public javax.swing.JButton getBtnRemoveEntry() {
        return (javax.swing.JButton) getView().getProperty("btnRemoveEntry");
    }

    public javax.swing.JButton getBtnAddEntry() {
        return (javax.swing.JButton) getView().getProperty("btnAddEntry");
    }

    public javax.swing.JButton getBtnEditEntry() {
        return (javax.swing.JButton) getView().getProperty("btnEditEntry");
    }

    public javax.swing.JButton getBtnWalletEncrypted() {
        return (javax.swing.JButton) getView().getProperty("btnWalletEncrypted");
    }

    public com.jeta.forms.components.label.JETALabel getLblUserFolder() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("lblUserFolder");
    }

    public javax.swing.JButton getBtnUserFolder() {
        return (javax.swing.JButton) getView().getProperty("btnUserFolder");
    }

    public org.jdesktop.swingx.JXTable getTblWallet() {
        return (org.jdesktop.swingx.JXTable) getView().getProperty("tblWallet");
    }

    public javax.swing.JButton getBtnAddUser() {
        return (javax.swing.JButton) getView().getProperty("btnAddUser");
    }

    public javax.swing.JButton getBtnRemoveUser() {
        return (javax.swing.JButton) getView().getProperty("btnRemoveUser");
    }

    public javax.swing.JButton getBtnEditUser() {
        return (javax.swing.JButton) getView().getProperty("btnEditUser");
    }

    public com.jeta.forms.components.label.JETALabel getLblUserFolderData() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("lblUserFolderData");
    }

    public com.jeta.forms.components.label.JETALabel getLblConfigFolderData() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("lblConfigFolderData");
    }

    public org.jdesktop.swingx.JXTable getTblUsers() {
        return (org.jdesktop.swingx.JXTable) getView().getProperty("tblUsers");
    }

    public com.jeta.forms.components.label.JETALabel getLblMasterKey() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("lblMasterKey");
    }

    public com.jeta.forms.components.label.JETALabel getLblMasterKeyData() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("lblMasterKeyData");
    }

    public javax.swing.JButton getBtnMasterKey() {
        return (javax.swing.JButton) getView().getProperty("btnMasterKey");
    }

    public javax.swing.JButton getBtnMasterKeyFromUser() {
        return (javax.swing.JButton) getView().getProperty("btnMasterKeyFromUser");
    }

    public javax.swing.JButton getBtnRenameUser() {
        return (javax.swing.JButton) getView().getProperty("btnRenameUser");
    }

    public javax.swing.JButton getBtnMasterKeyFromWallet() {
        return (javax.swing.JButton) getView().getProperty("btnMasterKeyFromWallet");
    }

    public javax.swing.JButton getBtnUseAsMasterKey() {
        return (javax.swing.JButton) getView().getProperty("btnUseAsMasterKey");
    }
    // Generation 'KeyManager' END
}
