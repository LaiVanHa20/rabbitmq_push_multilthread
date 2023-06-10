package dagoras.io.multithreadingrabbitmq;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import java.util.Date;

@SpringBootApplication
@Import(FileReadingConfig.class)
public class Application implements CommandLineRunner {

    private final FileReadingConfig.FileReadingTask fileReadingTask;

    private final FileReadingConfig.DataSendingTask dataSendingTask;

    public Application(FileReadingConfig.FileReadingTask fileReadingTask, FileReadingConfig.DataSendingTask dataSendingTask) {
        this.fileReadingTask = fileReadingTask;
        this.dataSendingTask = dataSendingTask;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("Bắt đầu: " + new Date());
        fileReadingTask.start();
        dataSendingTask.start();
        System.out.println("kết thúc: " + new Date());
    }
}

