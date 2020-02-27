// .../spoon/examples/src/main/java/fr/inria/gforge/spoon/transformation/
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

import spoon.reflect.reference.CtIntersectionTypeReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtWildcardReference;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.code.CtVariableAccess;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/*
    각 메소드의 로컬변수 출력 예제
		(1) name
        (2) type
        (3) assignment
        (4) reference 
*/


public class LocalVarInfoTest {
    // ***** for test ****** //
    @SuppressWarnings("all")
    @Test
    public void main() {
        // (1) set target path
        // .../spoon/examples/src/test/resources/project/src/main/java/jg/
        MavenLauncher launcher = new MavenLauncher(
                "./src/test/resources/project/",
                MavenLauncher.SOURCE_TYPE.APP_SOURCE);

        // (2) filter
        CtModel model = launcher.buildModel();
        List<CtMethod> methodList = model.
                filterChildren(new NamedElementFilter<CtPackage>(CtPackage.class, "jg")).
                filterChildren(new TypeFilter<CtMethod>(CtMethod.class)).list();

        Factory factory = launcher.getFactory();

        // (3) main work
        for (CtMethod method : methodList) {
          CtBlock methodBody = method.getBody();
          List<CtComment> cList = new ArrayList<>();
          List<CtLocalVariable> ctLocalVar = method.filterChildren(new TypeFilter<CtLocalVariable>(CtLocalVariable.class)).list();

          for (CtLocalVariable lvar : ctLocalVar) {
              // type
              String typeRef = lvar.getType().toString();
              // assignment
              String asm = lvar.getAssignment().toString();
              // reference
              CtLocalVariableReference ref = lvar.getReference();

			  // print
              CtComment cc = factory.createComment("\t**** varName: " + lvar.getSimpleName() + " ****\n\t@ typeRef: " + typeRef + "\n\t@ assignment: " + asm + "\n\t@ ref: " + ref.toString(), CtComment.CommentType.BLOCK);
			  cList.add(cc);
		  }
          for (CtComment cmt : cList)
            methodBody.addStatement(cmt);
        }

        // (4) env set, print
        Environment environment = launcher.getEnvironment();
        environment.setCommentEnabled(true);
        environment.setAutoImports(true);
        environment.setShouldCompile(true);
        launcher.prettyprint();
    }
}
