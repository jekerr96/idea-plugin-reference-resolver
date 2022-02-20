package components;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ComponentReference implements PsiReference {
    String uri;
    PsiElement psiElement;
    Project project;
    TextRange textRange;
    String dir;
    @Nullable PsiElement psiReference;

    public ComponentReference(String uri, PsiElement psiElement, TextRange textRange, Project project, String dir) {
        this.uri = uri;
        this.psiElement = psiElement;
        this.textRange = textRange;
        this.project = project;
        this.dir = dir;

        @Nullable VirtualFile virtualFile = project.getBaseDir().findFileByRelativePath(dir + uri + ".blade.php");

        if (virtualFile != null) {
            this.psiReference = PsiManager.getInstance(project).findFile(virtualFile);
        }
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
        return this.psiReference;
    }

    @Override
    public @NotNull @NlsSafe String getCanonicalText() {
        return psiElement.getText();
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
        return true;
    }

    @Override
    public @NotNull TextRange getAbsoluteRange() {
        return this.textRange;
    }

    protected void notify(String content) {
        NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
                .createNotification(content, NotificationType.INFORMATION)
                .notify(project);
    }
}
