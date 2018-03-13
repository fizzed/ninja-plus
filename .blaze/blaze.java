
import com.fizzed.blaze.Contexts;
import static com.fizzed.blaze.Systems.exec;
import com.fizzed.blaze.Task;
import java.io.IOException;
import org.slf4j.Logger;

public class blaze {
    private final Logger log = Contexts.logger();

    @Task(order=1, value="Run executors demo")
    public void demo_executors() throws IOException {
        exec("mvn", "-am", "-pl", "ninja-executors-demo", "-Pninja-run", "process-classes").run();
    }

}