package org.aklein.swap.util;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.effect.BufferedImageOpEffect;
import org.jdesktop.jxlayer.plaf.effect.LayerEffect;
import org.jdesktop.jxlayer.plaf.ext.LockableUI;

import com.jhlabs.image.BoxBlurFilter;

public class JXLayerHelper<T extends JComponent> {

    private EnhancedLockableUI lockableUI;

    private JXLayer<JComponent> layer;

    private BufferedImageOpEffect[] effects;

    private T component;

    private static Log log = LogFactory.getLog(JXLayerHelper.class);

    public JXLayerHelper(T component, BufferedImageOpEffect... effects) {
        this.effects = effects;
        this.component = component;
    }

    public EnhancedLockableUI getLockableUI() {
        if (lockableUI == null)
            lockableUI = new EnhancedLockableUI();
        return lockableUI;
    }

    public BufferedImageOpEffect[] getEffects() {
        return effects;
    }

    public T getComponent() {
        return component;
    }

    public JXLayer<JComponent> getLayer() {
        if (layer == null) {
            layer = new JXLayer<JComponent>(component, getLockableUI());
            getLockableUI().setLockedEffects(effects);
        }
        return layer;
    }

    public static BufferedImageOpEffect getBlurEffect() {
        BoxBlurFilter blurFilter = new BoxBlurFilter(1, 1, 3);
        return new BufferedImageOpEffect(blurFilter);
    }

    public boolean isLocked() {
        return getLockableUI().isLocked();
    }

    public void setLocked(final boolean flag) {
        try {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        getLockableUI().setLocked(flag);
                    }
                });
            } else
                getLockableUI().setLocked(flag);
        } catch (Exception e) {
            log.error("Error " + (flag ? "locking" : "unlocking") + " component: " + component.getName() + " - " + component.getClass(), e);
        }
    }

    /**
     * @return the displayComponent
     */
    public JComponent getDisplayComponent() {
        return getLockableUI().getComponent();
    }

    /**
     * @param displayComponent the displayComponent to set
     */
    public void setDisplayComponent(JComponent displayComponent) {
        getLockableUI().setComponent(displayComponent);
    }

    protected class EnhancedLockableUI extends LockableUI {
        private JComponent component;

        private JXLayer<JComponent> layer;

        public EnhancedLockableUI(LayerEffect... lockedEffects) {
            super(lockedEffects);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void installUI(JComponent c) {
            super.installUI(c);
            layer = (JXLayer<JComponent>) c;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void uninstallUI(JComponent c) {
            super.uninstallUI(c);
            layer = null;
        }

        @Override
        public void setLocked(boolean isLocked) {
            super.setLocked(isLocked);
            if (component != null)
                component.setVisible(isLocked);
        }

        /**
         * @return the component
         */
        public JComponent getComponent() {
            return component;
        }

        /**
         * @param component the component to set
         */
        public void setComponent(JComponent component) {
            Object old = this.component;
            this.component = component;
            firePropertyChange("component", old, component);
            if (component != null) {
                layer.getGlassPane().setLayout(new GridBagLayout());
                layer.getGlassPane().add(component);
                component.setCursor(Cursor.getDefaultCursor());
                component.setVisible(false);
            } else {
                layer.getGlassPane().removeAll();
                layer.getGlassPane().setLayout(new FlowLayout());
            }
        }
    }
}
