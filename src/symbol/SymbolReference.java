package symbol;

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

public class SymbolReference implements PsiReference {
    String uri;
    PsiElement psiElement;
    Project project;
    TextRange textRange;
    VirtualFile dir;
    VirtualFile dirDesign;

    public SymbolReference(String uri, PsiElement psiElement, TextRange textRange, Project project, VirtualFile dir, VirtualFile dirDesign) {
        this.uri = uri;
        this.psiElement = psiElement;
        this.textRange = textRange;
        this.project = project;
        this.dir = dir;
        this.dirDesign = dirDesign;
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
        List<LookupElement> variants = new ArrayList<>();
        VirtualFile symbolDir = dir == null ? dirDesign : dir;

        if (symbolDir == null) return new Object[0];

        for (VirtualFile symbol: symbolDir.getChildren()) {
            if (symbol.getName().trim().isEmpty()) continue;

            variants.add(LookupElementBuilder.create(symbol.getName().replace(".svg", "")));
        }

        return variants.toArray();
    }

    protected PsiElement findRef() {
        String file = psiElement.getText().replace("\"", "").replace("'", "");
        VirtualFile symbolFile = null;

        if (dir != null) {
             symbolFile = dir.findFileByRelativePath(file + ".svg");
        }

        if (symbolFile == null && dirDesign != null) {
            symbolFile = dirDesign.findFileByRelativePath(file + ".svg");
        }

        if (symbolFile == null) {
            return null;
        }

        return PsiManager.getInstance(project).findFile(symbolFile);
    }
}
