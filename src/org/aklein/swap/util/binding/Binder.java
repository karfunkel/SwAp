package org.aklein.swap.util.binding;

import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.adapter.ColorSelectionAdapter;
import com.jgoodies.binding.adapter.ComboBoxAdapter;
import com.jgoodies.binding.adapter.SingleListSelectionAdapter;
import com.jgoodies.binding.adapter.SpinnerToValueModelConnector;
import com.jgoodies.binding.beans.PropertyConnector;
import com.jgoodies.binding.formatter.EmptyDateFormatter;
import com.jgoodies.binding.formatter.EmptyNumberFormatter;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ConverterFactory;
import com.jgoodies.binding.value.ValueModel;

import com.jgoodies.validation.view.ValidationComponentUtils;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.table.TableColumnExtWrapper;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

import java.awt.Color;

import java.beans.PropertyChangeListener;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;

import java.util.Calendar;
import java.util.Date;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.InternationalFormatter;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;


/**
 * Consists only of static methods that bind and mend frequently used Swing components using a given ValueModel. <br/>
 * Additionally, a validation listener set by
 * {@link org.aklein.swap.util.binding.Binder#setValidationListener(PropertyChangeListener)} will be registered along
 * with any binding ut the generic bind.
 *
 * @author Alexander Klein
 * @see com.jgoodies.binding.value.ValueModel
 * @see com.jgoodies.binding.adapter.Bindings
 * @see com.jgoodies.binding.adapter.BasicComponentFactory
 * @deprecated replaced by SwingBinder and SwingXBinder
 */
@Deprecated
public class Binder {
    private static PropertyChangeListener validationListener;

    protected Binder() {
        // Reduce the visibility of the default constructor.
    }

    /**
     * get the actually registered validation listener
     *
     * @return
     */
    public static PropertyChangeListener getValidationListener() {
        return validationListener;
    }

    /**
     * set the validation listener used by the next bindings.
     *
     * @param validationListener
     */
    public static void setValidationListener(
        PropertyChangeListener validationListener) {
        Binder.validationListener = validationListener;
    }

    /**
     * Binds a check box to the given ValueModel. The check box is selected if and only if the model's value equals
     * <code>Boolean.TRUE</code>.
     * <p>
     * The model is converted to the required ToggleButtonModel using a ToggleButtonAdapter.
     */
    public static void bindCheckBox(JCheckBox checkBox, ValueModel valueModel) {
        Bindings.bind(checkBox, valueModel);

        if (validationListener != null) {
            valueModel.addValueChangeListener(validationListener);
        }
    }

    /**
     * Binds a non-editable JComboBox that is bound to the given SelectionInList. The SelectionInList's ListModel is the
     * list data provider and the selection index holder is used for the combo box model's selected item.
     * <p>
     * There are a couple of other possibilities to bind a JComboBox. See the constructors and the class comment of the
     * {@link ComboBoxAdapter}.
     *
     * @see ComboBoxAdapter
     */
    public static void bindComboBox(JComboBox comboBox,
        SelectionInList<?> selectionInList) {
        bindComboBox(comboBox, selectionInList, null);
    }

    /**
     * Binds a non-editable JComboBox that is bound to the given SelectionInList using the given cell renderer. The
     * SelectionInList provides the list data and the selection index holder is used for the combo box model's selected
     * item.
     * <p>
     * There are a couple of other possibilities to bind a JComboBox. See the constructors and the class comment of the
     * {@link ComboBoxAdapter}.
     *
     * @see ComboBoxAdapter
     */
    public static void bindComboBox(JComboBox comboBox,
        SelectionInList<?> selectionInList, ListCellRenderer cellRenderer) {
        Bindings.bind(comboBox, selectionInList);

        if (validationListener != null) {
            selectionInList.addValueChangeListener(validationListener);
        }

        if (cellRenderer != null) {
            comboBox.setRenderer(cellRenderer);
        }
    }

    /**
     * Creates and returns a JColorChooser that has the color selection bound to the given ValueModel. The ValueModel
     * must be of type Color and must allow read-access to its value, and the initial value must not be {@code null}.
     * <p>
     * It is strongly recommended (though not required) that the underlying ValueModel provides only non-null values.
     * This is so because the ColorSelectionModel behavior is undefined for {@code null} values and it may have
     * unpredictable results. To avoid these problems, you may create the ColorChooser with a default color using
     * {@link #createColorChooser(ValueModel, Color)}.
     *
     * @param valueModel a Color-typed ValueModel
     * @return a color chooser with the selected color bound to the given model
     * @throws NullPointerException if the valueModel is {@code null}, or if its initial value is {@code null}
     * @see #createColorChooser(ValueModel, Color)
     * @since 1.0.3
     */
    public static JColorChooser bindColorChooser(JColorChooser colorChooser,
        ValueModel valueModel) {
        if (valueModel.getValue() == null) {
            throw new NullPointerException(
                "The initial value must not be null.");
        }

        colorChooser.setSelectionModel(new ColorSelectionAdapter(valueModel));

        // Due to a bug in Java 1.4.2, Java 5 and Java 6, we don't use
        // the Bindings class, but provide a ColorSelectionModel at
        // instance creation time. The bug is in BasicColorChooserUI
        // that doesn't listen to color selection model changes.
        // This is required to update the color preview panel.
        // But the BasicColorChooserUI registers a preview listener
        // with the initial color selection model.
        // Bindings.bind(colorChooser, valueModel);
        return colorChooser;
    }

    /**
     * Mends a JColorChooser that has the color selection bound to the given ValueModel. The ValueModel must be of type
     * Color and must allow read-access to its value. If the valueModel returns {@code null}, the given default color is
     * used instead. This avoids problems with the ColorSelectionModel that may have unpredictable result for {@code
     * null} values.
     *
     * @param valueModel a Color-typed ValueModel
     * @param defaultColor the color used if the valueModel returns null
     * @return a color chooser with the selected color bound to the given model
     * @throws NullPointerException if the valueModel or the default color is {@code null},
     * @since 1.1
     */
    public static JColorChooser bindColorChooser(JColorChooser colorChooser,
        ValueModel valueModel, Color defaultColor) {
        if (defaultColor == null) {
            throw new NullPointerException(
                "The default color must not be null.");
        }

        colorChooser.setSelectionModel(new ColorSelectionAdapter(valueModel,
                defaultColor));

        // Due to a bug in Java 1.4.2, Java 5 and Java 6, we don't use
        // the Bindings class, but provide a ColorSelectionModel at
        // instance creation time. The bug is in BasicColorChooserUI
        // that doesn't listen to color selection model changes.
        // This is required to update the color preview panel.
        // But the BasicColorChooserUI registers a preview listener
        // with the initial color selection model.
        // Bindings.bind(colorChooser, valueModel);
        return colorChooser;
    }

    /**
     * Mends a formatted text field so that it is bound to the Date value of the given ValueModel. The
     * JFormattedTextField is configured with an AbstractFormatter that uses two different DateFormats to edit and
     * display the Date. A <code>SHORT</code> DateFormat with strict checking is used to edit (parse) a date; the
     * DateFormatter's default DateFormat is used to display (format) a date. In both cases <code>null</code> Dates are
     * mapped to the empty String.
     */
    public static void bindDateField(JFormattedTextField textField,
        ValueModel valueModel) {
        DateFormat shortFormat = DateFormat.getDateInstance(DateFormat.SHORT);
        shortFormat.setLenient(false);

        JFormattedTextField.AbstractFormatter defaultFormatter = new EmptyDateFormatter(shortFormat);
        JFormattedTextField.AbstractFormatter displayFormatter = new EmptyDateFormatter();

        DefaultFormatterFactory formatterFactory = new DefaultFormatterFactory(defaultFormatter,
                displayFormatter);
        bindFormattedTextField(textField, valueModel, formatterFactory);

        if (validationListener != null) {
            valueModel.addValueChangeListener(validationListener);
        }
    }

    /**
     * Returns an AbstractFormatterFactory suitable for the passed in Object type.
     */
    private static JFormattedTextField.AbstractFormatterFactory getDefaultFormatterFactory(
        Object type) {
        if (type instanceof DateFormat) {
            return new DefaultFormatterFactory(new DateFormatter(
                    (DateFormat) type));
        }

        if (type instanceof NumberFormat) {
            return new DefaultFormatterFactory(new NumberFormatter(
                    (NumberFormat) type));
        }

        if (type instanceof Format) {
            return new DefaultFormatterFactory(new InternationalFormatter(
                    (Format) type));
        }

        if (type instanceof Date) {
            return new DefaultFormatterFactory(new DateFormatter());
        }

        if (type instanceof Number) {
            AbstractFormatter displayFormatter = new NumberFormatter();
            ((NumberFormatter) displayFormatter).setValueClass(type.getClass());

            AbstractFormatter editFormatter = new NumberFormatter(new DecimalFormat(
                        "#.#"));
            ((NumberFormatter) editFormatter).setValueClass(type.getClass());

            return new DefaultFormatterFactory(displayFormatter,
                displayFormatter, editFormatter);
        }

        return new DefaultFormatterFactory(new DefaultFormatter());
    }

    public static void bindFormattedTextField(JFormattedTextField textField,
        ValueModel valueModel, Format format) {
        bindFormattedTextField(textField, valueModel, format, true);
    }

    /**
     * Binds a formatted text field to the given model and converts Strings to values using the given Format.
     */
    public static void bindFormattedTextField(JFormattedTextField textField,
        ValueModel valueModel, Format format, boolean commitOnFocusLost) {
        JFormattedTextField.AbstractFormatterFactory formatter = getDefaultFormatterFactory(format);
        bindFormattedTextField(textField, valueModel, formatter,
            commitOnFocusLost);
    }

    public static void bindFormattedTextField(JFormattedTextField textField,
        ValueModel valueModel, JFormattedTextField.AbstractFormatter formatter) {
        bindFormattedTextField(textField, valueModel, formatter, true);
    }

    /**
     * Binds a formatted text field to the given model and converts Strings to values using the given Formatter.
     */
    public static void bindFormattedTextField(JFormattedTextField textField,
        ValueModel valueModel, JFormattedTextField.AbstractFormatter formatter,
        boolean commitOnFocusLost) {
        if (formatter != null) {
            formatter.install(textField);
        }

        Bindings.bind(textField, valueModel, commitOnFocusLost);

        if (validationListener != null) {
            valueModel.addValueChangeListener(validationListener);
        }
    }

    public static void bindFormattedTextField(JFormattedTextField textField,
        ValueModel valueModel,
        JFormattedTextField.AbstractFormatterFactory formatterFactory) {
        bindFormattedTextField(textField, valueModel, formatterFactory, true);
    }

    /**
     * Binds a formatted text field that binds to the given model and converts Strings to values using the given
     * Formatter.
     */
    public static void bindFormattedTextField(JFormattedTextField textField,
        ValueModel valueModel,
        JFormattedTextField.AbstractFormatterFactory formatterFactory,
        boolean commitOnFocusLost) {
        JFormattedTextField.AbstractFormatter formatter = formatterFactory.getFormatter(textField);
        bindFormattedTextField(textField, valueModel, formatter,
            commitOnFocusLost);
    }

    public static void bindFormattedTextField(JFormattedTextField textField,
        ValueModel valueModel, String mask) {
        bindFormattedTextField(textField, valueModel, mask, true);
    }

    /**
     * Mends and binds a formatted text field to the given model and converts Strings to values using a
     * {@link MaskFormatter} that is based on the given mask.
     */
    public static void bindFormattedTextField(JFormattedTextField textField,
        ValueModel valueModel, String mask, boolean commitOnFocusLost) {
        MaskFormatter formatter = null;

        try {
            formatter = new MaskFormatter(mask);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid mask '" + mask + "'.");
        }

        bindFormattedTextField(textField, valueModel, formatter,
            commitOnFocusLost);
    }

    // Integer Fields
    // *********************************************************
    public static void bindIntegerField(JFormattedTextField textField,
        ValueModel valueModel) {
        bindIntegerField(textField, valueModel, true);
    }

    /**
     * Creates and returns a formatted text field that is bound to the Integer value of the given ValueModel. Empty
     * strings are converted to <code>null</code> and vice versa.
     * <p>
     * The Format used to convert numbers to strings and vice versa is <code>NumberFormat.getIntegerInstance()</code>.
     *
     * @param valueModel the model that holds the value to be edited
     * @return a formatted text field for Integer instances that is bound to the specified valueModel
     * @throws NullPointerException if the valueModel is <code>null</code>
     */
    public static void bindIntegerField(JFormattedTextField textField,
        ValueModel valueModel, boolean commitOnFocusLost) {
        bindIntegerField(textField, valueModel,
            NumberFormat.getIntegerInstance(), 0, commitOnFocusLost);
    }

    public static void bindIntegerField(JFormattedTextField textField,
        ValueModel valueModel, int emptyNumber) {
        bindIntegerField(textField, valueModel, emptyNumber, true);
    }

    /**
     * Creates and returns a formatted text field that is bound to the Integer value of the given ValueModel. Empty
     * strings are converted to the specified empty number.
     * <p>
     * The Format used to convert numbers to strings and vice versa is <code>NumberFormat.getIntegerInstance()</code>.
     *
     * @param valueModel the model that holds the value to be edited
     * @param emptyNumber an Integer that represents the empty string
     * @return a formatted text field for Integer instances that is bound to the specified valueModel
     * @throws NullPointerException if the valueModel is <code>null</code>
     */
    public static void bindIntegerField(JFormattedTextField textField,
        ValueModel valueModel, int emptyNumber, boolean commitOnFocusLost) {
        bindIntegerField(textField, valueModel,
            NumberFormat.getIntegerInstance(), emptyNumber, commitOnFocusLost);
    }

    public static void bindIntegerField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat) {
        bindIntegerField(textField, valueModel, numberFormat, true);
    }

    /**
     * Binds a formatted text field to the Integer value of the given ValueModel. Empty strings are converted to
     * <code>null</code> and vice versa.
     */
    public static void bindIntegerField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat,
        boolean commitOnFocusLost) {
        bindIntegerField(textField, valueModel, numberFormat, 0,
            commitOnFocusLost);
    }

    public static void bindIntegerField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, int emptyNumber) {
        bindIntegerField(textField, valueModel, numberFormat, emptyNumber, true);
    }

    /**
     * Binds a formatted text field to the Integer value of the given ValueModel. Empty strings are converted to the
     * specified empty number.
     */
    public static void bindIntegerField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, int emptyNumber,
        boolean commitOnFocusLost) {
        bindIntegerField(textField, valueModel, numberFormat,
            Integer.valueOf(emptyNumber), commitOnFocusLost);
    }

    public static void bindIntegerField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, Integer emptyNumber) {
        bindIntegerField(textField, valueModel, numberFormat, emptyNumber, true);
    }

    /**
     * Binds a formatted text field to the Integer value of the given ValueModel. Empty strings are converted to the
     * specified empty number.
     */
    public static void bindIntegerField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, Integer emptyNumber,
        boolean commitOnFocusLost) {
        NumberFormatter numberFormatter = new EmptyNumberFormatter(numberFormat,
                emptyNumber);
        numberFormatter.setValueClass(Integer.class);
        valueModel = new StringConverter(valueModel, numberFormatter);
        textField.putClientProperty("Formatter", numberFormatter);
        textField.setFormatterFactory(new AbstractFormatterFactory() {
                @Override
                public AbstractFormatter getFormatter(JFormattedTextField tf) {
                    return (AbstractFormatter) tf.getClientProperty("Formatter");
                }
            });
        textField.setInputVerifier(new BindingInputVerifier());
        textField.setFocusLostBehavior(JFormattedTextField.COMMIT);

        String key = (String) ValidationComponentUtils.getMessageKey(textField);

        if (key == null) {
            key = textField.hashCode() + "";
        }

        ValidationComponentUtils.setMandatory(textField, false);
        ValidationComponentUtils.setMessageKey(textField, key);
        ValidationComponentUtils.setInputHint(textField, "Ganzzahl");
        bindFormattedTextField(textField, valueModel, numberFormatter,
            commitOnFocusLost);
    }

    // Long Fields
    // ************************************************************
    public static void bindLongField(JFormattedTextField textField,
        ValueModel valueModel) {
        bindLongField(textField, valueModel, true);
    }

    /**
     * Binds a formatted text field to the Long value of the given ValueModel. Empty strings are converted to
     * <code>null</code> and vice versa.
     * <p>
     * The Format used to convert numbers to strings and vice versa is <code>NumberFormat.getIntegerInstance()</code>.
     */
    public static void bindLongField(JFormattedTextField textField,
        ValueModel valueModel, boolean commitOnFocusLost) {
        bindLongField(textField, valueModel, NumberFormat.getIntegerInstance(),
            0, commitOnFocusLost);
    }

    public static void bindLongField(JFormattedTextField textField,
        ValueModel valueModel, long emptyNumber) {
        bindLongField(textField, valueModel, emptyNumber, true);
    }

    /**
     * Binds a formatted text field to the Long value of the given ValueModel. Empty strings are converted to the
     * specified empty number.
     * <p>
     * The Format used to convert numbers to strings and vice versa is <code>NumberFormat.getIntegerInstance()</code>.
     */
    public static void bindLongField(JFormattedTextField textField,
        ValueModel valueModel, long emptyNumber, boolean commitOnFocusLost) {
        bindLongField(textField, valueModel, NumberFormat.getIntegerInstance(),
            emptyNumber, commitOnFocusLost);
    }

    public static void bindLongField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat) {
        bindLongField(textField, valueModel, numberFormat, true);
    }

    /**
     * Binds a formatted text field to the Long value of the given ValueModel. Empty strings are converted to
     * <code>null</code> and vice versa.
     */
    public static void bindLongField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat,
        boolean commitOnFocusLost) {
        bindLongField(textField, valueModel, numberFormat, 0, commitOnFocusLost);
    }

    public static void bindLongField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, long emptyNumber) {
        bindLongField(textField, valueModel, numberFormat, emptyNumber, true);
    }

    /**
     * Binds a formatted text field to the Long value of the given ValueModel. Empty strings are converted to the
     * specified empty number.
     */
    public static void bindLongField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, long emptyNumber,
        boolean commitOnFocusLost) {
        bindLongField(textField, valueModel, numberFormat,
            Long.valueOf(emptyNumber), commitOnFocusLost);
    }

    public static void bindLongField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, Long emptyNumber) {
        bindLongField(textField, valueModel, numberFormat, emptyNumber, true);
    }

    /**
     * Binds a formatted text field to the Long value of the given ValueModel. Empty strings are converted to the
     * specified empty number.
     */
    public static void bindLongField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, Long emptyNumber,
        boolean commitOnFocusLost) {
        NumberFormatter numberFormatter = new EmptyNumberFormatter(numberFormat,
                emptyNumber);
        numberFormatter.setValueClass(Long.class);
        valueModel = new StringConverter(valueModel, numberFormatter);
        textField.putClientProperty("Formatter", numberFormatter);
        textField.setFormatterFactory(new AbstractFormatterFactory() {
                @Override
                public AbstractFormatter getFormatter(JFormattedTextField tf) {
                    return (AbstractFormatter) tf.getClientProperty("Formatter");
                }
            });
        textField.setInputVerifier(new BindingInputVerifier());
        textField.setFocusLostBehavior(JFormattedTextField.COMMIT);

        String key = (String) ValidationComponentUtils.getMessageKey(textField);

        if (key == null) {
            key = textField.hashCode() + "";
        }

        ValidationComponentUtils.setMandatory(textField, false);
        ValidationComponentUtils.setMessageKey(textField, key);
        ValidationComponentUtils.setInputHint(textField, "Ganzzahl");
        bindFormattedTextField(textField, valueModel, numberFormatter,
            commitOnFocusLost);
    }

    // ************************************************************************

    // Double Fields
    // ************************************************************
    public static void bindDoubleField(JFormattedTextField textField,
        ValueModel valueModel, int maxFractions) {
        bindDoubleField(textField, valueModel, maxFractions, true);
    }

    /**
     * Binds a formatted text field to the Double value of the given ValueModel. Empty strings are converted to
     * <code>null</code> and vice versa.
     * <p>
     * The Format used to convert numbers to strings and vice versa is <code>NumberFormat.getIntegerInstance()</code>.
     */
    public static void bindDoubleField(JFormattedTextField textField,
        ValueModel valueModel, int maxFractions, boolean commitOnFocusLost) {
        bindDoubleField(textField, valueModel,
            NumberFormat.getNumberInstance(), 0.0, maxFractions,
            commitOnFocusLost);
    }

    public static void bindDoubleField(JFormattedTextField textField,
        ValueModel valueModel, double emptyNumber, int maxFractions) {
        bindDoubleField(textField, valueModel, emptyNumber, maxFractions, true);
    }

    /**
     * Binds a formatted text field to the Double value of the given ValueModel. Empty strings are converted to the
     * specified empty number.
     * <p>
     * The Format used to convert numbers to strings and vice versa is <code>NumberFormat.getIntegerInstance()</code>.
     */
    public static void bindDoubleField(JFormattedTextField textField,
        ValueModel valueModel, double emptyNumber, int maxFractions,
        boolean commitOnFocusLost) {
        bindDoubleField(textField, valueModel,
            NumberFormat.getNumberInstance(), emptyNumber, maxFractions,
            commitOnFocusLost);
    }

    public static void bindDoubleField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, int maxFractions) {
        bindDoubleField(textField, valueModel, numberFormat, maxFractions, true);
    }

    /**
     * Binds a formatted text field to the Double value of the given ValueModel. Empty strings are converted to
     * <code>null</code> and vice versa.
     */
    public static void bindDoubleField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, int maxFractions,
        boolean commitOnFocusLost) {
        bindDoubleField(textField, valueModel, numberFormat, 0.0, maxFractions,
            commitOnFocusLost);
    }

    public static void bindDoubleField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, double emptyNumber,
        int maxFractions) {
        bindDoubleField(textField, valueModel, numberFormat, emptyNumber,
            maxFractions, true);
    }

    /**
     * Binds a formatted text field to the Double value of the given ValueModel. Empty strings are converted to the
     * specified empty number.
     */
    public static void bindDoubleField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, double emptyNumber,
        int maxFractions, boolean commitOnFocusLost) {
        bindDoubleField(textField, valueModel, numberFormat,
            new Double(emptyNumber), maxFractions, commitOnFocusLost);
    }

    public static void bindDoubleField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, Double emptyNumber,
        int maxFractions) {
        bindDoubleField(textField, valueModel, numberFormat, emptyNumber,
            maxFractions, true);
    }

    /**
     * Binds a formatted text field to the Double value of the given ValueModel. Empty strings are converted to the
     * specified empty number.
     */
    public static void bindDoubleField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, Double emptyNumber,
        int maxFractions, boolean commitOnFocusLost) {
        numberFormat.setMaximumFractionDigits(maxFractions);

        NumberFormatter numberFormatter = new EmptyNumberFormatter(numberFormat,
                emptyNumber);
        numberFormatter.setValueClass(Double.class);
        valueModel = new StringConverter(valueModel, numberFormatter);
        textField.putClientProperty("Formatter", numberFormatter);
        textField.setFormatterFactory(new AbstractFormatterFactory() {
                @Override
                public AbstractFormatter getFormatter(JFormattedTextField tf) {
                    return (AbstractFormatter) tf.getClientProperty("Formatter");
                }
            });
        textField.setInputVerifier(new BindingInputVerifier());
        textField.setFocusLostBehavior(JFormattedTextField.COMMIT);

        String key = (String) ValidationComponentUtils.getMessageKey(textField);

        if (key == null) {
            key = textField.hashCode() + "";
        }

        ValidationComponentUtils.setMandatory(textField, false);
        ValidationComponentUtils.setMessageKey(textField, key);
        ValidationComponentUtils.setInputHint(textField, "Flieﬂkommazahl");
        bindFormattedTextField(textField, valueModel, numberFormatter,
            commitOnFocusLost);
    }

    // ************************************************************************

    // Currency Fields
    // ************************************************************
    public static void bindCurrencyField(JFormattedTextField textField,
        ValueModel valueModel, int maxFractions) {
        bindCurrencyField(textField, valueModel, maxFractions, true);
    }

    /**
     * Binds a formatted text field to the Double value of the given ValueModel. Empty strings are converted to
     * <code>null</code> and vice versa.
     * <p>
     * The Format used to convert numbers to strings and vice versa is <code>NumberFormat.getIntegerInstance()</code>.
     */
    public static void bindCurrencyField(JFormattedTextField textField,
        ValueModel valueModel, int maxFractions, boolean commitOnFocusLost) {
        bindCurrencyField(textField, valueModel,
            NumberFormat.getCurrencyInstance(), 0, maxFractions,
            commitOnFocusLost);
    }

    public static void bindCurrencyField(JFormattedTextField textField,
        ValueModel valueModel, double emptyNumber, int maxFractions) {
        bindCurrencyField(textField, valueModel, emptyNumber, maxFractions, true);
    }

    /**
     * Binds a formatted text field to the Double value of the given ValueModel. Empty strings are converted to the
     * specified empty number.
     * <p>
     * The Format used to convert numbers to strings and vice versa is <code>NumberFormat.getIntegerInstance()</code>.
     */
    public static void bindCurrencyField(JFormattedTextField textField,
        ValueModel valueModel, double emptyNumber, int maxFractions,
        boolean commitOnFocusLost) {
        bindCurrencyField(textField, valueModel,
            NumberFormat.getCurrencyInstance(), emptyNumber, maxFractions,
            commitOnFocusLost);
    }

    public static void bindCurrencyField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, int maxFractions) {
        bindCurrencyField(textField, valueModel, numberFormat, maxFractions,
            true);
    }

    /**
     * Binds a formatted text field to the Double value of the given ValueModel. Empty strings are converted to
     * <code>null</code> and vice versa.
     */
    public static void bindCurrencyField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, int maxFractions,
        boolean commitOnFocusLost) {
        bindCurrencyField(textField, valueModel, numberFormat, 0, maxFractions,
            commitOnFocusLost);
    }

    public static void bindCurrencyField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, double emptyNumber,
        int maxFractions) {
        bindCurrencyField(textField, valueModel, numberFormat, emptyNumber,
            maxFractions, true);
    }

    /**
     * Binds a formatted text field to the Double value of the given ValueModel. Empty strings are converted to the
     * specified empty number.
     */
    public static void bindCurrencyField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, double emptyNumber,
        int maxFractions, boolean commitOnFocusLost) {
        bindCurrencyField(textField, valueModel, numberFormat,
            new Double(emptyNumber), maxFractions, commitOnFocusLost);
    }

    public static void bindCurrencyField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, Double emptyNumber,
        int maxFractions) {
        bindCurrencyField(textField, valueModel, numberFormat, emptyNumber,
            maxFractions, true);
    }

    /**
     * Binds a formatted text field to the Double value of the given ValueModel. Empty strings are converted to the
     * specified empty number.
     */
    public static void bindCurrencyField(JFormattedTextField textField,
        ValueModel valueModel, NumberFormat numberFormat, Double emptyNumber,
        int maxFractions, boolean commitOnFocusLost) {
        numberFormat.setMaximumFractionDigits(maxFractions);

        final NumberFormatter numberFormatter = new EmptyNumberFormatter(numberFormat,
                emptyNumber);
        numberFormatter.setValueClass(Double.class);
        valueModel = new StringConverter(valueModel, numberFormatter);
        textField.setFormatterFactory(new AbstractFormatterFactory() {
                @Override
                public AbstractFormatter getFormatter(JFormattedTextField tf) {
                    return numberFormatter;
                }
            });
        textField.setInputVerifier(new BindingInputVerifier());
        textField.setFocusLostBehavior(JFormattedTextField.COMMIT);

        String key = (String) ValidationComponentUtils.getMessageKey(textField);

        if (key == null) {
            key = textField.hashCode() + "";
        }

        ValidationComponentUtils.setMandatory(textField, false);
        ValidationComponentUtils.setMessageKey(textField, key);
        ValidationComponentUtils.setInputHint(textField, "W‰hrungsfeld");
        bindFormattedTextField(textField, valueModel, numberFormatter,
            commitOnFocusLost);
    }

    /**
     * Binds a text label to the given ValueModel.
     */
    public static void bindLabel(JLabel label, ValueModel valueModel) {
        Bindings.bind(label, valueModel);

        if (validationListener != null) {
            valueModel.addValueChangeListener(validationListener);
        }
    }

    /**
     * Binds a text label to the given ValueModel that is wrapped by a <code>StringConverter</code>. The conversion to
     * Strings uses the specified Format.
     *
     * @see ConverterFactory
     */
    public static void bindLabel(JLabel label, ValueModel valueModel,
        Format format) {
        bindLabel(label,
            ConverterFactory.createStringConverter(valueModel, format));
    }

    /**
     * Binds a JList to the given SelectionInList.
     */
    public static void bindList(JList list, SelectionInList selectionInList) {
        bindList(list, selectionInList, null);
    }

    /**
     * Binds a JList to the given SelectionInList using the specified optional ListCellRenderer to render cells.
     */
    public static void bindList(JList list, SelectionInList<?> selectionInList,
        ListCellRenderer cellRenderer) {
        Bindings.bind(list, selectionInList);

        if (validationListener != null) {
            selectionInList.addValueChangeListener(validationListener);
        }

        if (cellRenderer != null) {
            list.setCellRenderer(cellRenderer);
        }
    }

    /**
     * Binds a JPasswordField with the content bound to the given ValueModel. Text changes are committed to the model on
     * focus lost.
     * <p>
     * <strong>Security Note: </strong> The binding created by this method uses Strings as values of the given
     * ValueModel. The String-typed passwords could potentially be observed in a security fraud. For stronger security
     * it is recommended to request a character array from the JPasswordField and clear the array after use by setting
     * each character to zero. Method {@link JPasswordField#getPassword()} return's the field's password as a character
     * array.
     *
     * @see #createPasswordField(ValueModel, boolean)
     * @see JPasswordField#getPassword()
     */
    public static void bindPasswordField(JPasswordField textField,
        ValueModel valueModel) {
        bindPasswordField(textField, valueModel, true);
    }

    /**
     * Binds a JPasswordField with the content bound to the given ValueModel. Text changes can be committed to the model
     * on focus lost or on every character typed.
     * <p>
     * <strong>Security Note: </strong> The binding created by this method uses Strings as values of the given
     * ValueModel. The String-typed passwords could potentially be observed in a security fraud. For stronger security
     * it is recommended to request a character array from the JPasswordField and clear the array after use by setting
     * each character to zero. Method {@link JPasswordField#getPassword()} return's the field's password as a character
     * array.
     *
     * @see #createPasswordField(ValueModel)
     * @see JPasswordField#getPassword()
     */
    public static void bindPasswordField(JPasswordField textField,
        ValueModel valueModel, boolean commitOnFocusLost) {
        Bindings.bind(textField, valueModel, commitOnFocusLost);

        if (validationListener != null) {
            valueModel.addValueChangeListener(validationListener);
        }
    }

    /**
     * Binds a radio button with the specified text label that is bound to the given ValueModel. The radio button is
     * selected if and only if the model's value equals the specified choice.
     * <p>
     * The model is converted to the required ToggleButton using a RadioButtonAdapter.
     */
    public static void bindRadioButton(JRadioButton radioButton,
        ValueModel model, Object choice) {
        Bindings.bind(radioButton, model, choice);

        if (validationListener != null) {
            model.addValueChangeListener(validationListener);
        }
    }

    /**
     * Binds a text area with the content bound to the given ValueModel. Text changes are committed to the model on
     * focus lost.
     *
     * @see #createTextArea(ValueModel, boolean)
     */
    public static void bindTextArea(JTextArea textArea, ValueModel valueModel) {
        bindTextArea(textArea, valueModel, true);
    }

    /**
     * Binds a text area with the content bound to the given ValueModel. Text changes can be committed to the model on
     * focus lost or on every character typed.
     *
     * @see #createTextArea(ValueModel)
     */
    public static void bindTextArea(JTextArea textArea, ValueModel valueModel,
        boolean commitOnFocusLost) {
        Bindings.bind(textArea, valueModel, commitOnFocusLost);

        if (validationListener != null) {
            valueModel.addValueChangeListener(validationListener);
        }
    }

    /**
     * Binds a text field with the content bound to the given ValueModel. Text changes are comitted to the model on
     * focus lost.
     *
     * @see #createTextField(ValueModel, boolean)
     */
    public static void bindTextField(JTextField textField, ValueModel valueModel) {
        bindTextField(textField, valueModel, true);
    }

    /**
     * Binds a text field with the content bound to the given ValueModel. Text changes can be committed to the model on
     * focus lost or on every character typed.
     *
     * @see #bindTextField(ValueModel)
     */
    public static void bindTextField(JTextField textField,
        ValueModel valueModel, boolean commitOnFocusLost) {
        Bindings.bind(textField, valueModel, commitOnFocusLost);

        if (validationListener != null) {
            valueModel.addValueChangeListener(validationListener);
        }
    }

    /**
     * Binds a JSpinner with the content bound to the given ValueModel. This expects the data to be an Integer.
     *
     * @see #bindJSpinner(ValueModel)
     */
    public static void bindJSpinner(JSpinner spinner, ValueModel valueModel,
        int defaultValue, int minValue, int maxValue, int stepSize) {
        bindJSpinner(spinner, valueModel, Integer.valueOf(defaultValue),
            Integer.valueOf(minValue), Integer.valueOf(maxValue),
            Integer.valueOf(stepSize));
    }

    /**
     * Binds a JSpinner with the content bound to the given ValueModel. This expects the data to be a Number.
     *
     * @see #bindJSpinner(ValueModel)
     */
    public static void bindJSpinner(JSpinner spinner, ValueModel valueModel,
        Number defaultValue, Comparable minValue, Comparable maxValue,
        Number stepSize) {
        Number valueModelNumber = (Number) valueModel.getValue();
        Number initialValue = (valueModelNumber != null) ? valueModelNumber
                                                         : defaultValue;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(initialValue,
                minValue, maxValue, stepSize);
        new SpinnerToValueModelConnector(spinnerModel, valueModel, defaultValue);
        spinner.setModel(spinnerModel);

        if (validationListener != null) {
            valueModel.addValueChangeListener(validationListener);
        }
    }

    /**
     * Binds a JSpinner with the content bound to the given ValueModel. This expects the data to be a Date.
     *
     * @see #bindJSpinner(ValueModel)
     */
    public static void bindJSpinner(JSpinner spinner, ValueModel valueModel,
        Date defaultDate) {
        bindJSpinner(spinner, valueModel, defaultDate, null, null,
            Calendar.DAY_OF_MONTH);
    }

    /**
     * Binds a JSpinner with the content bound to the given ValueModel. This expects the data to be a Date and sets
     * start and end borders to the possible dates.
     *
     * @see #bindJSpinner(ValueModel)
     */
    public static void bindJSpinner(JSpinner spinner, ValueModel valueModel,
        Date defaultDate, Comparable start, Comparable end, int calendarField) {
        if (valueModel == null) {
            throw new NullPointerException("The valueModel must not be null.");
        }

        if (defaultDate == null) {
            throw new NullPointerException("The default date must not be null.");
        }

        Date valueModelDate = (Date) valueModel.getValue();
        Date initialDate = (valueModelDate != null) ? valueModelDate : defaultDate;
        SpinnerDateModel spinnerModel = new SpinnerDateModel(initialDate,
                start, end, calendarField);
        new SpinnerToValueModelConnector(spinnerModel, valueModel, defaultDate);
        spinner.setModel(spinnerModel);

        if (validationListener != null) {
            valueModel.addValueChangeListener(validationListener);
        }
    }

    /**
     * Binds a JSlider with the content bound to the given ValueModel. This expects the data to be an Integer and sets
     * min = 0 and max = 100 as borders to the possible values.
     *
     * @see #bindJSlider(JSlider, ValueModel, int, int)
     */
    public static void bindJSlider(JSlider slider, ValueModel valueModel) {
        bindJSlider(slider, valueModel, 0, 100);
    }

    /**
     * Binds a JSlider with the content bound to the given ValueModel. This expects the data to be an Integer and sets
     * min and max borders to the possible values.
     */
    public static void bindJSlider(JSlider slider, ValueModel valueModel,
        int min, int max) {
        BoundedRangeModel model = new DefaultBoundedRangeModel((Integer) valueModel.getValue(),
                0, min, max);
        slider.setModel(model);
        bind(slider, "value", valueModel, "value");

        if (validationListener != null) {
            valueModel.addValueChangeListener(validationListener);
        }
    }

    public static <T> void bindJTable(final JTable table,
        TableColumnModel columnModel, ListModel listModel) {
        bindJTable(table, columnModel, listModel, null);
    }

    public static <T> void bindJTable(final JTable table,
        TableColumnModel columnModel, ListModel listModel,
        ValueModel selectionValueModel) {
        TableModel tableModel = new GenericTableAdapter<T>(listModel,
                columnModel);
        table.setModel(tableModel);
        table.setColumnModel(columnModel);

        if (selectionValueModel != null) {
            SelectionInList<T> selectionInList = null;

            if (table instanceof JXTable) {
                selectionInList = new SelectionInList<T>(listModel,
                        selectionValueModel) {
                            private static final long serialVersionUID = 0L;

                            @Override
                            protected T getSafeElementAt(int index) {
                                return ((index < 0) || (index >= getSize()))
                                ? null
                                : super.getSafeElementAt(((JXTable) table).getFilters()
                                                          .convertRowIndexToModel(index));
                            }
                        };
            } else {
                selectionInList = new SelectionInList<T>(listModel,
                        selectionValueModel);
            }

            table.setSelectionModel(new SingleListSelectionAdapter(
                    selectionInList.getSelectionIndexHolder()));
        }
    }

    public static <T> void bindJXTreeTable(final JXTreeTable table,
        TableColumnModel columnModel, DefaultTreeTableModel model,
        final ValueModel selectionValueModel) {
        table.setTreeTableModel(model);

        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumnExt tCol = table.getColumnExt(i);
            TableColumn sCol = columnModel.getColumn(i);
            tCol.setCellEditor(sCol.getCellEditor());
            tCol.setCellRenderer(sCol.getCellRenderer());
            tCol.setHeaderRenderer(sCol.getHeaderRenderer());
            tCol.setHeaderValue(sCol.getHeaderValue());
            tCol.setIdentifier(sCol.getIdentifier());
            tCol.setMaxWidth(sCol.getMaxWidth());
            tCol.setMinWidth(sCol.getMinWidth());
            tCol.setModelIndex(sCol.getModelIndex());
            tCol.setPreferredWidth(sCol.getPreferredWidth());
            tCol.setResizable(sCol.getResizable());
            tCol.setWidth(sCol.getWidth());

            for (PropertyChangeListener listener : tCol.getPropertyChangeListeners())
                tCol.removePropertyChangeListener(listener);

            for (PropertyChangeListener listener : sCol.getPropertyChangeListeners())
                tCol.addPropertyChangeListener(listener);

            if (sCol instanceof TableColumnExt) {
                TableColumnExt eCol = (TableColumnExt) sCol;
                tCol.setComparator(eCol.getComparator());
                tCol.setPrototypeValue(eCol.getPrototypeValue());
                tCol.setTitle(eCol.getTitle());
                tCol.setToolTipText(eCol.getToolTipText());
                tCol.setEditable(eCol.isEditable());
                tCol.setSortable(eCol.isSortable());
                tCol.setVisible(eCol.isVisible());
                tCol.setHighlighters(eCol.getHighlighters());
                new TableColumnExtWrapper(eCol).copyClientPropertiesTo(tCol);
            }
        }

        if (selectionValueModel != null) {
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.addTreeSelectionListener(new TreeSelectionListener() {
                    public void valueChanged(TreeSelectionEvent e) {
                        selectionValueModel.setValue((e.getNewLeadSelectionPath() == null)
                            ? null
                            : e.getNewLeadSelectionPath().getLastPathComponent());
                    }
                });
        }
    }

    /**
     * Binds a JComponent with the content bound to the given ValueModel. It can be choosen if the value should be
     * validated
     */
    public static void bind(JComponent component, String propertyName,
        ValueModel valueModel) {
        bind(component, propertyName, valueModel, true);
    }

    /**
     * Binds a JComponent with the content bound to the given ValueModel. It can be choosen if the value should be
     * validated
     */
    public static void bind(JComponent component, String propertyName,
        ValueModel valueModel, boolean validating) {
        Bindings.bind(component, propertyName, valueModel);

        if (validating && (validationListener != null)) {
            valueModel.addValueChangeListener(validationListener);
        }
    }

    /**
     * Method to bind two generic bean properties to each other.
     */
    public static void bind(Object bean1, String property1, Object bean2,
        String property2) {
        PropertyConnector connector = PropertyConnector.connect(bean1,
                property1, bean2, property2);
        connector.updateProperty2();
    }
}
