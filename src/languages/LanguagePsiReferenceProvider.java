package languages;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class LanguagePsiReferenceProvider extends PsiReferenceProvider {
    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        Project project = psiElement.getProject();
        Class elementClass = psiElement.getClass();
        String className = elementClass.getName();

        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        String langsDir = properties.getValue("sibirixLangDir", "/www/local/mvc/resources/lang/");
        String suggestLang = properties.getValue("sibirixLangSuggestions", "ru");

        langsDir = langsDir.length() == 0 ? "/www/local/mvc/resources/lang/" : langsDir;
        suggestLang = suggestLang.length() == 0 ? "ru" : suggestLang;

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

            if (isLangCall(project, psiElement)) {
                VirtualFile langsDirVf = project.getBaseDir().findFileByRelativePath(langsDir);
                VirtualFile[] langs = langsDirVf.getChildren();
                List<PsiReference> refs = new ArrayList<>();

                for (VirtualFile lang: langs) {
                    Boolean showVariants = lang.getName().equals(suggestLang);
                    refs.add(new LanguageReference(uri, psiElement, new TextRange(start, start + len), project, lang, showVariants));
                }

                return refs.toArray(PsiReference.EMPTY_ARRAY);
            }
        } catch (Exception e) {}

        return PsiReference.EMPTY_ARRAY;
    }

    public boolean isLangCall(Project project, @NotNull PsiElement element) {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        String langFn = properties.getValue("sibirixLangFn", "__");

        langFn = langFn.length() == 0 ? "__" : langFn;

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

                    if (name.toLowerCase().equals(langFn)) {
                        return true;
                    }
                } catch (Exception ex) {

                }
            }
        }
        return false;
    }

    protected void notify(Project project, String content) {
        NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
                .createNotification(content, NotificationType.INFORMATION)
                .notify(project);
    }
}
