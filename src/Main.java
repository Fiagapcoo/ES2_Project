import com.es2.project.*;

public class Main {
    public static void main(String[] args) {
        AppConfig config = AppConfig.getInstance();
        //System.out.println("Before Update:");
        System.out.println("Database URL: " + config.getDatabaseUrl());
        System.out.println("Encryption Key: " + config.getEncryptionKey());
        System.out.println("Password Length: " + config.getPasswordLength());
        System.out.println("File Storage Path: " + config.get_path());

        PasswordGenerator Alphanumerical = PasswordGeneratorFactory.createGenerator("ALPHANUMERIC");
        PasswordGenerator Special = PasswordGeneratorFactory.createGenerator("SPECIAL");

         System.out.println("Alphanumeric Password: " + Alphanumerical.generate(11) );
         System.out.println("Special Character Password: " + Special.generate(11));

         //Crio a pass com 11 de comprimento
         String password = Alphanumerical.generate(11);

         //crio a interface para lidar com cada tipo de armazenamento
         PasswordStorage fileStorage = new FilePasswordStorage(config.get_path());

         //crio o storage manager
         StorageManager FilestorageManager = new StorageManager(fileStorage);

         //crio uma categoria para guardar as passes de
        SubCategory escola = new SubCategory("Estudantes", FilestorageManager);

        //meto a pass criada anteriormente
        escola.setPassword(password);

        //Crio uma subcategoria para guardar a pass da turma
        SubCategory turmaA = new SubCategory("turmaA", FilestorageManager);

        turmaA.setPassword("turmaA123");

        escola.addChild(turmaA);

        escola.display();



    }
}
