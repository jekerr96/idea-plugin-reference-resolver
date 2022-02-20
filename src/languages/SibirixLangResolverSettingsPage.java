package languages;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SibirixLangResolverSettingsPage implements Configurable {
    Project project;
    private JBTextField langDir = new JBTextField();
    private JBTextField langFn = new JBTextField();

    public SibirixLangResolverSettingsPage(Project project) {
        this.project = project;
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Sibirix language config";
    }

    @Override
    public @Nullable JComponent createComponent() {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        @Nullable String dir = properties.getValue("sibirixLangDir");
        @Nullable String langFnName = properties.getValue("sibirixLangFn");

        if (dir != null) {
            langDir.setText(properties.getValue("sibirixLangDir"));
        }

        if (langFnName != null) {
            langFn.setText(properties.getValue("sibirixLangFn"));
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JBLabel("Путь до языков"));
        panel.add(langDir);
        panel.add(new JBLabel("Функция вызова переводов"));
        panel.add(langFn);

        return panel;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        properties.setValue("sibirixLangDir", langDir.getText());
        properties.setValue("sibirixLangFn", langFn.getText());
    }
}
