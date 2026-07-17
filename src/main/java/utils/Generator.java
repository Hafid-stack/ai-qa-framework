package utils;

import net.datafaker.Faker;
import parser.WebElementSelector;

public class Generator {

    private CustomerDetail  customerDetail;
    private Faker faker=new Faker();
    public CustomerDetail getRandomCustomerDetail() {

        return customerDetail = new CustomerDetail(faker.name().firstName(),faker.name().lastName(),faker.address().zipCode());


    }
    public String buildJavaFieldDeclaration(WebElementSelector selector) {
        String variableName = "input" + selector.getSelectorName();
        String byExpression;

        if (selector.getSelectorType().equals("linkText")) {
            byExpression = "By.linkText(\"" + selector.getSelectorValue() + "\")";
        } else {
            byExpression = "By.cssSelector(\"" + selector.getSelectorValue() + "\")";
        }

        return "private final By " + variableName + " = " + byExpression + ";";
    }


    public String getStringWithFirstLetterUpperCase(String text) {
        return Character.toUpperCase(text.charAt(0))+text.substring(1);
    }

}
