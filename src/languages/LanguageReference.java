package languages;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LanguageReference implements PsiReference {
    String uri;
    PsiElement psiElement;
    Project project;
    TextRange textRange;
    VirtualFile dir;

    public LanguageReference(String uri, PsiElement psiElement, TextRange textRange, Project project, VirtualFile dir) {
        this.uri = uri;
        this.psiElement = psiElement;
        this.textRange = textRange;
        this.project = project;
        this.dir = dir;
    }

    @Override
    public @NotNull PsiElement getElement() {
        return this.psiElement;
    }

    @Override
    public @NotNull TextRange getRangeInElement() {
        return this.textRange;
    }

    @Override
    public @Nullable PsiElement resolve() {
        return findRef();
    }

    @Override
    public @NotNull @NlsSafe String getCanonicalText() {
        return this.psiElement.toString();
    }

    @Override
    public PsiElement handleElementRename(@NotNull String s) throws IncorrectOperationException {
        return null;
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement psiElement) throws IncorrectOperationException {
        return null;
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement psiElement) {
        return resolve() == psiElement;
    }

    @Override
    public boolean isSoft() {
        return false;
    }

    @Override
    public @NotNull TextRange getAbsoluteRange() {
        return this.textRange;
    }

    @Override
    public Object[] getVariants() {
        String curValue = psiElement.getOriginalElement().getText();

        String[] curValueParts = curValue.split("\\.");
        notify(curValue);
        List<LookupElement> variants = new ArrayList<>();

        if (curValueParts.length <= 1) {
            for (VirtualFile translateFile: dir.getChildren()) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(translateFile);

                variants.add(LookupElementBuilder.create(
                                        psiFile.getName()
                                                .replace(".php", "")
                                )
                                .withTypeText(translateFile.getName())
                );
            }

            return variants.toArray();
        }


        return variants.toArray();
    }

    protected PsiElement findRef() {
        String[] langParts = psiElement.getText().split("\\.");

        if (langParts.length < 2) {
            return null;
        }

        String file = langParts[0].replace("\"", "").replace("'", "");
        VirtualFile translateFile = dir.findFileByRelativePath("/" + file + ".php");

        if (translateFile == null) {
            return null;
        }

        return PsiManager.getInstance(project).findFile(translateFile);
    }

    protected void notify(String content) {
        NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
                .createNotification(content, NotificationType.INFORMATION)
                .notify(project);
    }
}
