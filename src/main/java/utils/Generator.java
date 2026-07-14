package utils;

import net.datafaker.Faker;

public class Generator {

    private CustomerDetail  customerDetail;
    private Faker faker=new Faker();
    public CustomerDetail getRandomCustomerDetail() {

        return customerDetail = new CustomerDetail(faker.name().firstName(),faker.name().lastName(),faker.address().zipCode());


    }
}
