import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class MyPsiReferenceProvider extends PsiReferenceProvider {
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        Project project = psiElement.getProject();
        Class elementClass = psiElement.getClass();
        String className = elementClass.getName();

        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        String bladeDir = properties.getValue("bladeDir", "/www/local/mvc/views/");
        String bladeDirDesign = properties.getValue("bladeDirDesign", "/design/partials/");

        bladeDir = bladeDir.length() == 0 ? "/www/local/mvc/views/" : bladeDir;
        bladeDirDesign = bladeDirDesign.length() == 0 ? "/design/partials/" : bladeDirDesign;

        if (!className.endsWith("StringLiteralExpressionImpl")) {
            return PsiReference.EMPTY_ARRAY;
        }

        try {
            Method method = elementClass.getMethod("getValueRange");
            Object obj = method.invoke(psiElement);
            TextRange textRange = (TextRange) obj;
            Class _PhpPsiElement = elementClass.getSuperclass().getSuperclass().getSuperclass();
            Method phpPsiElementGetText = _PhpPsiElement.getMethod("getText");
            Object obj2 = phpPsiElementGetText.invoke(psiElement);
            String str = obj2.toString();
            String uri = str.substring(textRange.getStartOffset(), textRange.getEndOffset());
            int start = textRange.getStartOffset();
            int len = textRange.getLength();

            // Проверяем, подходит ли нам данная PHP-строка (путь к шаблону) или нет
            if (isViewFactoryCall(psiElement)) {
                PsiReference ref = new MyReference(uri, psiElement, new TextRange(start, start + len), project, bladeDir);
                PsiReference refDesign = new MyReference(uri, psiElement, new TextRange(start, start + len), project, bladeDirDesign);
                return new PsiReference[]{ref, refDesign};
            }
        } catch (Exception e) {}

        return PsiReference.EMPTY_ARRAY;
    }

    public boolean isViewFactoryCall(PsiElement element) {
        PsiElement prevEl = element.getParent();

        String elClassName;
        if (prevEl != null) {
            elClassName = prevEl.getClass().getName();
        }
        prevEl = prevEl.getParent();
        if (prevEl != null) {
            elClassName = prevEl.getClass().getName();
            if (elClassName.endsWith("FunctionReferenceImpl")) {
                try {

                    Method phpPsiElementGetName = prevEl.getClass().getMethod("getName");
                    String name = (String) phpPsiElementGetName.invoke(prevEl);
                    if (name.toLowerCase().equals("view")) {
                        return true;
                    }
                } catch (Exception ex) {

                }
            }
        }
        return false;
    }
}
