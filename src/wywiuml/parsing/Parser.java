package wywiuml.parsing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class Parser {

    final static String testdirPath = System.getProperty("user.dir");
    // final static String dirPath =
    // "F:\\Dokumente\\eclipse-workspace\\GIT_UML-Editor\\src";
    private final static FilenameFilter fileFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            // Accept all directories and .java files
            if (new File(dir + File.separator + name).isDirectory())
                return true;
            return name.contains(".java");
        }
    };

   /* public static void main(String[] args) {
        // System.out.println("path =" + System.getProperty("user.dir"));
        List<ClassOrInterfaceDeclaration> parsedClasses = parseClassesFromProjectPath(testdirPath);
        // System.out.println("classes parsed: " + parsedClasses.size());
        for (ClassOrInterfaceDeclaration parsedClass : parsedClasses) {
            // System.out.println(parsedClass.toString());
            System.out.println("\r\n");
        }
    }*/

    // =================================================

    public static List<ClassOrInterfaceDeclaration> parseClassesFromProjectPath(String projectPath) {
        List<ClassOrInterfaceDeclaration> allClasses = new ArrayList<ClassOrInterfaceDeclaration>();

        // First, get all .java files in the Project
        List<File> allFiles = getAllJavaFiles(projectPath);

        for (File f : allFiles) {
            // Turn the file into an CompilationUnit
            CompilationUnit cu = null;
            try {
                cu = StaticJavaParser.parse(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                continue;
            }
            // Extract all classes
            VoidVisitor<List<ClassOrInterfaceDeclaration>> classVisitor = new ClassVisitorAdapter();
            classVisitor.visit(cu, allClasses);
        }
        return allClasses;
    }

    public static String parseClassnameToUML(ClassOrInterfaceDeclaration cid) {
        StringBuilder str = new StringBuilder("");
        boolean hasVisibilityModifier = false;
        for (Modifier m : cid.getModifiers()) {
            if (m.getKeyword() == Modifier.Keyword.PRIVATE) {
                str.append("-");
                hasVisibilityModifier = true;
            } else if (m.getKeyword() == Modifier.Keyword.PROTECTED) {
                str.append("~");
                hasVisibilityModifier = true;
            } else if (m.getKeyword() == Modifier.Keyword.PUBLIC) {
                str.append("+");
                hasVisibilityModifier = true;
            }
        }
        if (hasVisibilityModifier == false) {
            str.append("#");
        }
        str.append(cid.getNameAsString());
        return str.toString();
    }

    public static List<FieldDeclaration> getFields(ClassOrInterfaceDeclaration cid) {

        List<FieldDeclaration> listField = new ArrayList<FieldDeclaration>();
        FieldCollector fieldCollector = new FieldCollector();
        fieldCollector.setContext(cid);
        fieldCollector.visit(cid, listField);

        return listField;
    }

    public static List<MethodDeclaration> getMethods(ClassOrInterfaceDeclaration cid) {

        List<MethodDeclaration> methodList = new ArrayList<MethodDeclaration>();
        MethodCollector methodsCollector = new MethodCollector();
        methodsCollector.setContext(cid);
        methodsCollector.visit(cid, methodList);

        return methodList;
    }

    public static void sort(ClassOrInterfaceDeclaration cid) {
        cid.getMembers().sort(new Comparator<BodyDeclaration>() {
            @Override
            public int compare(BodyDeclaration o1, BodyDeclaration o2) {
                if (o1.isFieldDeclaration() && o2.isMethodDeclaration())
                    return -1;
                if (o1.isMethodDeclaration() && o2.isFieldDeclaration())
                    return 1;
                if (o1.isClassOrInterfaceDeclaration() && !o2.isClassOrInterfaceDeclaration())
                    return 1;
                if (!o1.isClassOrInterfaceDeclaration() && o2.isClassOrInterfaceDeclaration())
                    return -1;
                
                return 0;
            }
        });
    }

   
    private static List<File> getAllJavaFiles(String path) {

        List<File> list = new ArrayList<File>();

        File directory = new File(path);
        if (directory.isDirectory() == false) {
            directory = directory.getParentFile();
        }

        // get all files of current directory
        File[] files = directory.listFiles(fileFilter);
        // System.out.println("files:" + files.length);
        if (files != null) {
            for (File f : files) {
                if (f.isFile())
                    list.add(f);
                else if (f.isDirectory())
                    // Go down every Directory
                    list.addAll(getAllJavaFiles(f.getAbsolutePath()));
            }
        }

        return list;
    }

    private static class ClassVisitorAdapter extends VoidVisitorAdapter<List<ClassOrInterfaceDeclaration>> {

        @Override
        public void visit(ClassOrInterfaceDeclaration cid, List<ClassOrInterfaceDeclaration> list) {
            super.visit(cid, list);
            list.add(cid);
        }
    }

    private static class MethodCollector extends VoidVisitorAdapter<List<MethodDeclaration>> {

        private ClassOrInterfaceDeclaration contextClass;

        public void setContext(ClassOrInterfaceDeclaration cid) {
            contextClass = cid;
        }

        @Override
        public void visit(MethodDeclaration md, List<MethodDeclaration> output) {
            super.visit(md, output);
            if (md.getParentNode().get() == contextClass) {
                output.add(md);
            }
        }
    }

    private static class FieldCollector extends VoidVisitorAdapter<List<FieldDeclaration>> {

        private ClassOrInterfaceDeclaration contextClass;

        public void setContext(ClassOrInterfaceDeclaration cid) {
            contextClass = cid;
        }

        @Override
        public void visit(FieldDeclaration fd, List<FieldDeclaration> output) {
            super.visit(fd, output);
            if (fd.getParentNode().get() == contextClass) {
                output.add(fd);
            }
        }
    }

}
