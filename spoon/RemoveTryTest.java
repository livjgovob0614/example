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
import spoon.support.reflect.code.CtStatementImpl;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtCatch;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Filter;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.visitor.filter.NamedElementFilter;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.reflect.code.CtCodeSnippetStatement;

import java.util.ArrayList;
import java.util.List;


public class RemoveTryTest {
    @SuppressWarnings("all")
    @Test
    public void main() {
		// (1) set target path
        MavenLauncher launcher = new MavenLauncher(
                "./src/test/resources/project/",
                MavenLauncher.SOURCE_TYPE.APP_SOURCE);

        CtModel model = launcher.buildModel();
        List<CtTry> tryList = model.
                filterChildren(new NamedElementFilter<CtPackage>(CtPackage.class, "jg")).
                filterChildren(new TypeFilter<CtMethod>(CtMethod.class)).
                filterChildren(new TypeFilter<CtTry>(CtTry.class)).list();

		Factory factory = launcher.getFactory();

		for (CtTry t : tryList) {
            CtBlock tryBody = t.getBody();
            CtBlock finalizer = t.getFinalizer();
        	CtBlock methodBody = t.getParent(CtExecutable.class).getBody();

            ArrayList<CtStatement> ctStmt = new ArrayList<>(tryBody.getStatements());
            CtStatement startTry = t;

            if (!ctStmt.isEmpty()) {
                for (CtStatement stmt : ctStmt) {
					String s = stmt.toString();
                    CtStatementImpl.insertBefore(startTry, stmt);
				}
            }
            if (finalizer != null) {
                ctStmt = new ArrayList<>(finalizer.getStatements());
                if (!ctStmt.isEmpty()) {
                    for (CtStatement stmt : ctStmt) {
						String s = stmt.toString();
                        CtStatementImpl.insertBefore(startTry, stmt);
					}
                }
            }
            methodBody.removeStatement(t);
        }

        Environment environment = launcher.getEnvironment();
        environment.setCommentEnabled(true);
        environment.setAutoImports(true);
        environment.setShouldCompile(true);
        launcher.prettyprint();
	
    }
}
