package utils;

public class App {
    public static void main(String[] args) {

        Generator generator = new Generator();
        CustomerDetail customerDetail=generator.getRandomCustomerDetail();

        System.out.println(customerDetail.getFirstName());
    }
}
