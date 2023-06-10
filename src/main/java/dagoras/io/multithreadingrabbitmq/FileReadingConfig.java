package dagoras.io.multithreadingrabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class FileReadingConfig {

    private static final String FILE_NAME = "D:\\DagorasCompany\\Varnish_Cache\\output.txt";
    private static final String QUEUE_NAME = "thread_queue";
    private static final String END_OF_FILE = "END_OF_FILE";
    private final RabbitTemplate rabbitTemplate;
    public FileReadingConfig(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Bean
    public TaskExecutor dataSendingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20); // Số lượng luồng trong nhóm
        executor.setMaxPoolSize(40); // Số lượng tối đa của nhóm; nhiều luồng gây xung đột
        executor.setQueueCapacity(200); // Số lượng tối đa các tác vụ chờ trong hàng đợi
        executor.setThreadNamePrefix("DataSendingThread-");//
        executor.initialize();
        return executor;
    }

    @Bean
    public BlockingQueue<String> dataQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Bean
    public Queue rabbitQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public FileReadingTask fileReadingTask(@Qualifier("dataSendingTaskExecutor") TaskExecutor taskExecutor, BlockingQueue<String> dataQueue) {
        return new FileReadingTask(taskExecutor, dataQueue);
    }

    @Bean
    public DataSendingTask dataSendingTask(BlockingQueue<String> dataQueue, @Qualifier("dataSendingTaskExecutor") TaskExecutor executor) {
        return new DataSendingTask(dataQueue, executor);
    }


    public static class FileReadingTask {

        private final TaskExecutor taskExecutor;
        private final BlockingQueue<String> dataQueue;

        public FileReadingTask(TaskExecutor taskExecutor, BlockingQueue<String> dataQueue) {
            this.taskExecutor = taskExecutor;
            this.dataQueue = dataQueue;
        }

        public void start() {
            taskExecutor.execute(() -> {
                try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        dataQueue.put(line);
                    }
                    dataQueue.put(END_OF_FILE); // Đánh dấu kết thúc đọc dữ liệu
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public class DataSendingTask {

        private final BlockingQueue<String> dataQueue;
        private final TaskExecutor executor;

        public DataSendingTask(BlockingQueue<String> dataQueue, TaskExecutor executor) {
            this.dataQueue = dataQueue;
            this.executor = executor;
        }

        public void start() {
            for (int i = 0; i < 20; i++) {
                executor.execute(() -> {
                    while (true) {
                        try {
                            String data = dataQueue.take();
                            if (data.equals(END_OF_FILE)) {
                                break; // Kết thúc nếu nhận được giá trị đánh dấu
                            }
                            rabbitTemplate.convertAndSend(QUEUE_NAME, data);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

}
