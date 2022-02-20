package components;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.impl.source.html.HtmlTagImpl;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class ComponentPsiReferenceProvider extends PsiReferenceProvider {
    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        Project project = psiElement.getProject();
        Class elementClass = psiElement.getClass();
        String className = elementClass.getName();

        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        String bladeDir = properties.getValue("bladeDir", "/resources/views/components/");

        if (!psiElement.getText().startsWith("<x-")) {
            return PsiReference.EMPTY_ARRAY;
        }

        try {
            Method method = elementClass.getMethod("getTextRange");
            Object obj = method.invoke(psiElement);
            TextRange textRange = (TextRange) obj;
            String componentName = psiElement.getText().split(" ")[0].replace("<x-", "").replace(".", "/");


            PsiReference ref = new ComponentReference(componentName, psiElement, new TextRange(0, componentName.length()), project, bladeDir);

            return new PsiReference[]{ref};
        } catch (Exception e) {
            notify(project, "exc + " + e.getMessage());
        }

        return PsiReference.EMPTY_ARRAY;
    }

    protected void notify(Project project, String content) {
        NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
                .createNotification(content, NotificationType.INFORMATION)
                .notify(project);
    }
}
