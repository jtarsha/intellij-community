package com.intellij.refactoring.extractclass;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.ScrollingModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.RefactorJBundle;
import com.intellij.refactoring.RefactorJHelpID;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.sixrr.rpp.RefactorJ;

public class ExtractClassHandler implements RefactoringActionHandler {

    protected String getRefactoringName(){
        return RefactorJBundle.message("extract.class");
    }

    protected String getHelpID(){
        return RefactorJHelpID.ExtractClass;
    }

    public void invoke(Project project,
                       Editor editor,
                       PsiFile file,
                       DataContext dataContext){
        final RefactorJ plugin = RefactorJ.getInstance();
        final ScrollingModel scrollingModel = editor.getScrollingModel();
        scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE);
        final CaretModel caretModel = editor.getCaretModel();
        final int position = caretModel.getOffset();
        final PsiElement element = file.findElementAt(position);
        final PsiClass containingClass =
                PsiTreeUtil.getParentOfType(element, PsiClass.class, false);

        final PsiMember selectedMember =
                PsiTreeUtil.getParentOfType(element, PsiMember.class, false);

        if(containingClass == null){
          CommonRefactoringUtil.showErrorMessage(null, RefactorJBundle.message("cannot.perform.the.refactoring") +
                        RefactorJBundle.message("the.caret.should.be.positioned.within.a.class.to.be.refactored"), this.getHelpID(), project);
          return;
        }
        if(containingClass.isInterface()){
          CommonRefactoringUtil.showErrorMessage(null, RefactorJBundle.message("cannot.perform.the.refactoring") +
                        RefactorJBundle.message("the.selected.class.is.an.interface"), this.getHelpID(), project);
          return;
        }
        if(containingClass.isEnum()){
          CommonRefactoringUtil.showErrorMessage(null, RefactorJBundle.message("cannot.perform.the.refactoring") +
                        RefactorJBundle.message("the.selected.class.is.an.enumeration"), this.getHelpID(), project);
          return;
        }
        if(containingClass.isAnnotationType()){
          CommonRefactoringUtil.showErrorMessage(null, RefactorJBundle.message("cannot.perform.the.refactoring") +
                        RefactorJBundle.message("the.selected.class.is.an.annotation.type"), this.getHelpID(), project);
          return;
        }
        if(classIsInner(containingClass) && !containingClass.hasModifierProperty(PsiModifier.STATIC)){
          CommonRefactoringUtil.showErrorMessage(null, RefactorJBundle.message("cannot.perform.the.refactoring") +
                        RefactorJBundle.message("the.refactoring.is.not.supported.on.non.static.inner.classes"), this.getHelpID(), project);
          return;
        }
        if(classIsTrivial(containingClass)){
          CommonRefactoringUtil.showErrorMessage(null, RefactorJBundle.message("cannot.perform.the.refactoring") +
                        RefactorJBundle.message("the.selected.class.has.no.members.to.extract"), this.getHelpID(), project);
          return;
        }
        doExtractClass(containingClass, project, selectedMember);
    }

    private static boolean classIsInner(PsiClass aClass){
        return PsiTreeUtil.getParentOfType(aClass, PsiClass.class, true) != null;
    }

    public void invoke(Project project,
                       PsiElement[] elements,
                       DataContext dataContext){
        if(elements.length != 1){
            return;
        }
        final PsiClass containingClass =
                PsiTreeUtil.getParentOfType(elements[0], PsiClass.class, false);

        final PsiMember selectedMember =
                PsiTreeUtil.getParentOfType(elements[0], PsiMember.class, false);
        if(containingClass == null){
            return;
        }
        if(classIsTrivial(containingClass)){
            return;
        }
        doExtractClass(containingClass, project, selectedMember);
    }

    private void doExtractClass(final PsiClass containingClass, Project project, PsiMember selectedMember){
        final ExtractClassDialog dialog = new ExtractClassDialog(containingClass, selectedMember);
        dialog.show();
        if(!dialog.isOK()){
            return;
        }
       

    }

    private static boolean classIsTrivial(PsiClass containingClass){
        return containingClass.getFields().length == 0 && containingClass.getMethods().length == 0;
    }
}
