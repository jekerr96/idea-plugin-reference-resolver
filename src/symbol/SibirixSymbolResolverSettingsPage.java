package symbol;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SibirixSymbolResolverSettingsPage implements Configurable {
    Project project;
    private JBTextField symbolDir = new JBTextField();
    private JBTextField symbolDirDesign = new JBTextField();

    public SibirixSymbolResolverSettingsPage(Project project) {
        this.project = project;
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Sibirix symbol config";
    }

    @Override
    public @Nullable JComponent createComponent() {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        @Nullable String dir = properties.getValue("sibirixSymbolsDir");
        @Nullable String dirDesign = properties.getValue("sibirixSymbolsDirDesign");

        if (dir != null) {
            symbolDir.setText(properties.getValue("sibirixSymbolsDir"));
        }

        if (dirDesign != null) {
            symbolDirDesign.setText(properties.getValue("sibirixSymbolsDirDesign"));
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JBLabel("Путь до символов"));
        panel.add(symbolDir);
        panel.add(new JBLabel("Путь до символов верстка"));
        panel.add(symbolDirDesign);

        return panel;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        properties.setValue("sibirixSymbolsDir", symbolDir.getText());
        properties.setValue("sibirixSymbolsDirDesign", symbolDirDesign.getText());
    }
}
