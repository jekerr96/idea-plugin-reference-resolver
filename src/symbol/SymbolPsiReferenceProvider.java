package symbol;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class SymbolPsiReferenceProvider extends PsiReferenceProvider {
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        Project project = psiElement.getProject();
        Class elementClass = psiElement.getClass();
        String className = elementClass.getName();

        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        String symbolsDir = properties.getValue("sibirixSymbolsDir", "/www/local/images/symbol/");
        String symbolsDirDesign = properties.getValue("sibirixSymbolsDirDesign", "/design/images/symbol/");

        symbolsDir = symbolsDir.length() == 0 ? "/www/local/images/symbol/" : symbolsDir;
        symbolsDirDesign = symbolsDirDesign.length() == 0 ? "/design/images/symbol/" : symbolsDirDesign;

        if (!className.endsWith("StringLiteralExpressionImpl") && !className.endsWith("XmlAttributeValueImpl")) {
            return PsiReference.EMPTY_ARRAY;
        }

        try {
            TextRange textRange;
            String uri;

            if (elementClass.isAssignableFrom(XmlAttributeValueImpl.class)) {
                textRange = new TextRange(1, psiElement.getText().length() - 1);
                uri = psiElement.getText();
            } else {
                Method method = elementClass.getMethod("getValueRange");
                Object obj = method.invoke(psiElement);
                textRange = (TextRange) obj;
                Class _PhpPsiElement = elementClass.getSuperclass().getSuperclass().getSuperclass();
                Method phpPsiElementGetText = _PhpPsiElement.getMethod("getText");
                Object obj2 = phpPsiElementGetText.invoke(psiElement);
                String str = obj2.toString();
                str.substring(textRange.getStartOffset(), textRange.getEndOffset());
                uri = str.substring(textRange.getStartOffset(), textRange.getEndOffset());
            }

            if (isSymbolCall(psiElement)) {
                VirtualFile symbolDirVf = project.getBaseDir().findFileByRelativePath(symbolsDir);
                VirtualFile symbolDirDesignVf = project.getBaseDir().findFileByRelativePath(symbolsDirDesign);

                return new PsiReference[]{new SymbolReference(uri, psiElement, textRange, project, symbolDirVf, symbolDirDesignVf)};
            }
        } catch (Exception e) {

        }

        return PsiReference.EMPTY_ARRAY;
    }

    public boolean isSymbolCall(@NotNull PsiElement element) {
        return this.checkVue(element) || this.checkViewHelper(element);
    }

    protected boolean checkViewHelper(@NotNull PsiElement element) {
        PsiElement prevEl = element.getParent().getPrevSibling().getPrevSibling();
        String elClassName;

        if (prevEl != null) {
            elClassName = prevEl.getClass().getName();

            if (elClassName.endsWith("LeafPsiElement")) {
                try {

                    Method phpPsiElementGetText = prevEl.getClass().getMethod("getText");
                    String text = (String) phpPsiElementGetText.invoke(prevEl);

                    if (text.equals("getSymbol")) {
                        return true;
                    }
                } catch (Exception ex) {

                }
            }
        }
        return false;
    }

    protected boolean checkVue(@NotNull PsiElement element) {
        return element.getParent().getText().startsWith("name") && element.getParent().getPrevSibling().getPrevSibling().getText().trim().startsWith("SvgSymbol");
    }
}
