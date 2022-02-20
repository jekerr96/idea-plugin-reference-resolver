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
    Boolean showVariants;

    public LanguageReference(String uri, PsiElement psiElement, TextRange textRange, Project project, VirtualFile dir, Boolean showVariants) {
        this.uri = uri;
        this.psiElement = psiElement;
        this.textRange = textRange;
        this.project = project;
        this.dir = dir;
        this.showVariants = showVariants;
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

        if (!this.showVariants) {
            return variants.toArray();
        }

        String curValue = replaceQuote(psiElement.getOriginalElement().getText());
        String[] curValueParts = curValue.split("\\.");

        int curDeep = curValueParts.length;

        if (curValue.endsWith(".")) curDeep++;

        if (curDeep <= 1) {
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

        VirtualFile translateFile = dir.findFileByRelativePath("/" + curValueParts[0] + ".php");

        if (translateFile == null) {
            return variants.toArray();
        }

        PsiFile psiFile = PsiManager.getInstance(project).findFile(translateFile);

        PsiElement rootArray = findByClassName("com.jetbrains.php.lang.psi.elements.impl.ArrayCreationExpressionImpl", psiFile);

        if (rootArray == null) {
            return variants.toArray();
        }

        return getVariantsFromArray(rootArray, psiFile.getName().replace(".php", ""), curDeep, 1).toArray();
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

    protected PsiElement findByClassName(String className, PsiElement startElement) {
        if (startElement.getClass().getName().equals(className)) return startElement;

        PsiElement[] children = startElement.getChildren();

        if (children.length == 0 && startElement.getNextSibling() != null) {
            return findByClassName(className, startElement.getNextSibling());
        }

        for (PsiElement child: children) {
            PsiElement childResult = findByClassName(className, child);

            if (childResult != null) {
                return childResult;
            }
        }

        return null;
    }

    protected PsiElement[] findAllChildByClassName(String className, PsiElement parentElement) {
        List<PsiElement> elements = new ArrayList<>();

        for (PsiElement child: parentElement.getChildren()) {
            if (child.getClass().getName().equals(className)) {
                elements.add(child);
            }
        }

        return elements.toArray(new PsiElement[elements.size()]);
    }

    protected List<LookupElement> getVariantsFromArray(PsiElement rootArray, String prevPath, int maxDeep, int curDeep) {
        List<LookupElement> variants = new ArrayList<>();

        if (curDeep >= maxDeep) return variants;

        PsiElement[] arrayElements = findAllChildByClassName("com.jetbrains.php.lang.psi.elements.impl.ArrayHashElementImpl", rootArray);

        for (PsiElement arrayElement: arrayElements) {
            PsiElement elementName = arrayElement.getFirstChild();

            if (elementName == null) continue;

            PsiElement elementValue = arrayElement.getLastChild().getFirstChild();
            Boolean isArray = elementValue.getClass().getName().equals("com.jetbrains.php.lang.psi.elements.impl.ArrayCreationExpressionImpl");

            String path = prevPath + "." + replaceQuote(elementName.getText());

            variants.add(LookupElementBuilder.create(path).withTypeText(isArray ? "Array" : replaceQuote(elementValue.getText())));

            if (isArray) {
                for (LookupElement lookupElement: getVariantsFromArray(elementValue, path, maxDeep, curDeep + 1)) {
                    variants.add(lookupElement);
                }
            }
        }

        return variants;
    }

    protected String replaceQuote(String original) {
        return original.replace("\"", "").replace("'", "");
    }

    protected void notify(String content) {
        NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
                .createNotification(content, NotificationType.INFORMATION)
                .notify(project);
    }
}
