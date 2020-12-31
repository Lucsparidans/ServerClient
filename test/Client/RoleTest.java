package Client;

import static Client.Organisation.Role;
import static Server.Encryption.encrypt;

public class RoleTest {
    public static final String key = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA33bh0ZPszl9wxWqP5TWtvO44QrqSdNA3yoPCRf8gBXvthOK9QK/xLART+q9yPIXjdUyR855GeAiPXvbee7IjzgY1ZGKBfpTxojPn13M+xRCae0SCezTWGZ1sxYYB1FRUsfQaTzzPC6wgJrYwh4BoRruWTruXzq3UbtQxmDKqCO2D0nIzrnubZQLfXBFMyGkVnqghiGblbXd7TcT6eJA7kGLnrCWhgt/TlLQwudOZ1VdfB7cHcNX8gCHV4E9rEoPMTEoc+kzXNEyWkdnivuNg7z1sGW2jDuHCcOEYJxwq6UaRn3qwe54VfkkMonR+d5UYuwJIbWuUhog5jcUbCQ5v8YhThk3vgiE6sDulAx1cOtCBk1JofTTnNOxzLOnxz82UUBYB0hUXRsWl8U15wELXIAw5glUzc0gVLMJeiLKwye7zCebpEL+HhKtTBcW6q7VWV4cu3dls18Tf+UjtMB+wRvh25y0mBNK+odKmVmko2Lf+IaAsbYvcjQTqxCVvIGqvQ9683RFBu1cPQkyiy60KldkRWVjTei98PjQafcqhxTAgUCBByoNuzTn+w0Mi1By4kIWkqOXEQWUQ0aprHPsk7v//aIJM2rBltcGk0EedwWvoiGaKzjdqIkXEP7RDM/h2V6VpYYAuPxsnSx1yPWfnixcoefQDDWXvBcvuvuRtAmcCAwEAAQ==";

    public static void main(String[] args) {
        System.out.println(Role.CUSTOMER);
        System.out.println(Role.EMPLOYEE);
        System.out.println(encrypt(key,Role.ADMIN.toString()));
    }
}
