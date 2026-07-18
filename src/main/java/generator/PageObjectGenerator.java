package generator;

import parser.WebElementSelector;

import java.util.List;

public class PageObjectGenerator {

    public String generateClassSource(String className, List<WebElementSelector> selectors) {
        StringBuilder sb = new StringBuilder();

        // Package + imports
        sb.append("package pages.generated;\n\n");
        sb.append("import base.BasePage;\n");
        sb.append("import org.openqa.selenium.By;\n");
        sb.append("import org.openqa.selenium.WebDriver;\n\n");

        // Class declaration
        sb.append("public class ").append(className).append(" extends BasePage {\n\n");

        // Field declarations (the By locators)
        for (WebElementSelector selector : selectors) {
            sb.append("    ").append(buildFieldDeclaration(selector)).append("\n");
        }
        sb.append("\n");

        // Constructor
        sb.append("    public ").append(className).append("(WebDriver driver) {\n");
        sb.append("        super(driver);\n");
        sb.append("    }\n\n");

        // Action methods, one or two per element depending on category
        for (WebElementSelector selector : selectors) {
            sb.append(buildMethods(selector));
        }

        sb.append("}\n");
        return sb.toString();
    }

    private String buildFieldDeclaration(WebElementSelector selector) {
        String fieldName = "by" + selector.getSelectorName();
        String byExpression = selector.getSelectorType().equals("linkText")
                ? "By.linkText(\"" + selector.getSelectorValue() + "\")"
                : "By.cssSelector(\"" + selector.getSelectorValue() + "\")";
        return "private final By " + fieldName + " = " + byExpression + ";";
    }

    private String buildMethods(WebElementSelector selector) {
        String fieldName = "by" + selector.getSelectorName();
        String methodSuffix = selector.getSelectorName();
        StringBuilder methods = new StringBuilder();

        switch (selector.getElementCategory()) {
            case "input":
                methods.append("    public void type").append(methodSuffix).append("(String value) {\n");
                methods.append("        type(").append(fieldName).append(", value);\n");
                methods.append("    }\n\n");
                break;

            case "button":
                methods.append("    public void click").append(methodSuffix).append("() {\n");
                methods.append("        click(").append(fieldName).append(");\n");
                methods.append("    }\n\n");
                break;

            case "a":
                methods.append("    public void click").append(methodSuffix).append("() {\n");
                methods.append("        click(").append(fieldName).append(");\n");
                methods.append("    }\n\n");

                if (selector.hasVisibleText()) {
                    methods.append("    public String getText").append(methodSuffix).append("() {\n");
                    methods.append("        return getText(").append(fieldName).append(");\n");
                    methods.append("    }\n\n");
                }
                break;

            default:
                // Unknown category — generate a basic isDisplayed check as a safe fallback
                methods.append("    public boolean is").append(methodSuffix).append("Displayed() {\n");
                methods.append("        return isDisplayed(").append(fieldName).append(");\n");
                methods.append("    }\n\n");
        }

        return methods.toString();
    }
    public void writeToFile(String className, String sourceCode) throws java.io.IOException {
        String path = "src/main/java/pages/generated/" + className + ".java";
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get("src/main/java/pages/generated"));
        java.nio.file.Files.writeString(java.nio.file.Paths.get(path), sourceCode);
    }
}