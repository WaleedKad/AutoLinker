import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class LinkingTool extends JFrame {

    private JTextField uiClassField;
    private JTextField methodClassField;
    private JTextField methodNameField;
    private JComboBox<String> parameterRangeComboBox;
    private List<JComboBox<String>> parameterTypeComboBoxes;
    private JPanel parameterTypePanel;
    private JTextField componentNameField;
    private JTextField resultComponentNameField;
    private JButton generateButton, clear, copy;
    private JTextArea codeTextArea;
    private JLabel statusLabel;

    public LinkingTool() {
        setTitle("Linking Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(12, 2));
        setBackground(Color.black);

        add(new JLabel("UI Class Name:"));
        uiClassField = new JTextField();
        add(uiClassField);

        add(new JLabel("Method Class Name:"));
        methodClassField = new JTextField();
        add(methodClassField);

        add(new JLabel("Method Name:"));
        methodNameField = new JTextField();
        add(methodNameField);

        add(new JLabel("Parameter Range (1 to 100):"));
        parameterRangeComboBox = new JComboBox<>();
        for (int i = 1; i <= 100; i++) {
            parameterRangeComboBox.addItem(String.valueOf(i));
        }
        parameterRangeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateParameterTypePanel();
            }
        });
        add(parameterRangeComboBox);

        JPanel parameterPanel = new JPanel();
        parameterPanel.setLayout(new BorderLayout());

        parameterTypePanel = new JPanel();
        parameterTypePanel.setLayout(new GridLayout(0, 1));
        JScrollPane parameterScrollPane = new JScrollPane(parameterTypePanel);
        parameterPanel.add(parameterScrollPane, BorderLayout.CENTER);

        add(new JLabel("Parameter Types:"));
        add(parameterPanel);

        add(new JLabel("Component Name:"));
        componentNameField = new JTextField();
        add(componentNameField);

        add(new JLabel("Result Component Name:"));
        resultComponentNameField = new JTextField();
        add(resultComponentNameField);

        generateButton = new JButton("Generate Code");
        add(generateButton);

        statusLabel = new JLabel();
        add(statusLabel);

        codeTextArea = new JTextArea();
        codeTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(codeTextArea);
        add(scrollPane);

        add(new JPanel()); // Space

        clear = new JButton("Clear");
        copy = new JButton("Copy");
        add(clear);
        add(copy);

        // Add a clear code button
        JButton clearCodeButton = new JButton("Clear Code");
        add(clearCodeButton);

        // Associate clearCodeTextArea method with the clear code button
        clearCodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearCodeTextArea();
            }
        });

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateCodeSnippet();
            }
        });

        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });

        copy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyCodeToClipboard();
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void updateParameterTypePanel() {
        int parameterRange = Integer.parseInt((String) parameterRangeComboBox.getSelectedItem());
        parameterTypePanel.removeAll();
        parameterTypePanel.setLayout(new GridLayout(parameterRange, 1));
        parameterTypeComboBoxes = new ArrayList<>();
        for (int i = 1; i <= parameterRange; i++) {
            JComboBox<String> parameterTypeComboBox = new JComboBox<>();
            parameterTypeComboBox.addItem("int");
            parameterTypeComboBox.addItem("double");
            parameterTypeComboBox.addItem("float");
            parameterTypeComboBox.addItem("boolean");
            parameterTypePanel.add(new JLabel("Parameter " + i));
            parameterTypePanel.add(parameterTypeComboBox);
            parameterTypeComboBoxes.add(parameterTypeComboBox);
        }
        parameterTypePanel.revalidate();
        parameterTypePanel.repaint();
    }

    private void generateCodeSnippet() {
        try {
            String uiClassName = uiClassField.getText().trim();
            String methodClassName = methodClassField.getText().trim();
            String methodName = methodNameField.getText().trim();
            int parameterRange = Integer.parseInt((String) parameterRangeComboBox.getSelectedItem());
            String componentName = componentNameField.getText().trim();
            String resultComponentName = resultComponentNameField.getText().trim();

            StringBuilder paramParsing = new StringBuilder();

            for (int i = 0; i < parameterRange; i++) {
                JComboBox<String> parameterTypeComboBox = parameterTypeComboBoxes.get(i);
                String parameterType = (String) parameterTypeComboBox.getSelectedItem();
                String parameterName = "param" + (i + 1);
                paramParsing.append(getParamParsingLine(parameterType, parameterName, i));
            }

            StringBuilder methodInvocation = new StringBuilder();
            methodInvocation.append(methodClassName).append(" ").append(methodName.substring(0, 1).toLowerCase()).append(methodName.substring(1)).append(" = new ").append(methodClassName).append("();\n");
            for (int i = 0; i < parameterRange; i++) {
                methodInvocation.append(getMethodParameter(parameterTypeComboBoxes.get(i), "param" + (i + 1)));
            }
            methodInvocation.append("Object result = ").append(methodName.substring(0, 1).toLowerCase()).append(methodName.substring(1)).append(".getClass().getMethod(\"").append(methodName).append("\", ");
            for (int i = 0; i < parameterRange; i++) {
                methodInvocation.append(getMethodParameterType(parameterTypeComboBoxes.get(i)));
                if (i < parameterRange - 1) {
                    methodInvocation.append(", ");
                }
            }
            methodInvocation.append(").invoke(").append(methodName.substring(0, 1).toLowerCase()).append(methodName.substring(1)).append(", ");
            for (int i = 0; i < parameterRange; i++) {
                methodInvocation.append("param").append(i + 1);
                if (i < parameterRange - 1) {
                    methodInvocation.append(", ");
                }
            }
            methodInvocation.append(");\n");

            String codeSnippet =
                    componentName + ".addActionListener(new ActionListener() {\n" +
                            "    @Override\n" +
                            "    public void actionPerformed(ActionEvent e) {\n" +
                            "        try {\n" +
                            "            " + methodInvocation.toString() +
                            "            // Display result in the specified component\n" +
                            "            // Modify this line based on your UI structure\n" +
                            "            " + resultComponentName + ".setText(\"Result: \" + result);\n" +
                            "        } catch (Exception ex) {\n" +
                            "            " + resultComponentName + ".setText(\"Error: \" + ex.getMessage());\n" +
                            "        }\n" +
                            "    }\n" +
                            "});\n";

            codeTextArea.append(codeSnippet);
            statusLabel.setText("Code generated successfully.");
        } catch (NumberFormatException ex) {
            statusLabel.setText("Error: Invalid parameter range.");
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

    private String getParamParsingLine(String parameterType, String parameterName, int index) {
        switch (parameterType) {
            case "int":
                return "int " + parameterName + " = Integer.parseInt(field" + (index + 1) + ".getText());\n";
            case "double":
                return "double " + parameterName + " = Double.parseDouble(field" + (index + 1) + ".getText());\n";
            case "float":
                return "float " + parameterName + " = Float.parseFloat(field" + (index + 1) + ".getText());\n";
            case "boolean":
                return "boolean " + parameterName + " = Boolean.parseBoolean(field" + (index + 1) + ".getText());\n";
            default:
                return "";
        }
    }

    private String getMethodParameter(JComboBox<String> parameterTypeComboBox, String parameterName) {
        String parameterType = (String) parameterTypeComboBox.getSelectedItem();
        switch (parameterType) {
            case "int":
            case "double":
            case "float":
                return getParamParsingLine(parameterType, parameterName, Integer.parseInt(parameterName.substring(5)) - 1);
            case "boolean":
                return "boolean " + parameterName + " = Boolean.parseBoolean(field" + parameterName.substring(5) + ".getText());\n";
            default:
                return "";
        }
    }

    private String getMethodParameterType(JComboBox<String> parameterTypeComboBox) {
        String parameterType = (String) parameterTypeComboBox.getSelectedItem();
        switch (parameterType) {
            case "int":
                return "int.class";
            case "double":
                return "double.class";
            case "float":
                return "float.class";
            case "boolean":
                return "boolean.class";
            default:
                return "";
        }
    }

    private void clearFields() {
        uiClassField.setText("");
        methodClassField.setText("");
        methodNameField.setText("");
        parameterRangeComboBox.setSelectedIndex(0);
        parameterTypePanel.removeAll();
        componentNameField.setText("");
        resultComponentNameField.setText("");
        codeTextArea.setText("");
        statusLabel.setText("");
    }

    private void copyCodeToClipboard() {
        String code = codeTextArea.getText();
        if (!code.isEmpty()) {
            StringSelection stringSelection = new StringSelection(code);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            statusLabel.setText("Code copied to clipboard.");
        } else {
            statusLabel.setText("Error: Nothing to copy.");
        }
    }

    private void clearCodeTextArea() {
        codeTextArea.setText(""); // Clear the code text area
        statusLabel.setText(""); // Clear status label
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LinkingTool::new);
    }
}