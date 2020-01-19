package wywiuml.structures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.html.HTMLDocument.HTMLReader.SpecialAction;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import wywiuml.parsing.Parser;

@SuppressWarnings("serial")
public class ClassOrInterfaceUML {

	private final static String firstCharacters = "[\\p{L}_\\$]";
	private final static String followingCharacters = "[\\w|" + firstCharacters + "]";
	private final static String visibilityCharacters = "[+#~-]";
	private final static String namingRegEx = firstCharacters + followingCharacters + "*";

	private final static String classRegex = "={3,}Name={3,}(.*)={3,}Attribute={3,}(.*)={3,}Methoden={3,}(.*)";
	private final static String nameRegex = "^\\s*(" + visibilityCharacters + "?)" + "\\s*(" + namingRegEx + ")";
	private final static String attributeRegex = "^\\s*(" + visibilityCharacters + "?)\\s*(" + namingRegEx
			+ ")\\s*:\\s*(" + namingRegEx + ")";
	private final static String methodRegex = "^\\s*(" + visibilityCharacters + "?)\\s*(" + namingRegEx
			+ ")\\s*\\((.*)\\)\\s*:\\s*(" + namingRegEx + ")";
	private final static String parameterRegex = "^(" + namingRegEx + ")\\s(" + namingRegEx + ")$";

	static List<ClassOrInterfaceUML> allClassesOrInterfaces = new ArrayList<ClassOrInterfaceUML>();

	ClassOrInterfaceDeclaration classInfo;
	//List<UMLLine> umlLines = new ArrayList<UMLLine>();

	public static ClassOrInterfaceUML fromDeclaration(ClassOrInterfaceDeclaration cid) {
		ClassOrInterfaceUML info = new ClassOrInterfaceUML();
		info.classInfo = cid;
		return info;
	}

	public static ClassOrInterfaceUML quickCreate(String parseString) throws RegExException {
		ClassOrInterfaceUML info = new ClassOrInterfaceUML();
		Pattern pattern = Pattern.compile(nameRegex);
		Matcher matcher;
		matcher = pattern.matcher(parseString.trim());

		if (matcher.find() == false) {
			throw new RegExException(parseString.trim());
		} else {
			if (matcher.group(1) != null && matcher.group(1).equals("") == false) {
				switch (matcher.group(1).charAt(0)) {
					case '+':
						info.classInfo.addModifier(Modifier.Keyword.PUBLIC);
						break;
					case '#':
						// "Package Modifier" doesn't exist
						break;
					case '~':
						info.classInfo.addModifier(Modifier.Keyword.PROTECTED);
						break;
					case '-':
						info.classInfo.addModifier(Modifier.Keyword.PRIVATE);
						break;

				}

			}

			info.classInfo.setName(matcher.group(2));
		}

		return info;
	}

	public static ClassOrInterfaceUML parseInfoString(String parseString) throws Exception {
		ClassOrInterfaceUML umlClass = new ClassOrInterfaceUML();

		String nameString = null;
		String attString = null;
		String methString = null;

		Pattern classPattern = Pattern.compile(classRegex, Pattern.DOTALL);
		Matcher classMatcher = classPattern.matcher(parseString);

		if (classMatcher.find() == true) {
			nameString = classMatcher.group(1).trim();
			attString = classMatcher.group(2).trim();
			methString = classMatcher.group(3).trim();
		} else {
			throw new Exception("Could not find a valid separation between name, attributes and methods");
		}

		umlClass = quickCreate(nameString);

		String[] umlAttributes = attString.trim().split("\r?\n");
		for (String s : umlAttributes) {
			try {
				umlClass.addAttributeFromUMLString(s);
			} catch (Exception error) {

			}
		}

		String[] umlMethods = methString.trim().split("\r?\n");
		for (String s : umlMethods) {
			try {
				umlClass.addMethodFromUMLString(s);
			} catch (Exception error) {

			}
		}
		return umlClass;
	}

	public static ClassOrInterfaceUML getClassByName(String name) {
		for (ClassOrInterfaceUML uci : allClassesOrInterfaces) {
			if (uci.classInfo.getNameAsString().equals(name))
				return uci;
		}
		return null;
	}

	public static boolean removeFromList(ClassOrInterfaceUML uci) {
		// TODO check for UMLLines?
		return allClassesOrInterfaces.remove(uci);
	}

	public static void removeAll() {
		allClassesOrInterfaces.clear();
	}
	
	public static boolean doesExist(String name) {
		for (ClassOrInterfaceUML uci : allClassesOrInterfaces) {
			if (uci.classInfo.getNameAsString().equals(name))
				return true;
		}
		return false;
	}

	public List<ClassOrInterfaceUML> getExtendedClasses() {
		List<ClassOrInterfaceUML> extClasses = new ArrayList<ClassOrInterfaceUML>();

		for (ClassOrInterfaceType type : classInfo.getExtendedTypes()) {
			for (ClassOrInterfaceUML uc : allClassesOrInterfaces) {
				if (type.getNameAsString().equals(uc.classInfo.getNameAsString())) {
					extClasses.add(uc);
				}
			}
		}
		return extClasses;
	}

	public List<ClassOrInterfaceUML> getImplementedClasses() {
		List<ClassOrInterfaceUML> impClasses = new ArrayList<ClassOrInterfaceUML>();

		for (ClassOrInterfaceType type : classInfo.getImplementedTypes()) {
			for (ClassOrInterfaceUML uc : allClassesOrInterfaces) {
				if (type.getNameAsString().equals(uc.classInfo.getNameAsString())) {
					impClasses.add(uc);
				}
			}
		}
		return impClasses;
	}

	private ClassOrInterfaceUML() {
		classInfo = new ClassOrInterfaceDeclaration();
		allClassesOrInterfaces.add(this);
	}

	public String getNameInUML() {
		StringBuilder str = new StringBuilder("");
		boolean hasVisibilityModifier = false;
		for (Modifier m : classInfo.getModifiers()) {
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
		str.append(classInfo.getNameAsString());
		return str.toString();
	}

	public List<String> getAttributesInUML() {
		List<String> umlAttributes = new ArrayList<String>();
		StringBuilder str = null;

		List<FieldDeclaration> fieldList = Parser.getFields(classInfo);
		for (FieldDeclaration fd : fieldList) {
			for (VariableDeclarator vd : fd.getVariables()) {
				str = new StringBuilder("");

				switch (fd.getAccessSpecifier()) {
					case PRIVATE:
						str.append("-");
						break;
					case PROTECTED:
						str.append("~");
						break;
					case PACKAGE_PRIVATE:
						str.append("#");
						break;
					case PUBLIC:
						str.append("+");
						break;
					default:
						str.append("#");
				}
				str.append(vd.getNameAsString());
				str.append(" : ");
				str.append(vd.getTypeAsString());

				umlAttributes.add(str.toString());
			}
		}
		return umlAttributes;
	}

	public List<String> getMethodsInUML() {
		return getMethodsInUML(true);
	}

	public List<String> getMethodsInUML(boolean showParameters) {
		List<String> umlMethods = new ArrayList<String>();
		StringBuilder str = null;
		List<MethodDeclaration> methodList = Parser.getMethods(classInfo);

		for (MethodDeclaration md : methodList) {
			str = new StringBuilder("");

			switch (md.getAccessSpecifier()) {
				case PRIVATE:
					str.append("-");
					break;
				case PROTECTED:
					str.append("~");
					break;
				case PACKAGE_PRIVATE:
					str.append("#");
					break;
				case PUBLIC:
					str.append("+");
					break;
				default:
					str.append("#");
			}
			str.append(md.getNameAsString());
			str.append("(");
			if (showParameters)
				for (Parameter p : md.getParameters()) {
					if (p != md.getParameter(0)) {
						str.append(", ");
					}
					str.append(p.toString());
				}
			else
				str.append("...");

			str.append(") : ");
			str.append(md.getTypeAsString());

			umlMethods.add(str.toString());

		}
		return umlMethods;
	}

	public void setName(String name)
	{
		classInfo.setName(name);
	}

	public void setGeneralization(ClassOrInterfaceUML umlclass) {
		if (classInfo.isInterface() == false)
			classInfo.setExtendedTypes(new NodeList<ClassOrInterfaceType>());
		classInfo.addExtendedType(new ClassOrInterfaceType(null, umlclass.classInfo.getNameAsString()));
	}

	public String getSignature() {
		StringBuilder str = new StringBuilder("");

		switch (classInfo.getAccessSpecifier()) {
			case PRIVATE:
				str.append("-");
				break;
			case PROTECTED:
				str.append("~");
				break;
			case PACKAGE_PRIVATE:
				str.append("#");
				break;
			case PUBLIC:
				str.append("+");
				break;
			default:
				str.append("#");
		}
		str.append(classInfo.getNameAsString());
		return str.toString();
	}

	public void addAttributeFromUMLString(String inputString) throws RegExException, DuplicateException {
		Modifier mod = new Modifier();

		Pattern pattern = Pattern.compile(attributeRegex);
		Matcher matcher = pattern.matcher(inputString);

		if (matcher.find() == false) {
			throw new RegExException(inputString);
		} else {
			String visibility = matcher.group(1);
			String name = matcher.group(2);
			String type = matcher.group(3);
			Optional<FieldDeclaration> fd = classInfo.getFieldByName(name);
			if (fd.isPresent()) {
				throw new DuplicateException("Variable " + name + " already exists!\r\n" + fd.get().toString());
			}

			VariableDeclarator variable = new VariableDeclarator(StaticJavaParser.parseType(type), name);
			Keyword keyword = null;
			if (visibility != null && visibility.isEmpty() == false) {
				switch (visibility) {
					case "+":
						keyword = (Modifier.Keyword.PUBLIC);
						break;
					case "#":
						// PACKAGE visibility has no modifier
						break;
					case "~":
						keyword = (Modifier.Keyword.PROTECTED);
						break;
					case "-":
						keyword = (Modifier.Keyword.PRIVATE);
						break;
					default:
						// Package visibility
						break;
				}
			} else {
				if (matcher.group(1) == null)
					throw new RegExException("Expected '+', '#', '~' or '+'");
			}
			FieldDeclaration field = new FieldDeclaration();
			;
			field.getVariables().add(variable);
			if (keyword != null) {
				field.setModifiers(keyword);
			}
			classInfo.getMembers().add(field);
		}
	}

	public void addMethodFromUMLString(String inputString) throws RegExException {
		Modifier mod = new Modifier();

		Pattern pattern = Pattern.compile(methodRegex);
		Matcher matcher = pattern.matcher(inputString);

		// TODO check for duplicate
		// Throw Exception?

		if (matcher.find() == false) {
			throw new RegExException(inputString);
		}

		String visibility = matcher.group(1);
		String name = matcher.group(2);
		String parameters = matcher.group(3);
		Type returnType = StaticJavaParser.parseType(matcher.group(4));
		Keyword keyword = null;
		if (visibility != null && visibility.isEmpty() == false) {
			switch (visibility) {
				case "+":
					keyword = Modifier.Keyword.PUBLIC;
					break;
				case "#":
					// PACKAGE visibility has no modifier
					break;
				case "~":
					keyword = Modifier.Keyword.PROTECTED;
					break;
				case "-":
					keyword = Modifier.Keyword.PRIVATE;
					break;
				default:
					// Package visibility
					break;
			}
		}

		// assemble the Method
		MethodDeclaration method = new MethodDeclaration();
		method.setName(name);
		method.setType(returnType);
		if (keyword != null) {
			method.setModifiers(keyword);
		}
		// Add Parameters or throw Exception
		Pattern paraPattern = Pattern.compile(parameterRegex);
		String[] uncheckeParameters = parameters.split(",");
		for (String p : uncheckeParameters) {
			if (p.trim().isEmpty())
				continue;
			matcher = paraPattern.matcher(p.trim());
			if (matcher.find() == false) {
				throw new RegExException("Parameter: " + p);
			}
			String ptype = matcher.group(1);
			String pname = matcher.group(2);
			method.addAndGetParameter(ptype, pname);
		}

		classInfo.getMembers().add(method);
	}

	public String toString() {
		StringBuilder str = new StringBuilder("");

		str.append("=====Name=====\r\n");
		str.append(getSignature());

		str.append("\r\n=====Attribute=====\r\n");
		List<String> attributes = getAttributesInUML();
		for (String s : attributes) {
			str.append(s);
			str.append("\r\n");
		}

		str.append("=====Methoden=====\r\n");
		List<String> methods = getMethodsInUML();
		for (String s : methods) {
			str.append(s);
			str.append("\r\n");
		}

		return str.toString();
	}

	public String toCode() {
		Parser.sort(classInfo);
		return classInfo.toString();
	}

	public boolean isInterface() {
		return classInfo.isInterface();
	}

	public void setIsInterface(boolean bool) {
		classInfo.setInterface(bool);
	}

	public boolean isAbstract() {
		return classInfo.isAbstract();
	}

	public void setIsAbstract(boolean bool) {
		classInfo.setAbstract(bool);
	}

	public void removeAttribute(String name) {
		if (classInfo.getFieldByName(name).isPresent()) {
			classInfo.remove(classInfo.getFieldByName(name).get());
		}
	}

	@SuppressWarnings("serial")
	public static class RegExException extends RuntimeException {
		public RegExException(String message) {
			super("Regular Expression Error: " + message);
		}
	}

	@SuppressWarnings("serial")
	public static class DuplicateException extends RuntimeException {
		public DuplicateException(String message) {
			super("Duplication Error: " + message);
		}
	}
}
