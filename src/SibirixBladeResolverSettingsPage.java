import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SibirixBladeResolverSettingsPage implements Configurable {
    Project project;
    private JBTextField bladeDirComponent = new JBTextField();
    private JBTextField bladeDirDesignComponent = new JBTextField();

    public SibirixBladeResolverSettingsPage(Project project) {
        this.project = project;
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Sibirix blade config";
    }

    @Override
    public @Nullable JComponent createComponent() {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        @Nullable String dir = properties.getValue("bladeDir");
        @Nullable String dirDesign = properties.getValue("bladeDir");

        if (dir != null) {
            bladeDirComponent.setText(properties.getValue("bladeDir"));
        }

        if (dirDesign != null) {
            bladeDirDesignComponent.setText(properties.getValue("bladeDirDesign"));
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JBLabel("Путь до шаблонов для сборки"));
        panel.add(bladeDirComponent);
        panel.add(new JBLabel("Путь до шаблонов для верстки"));
        panel.add(bladeDirDesignComponent);

        return panel;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        properties.setValue("bladeDir", bladeDirComponent.getText());
        properties.setValue("bladeDirDesign", bladeDirDesignComponent.getText());
    }
}
