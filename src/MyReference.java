import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MyReference implements PsiReference {
    String uri;
    PsiElement psiElement;
    Project project;
    TextRange textRange;
    String dir;
    @Nullable PsiElement psiReference;

    public MyReference(String uri, PsiElement psiElement, TextRange textRange, Project project, String dir) {
        this.uri = uri;
        this.psiElement = psiElement;
        this.textRange = textRange;
        this.project = project;
        this.dir = dir;

        @Nullable VirtualFile virtualFile = project.getBaseDir().findFileByRelativePath(dir + this.psiElement.getText().replace("'", "").replace(".", "/") + ".blade.php");

        if (virtualFile == null) {
            virtualFile = project.getBaseDir().findFileByRelativePath(dir + this.psiElement.getText().replace("'", "").replace(".", "/") + ".php");
        }

        if (virtualFile != null) {
            this.psiReference = PsiManager.getInstance(this.project).findFile(virtualFile);
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
        return true;
    }

    @Override
    public @NotNull TextRange getAbsoluteRange() {
        return this.textRange;
    }

    @Override
    public Object[] getVariants() {
        int dirIndex = dir.indexOf("/", 1);

        if (!psiElement.getOriginalElement().getContainingFile().getVirtualFile().getPath().contains(dir.substring(1, dirIndex == -1 ? 1 : dirIndex))) {
            return new Object[0];
        }

        String curPath = this.psiElement.getText().replace("\"", "").replace("'", "").replace(".", "/");
        curPath = curPath.replace("IntellijIdeaRulezzz", ""); // wtf???
        int lastIndex = curPath.lastIndexOf("/");

        if (lastIndex == -1) {
            curPath = "";
        } else {
            curPath = curPath.substring(0, lastIndex);
        }

        VirtualFile vf = project.getBaseDir().findFileByRelativePath(dir + curPath + "/");
        List<LookupElement> variants = new ArrayList<>();
        VirtualFile[] children;

        if (vf != null) {
            children = vf.getChildren();
        } else {
            return variants.toArray();
        }

        for (VirtualFile child: children) {
            String left = curPath.length() == 0 ? "" : curPath.replace("/", ".") + ".";

            if (left.length() != 0 && !left.contains(child.getParent().getName())) {
                continue;
            } else if (left.length() == 0 && !dir.contains(child.getParent().getName())) {
                continue;
            }

            variants.add(LookupElementBuilder.create(
                    left + child.getName()
                            .replace(".blade.php", "")
                            .replace(".php", "")
                    )
                    .withTypeText(child.getName())
            );
        }

        return variants.toArray();
    }
}
