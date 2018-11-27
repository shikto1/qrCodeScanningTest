package walletmix.com.walletmixpayment.data.firebase;

public class User {

    private String userFullName;
    private String email;
    private String  phoneNumber;

    public User(String userFullName, String email, String phoneNumber) {
        this.userFullName = userFullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public User(){}

    public String getUserFullName() {
        return userFullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
