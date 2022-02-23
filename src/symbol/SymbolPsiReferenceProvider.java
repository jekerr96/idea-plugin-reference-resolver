package symbol;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class SymbolPsiReferenceProvider extends PsiReferenceProvider {
    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        Project project = psiElement.getProject();
        Class elementClass = psiElement.getClass();
        String className = elementClass.getName();

        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        String symbolsDir = properties.getValue("sibirixSymbolsDir", "/www/local/images/symbol/");
        String symbolsDirDesign = properties.getValue("sibirixSymbolsDirDesign", "/design/images/symbol/");

        symbolsDir = symbolsDir.length() == 0 ? "/www/local/images/symbol/" : symbolsDir;
        symbolsDirDesign = symbolsDirDesign.length() == 0 ? "/design/images/symbol/" : symbolsDirDesign;

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

            if (isSymbolCall(psiElement)) {
                VirtualFile symbolDirVf = project.getBaseDir().findFileByRelativePath(symbolsDir);
                VirtualFile symbolDirDesignVf = project.getBaseDir().findFileByRelativePath(symbolsDirDesign);

                return new PsiReference[]{new SymbolReference(uri, psiElement, new TextRange(start, start + len), project, symbolDirVf, symbolDirDesignVf)};
            }
        } catch (Exception e) {}

        return PsiReference.EMPTY_ARRAY;
    }

    public boolean isSymbolCall(@NotNull PsiElement element) {
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
}
