import com.es2.project.AppConfig;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
     AppConfig a =   AppConfig.getInstance("jdbc:mysql://localhost:3306/mydb", "mykey", 8);
     System.out.println(a.getDatabaseUrl());
     System.out.println(a.getEncryptionKey());
    }
}