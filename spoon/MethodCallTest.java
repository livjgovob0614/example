package fr.inria.gforge.spoon.transformation;

import org.junit.Test;
import spoon.MavenLauncher;
import spoon.compiler.Environment;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtBlock;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.filter.NamedElementFilter;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.declaration.CtExecutable;

import spoon.reflect.reference.CtIntersectionTypeReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtWildcardReference;
import spoon.reflect.reference.CtArrayTypeReference;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Collection;


/*
    각 method의 모든 method call 출력 테스트
        : 해당 class에 바디가 있는 메소드만 출력함. 
*/


public class MethodCallTest {
    @SuppressWarnings("all")
    @Test
    public void main() {
        MavenLauncher launcher = new MavenLauncher(
                "./src/test/resources/project/",
                MavenLauncher.SOURCE_TYPE.APP_SOURCE);

        CtModel model = launcher.buildModel();
    
        List<CtClass> classList = model.
                filterChildren(new NamedElementFilter<CtPackage>(CtPackage.class, "jg")).
                filterChildren(new TypeFilter<CtClass>(CtClass.class)).list();

        Factory factory = launcher.getFactory();

    for (CtClass cls : classList) {
        List<CtMethod> methodList = cls.filterChildren(new TypeFilter<CtMethod>(CtMethod.class)).list();

        for (CtMethod method : methodList) {
          List<CtComment> cList = new ArrayList<>();
          CtBlock methodBody = method.getBody();


          MethodInvocationSearch mis = new MethodInvocationSearch();

          model.filterChildren(new NamedElementFilter<CtPackage>(CtPackage.class, "jg"))
            .filterChildren(new NamedElementFilter<CtClass>(CtClass.class, cls.getSimpleName()))
            .filterChildren(new NamedElementFilter<CtMethod>(CtMethod.class, method.getSimpleName()))
            .forEach(mis::scan);

          Collection<MethodCallState> invOfMethod = mis.getInvocationsOfMethod();
          CtComment cc3  = factory.createComment("\n@@ in " + cls.getSimpleName() + "'s " + method.getSimpleName() + "@@ ====> \n", CtComment.CommentType.BLOCK);
          cList.add(cc3);
          for (MethodCallState mcs : invOfMethod) {
              CtExecutable<?> m = mcs.getMethod();
              CtComment cc = factory.createComment("@@@@@@@@@@@\n@method: " + m.toString() + "\n@simplename: " + m.getSimpleName(), CtComment.CommentType.BLOCK);
              cList.add(cc);
          }

          for (CtComment cmt : cList)
            methodBody.addStatement(cmt);
        }
      }

        Environment environment = launcher.getEnvironment();
        environment.setCommentEnabled(true);
        environment.setAutoImports(true);
        environment.setShouldCompile(true);
        launcher.prettyprint();
    }
}
