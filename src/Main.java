import com.baxolino.apps.floats.core.ByteDivider;
import com.baxolino.apps.floats.core.MultiChannelSystem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

  private static MultiChannelSystem system;

  public static void main(String[] args) throws IOException {
    // make output stream as receiver also
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    system = new MultiChannelSystem(outputStream);
    add(1, "hello" + "world" + "katty" + "miawww");

    system.start();

    addAfterDelay(2, "mango" + "ooo");

  }

  private static void add(int n, String text) throws IOException {
    ByteDivider divider = new ByteDivider((byte) n,
            new ByteArrayInputStream(text.getBytes()));
    system.add(
            divider.divide(),
            MultiChannelSystem.Priority.NORMAL
    );
    system.listen(() -> {
      try {
        if (divider.pending()) {
          system.add(
                  divider.divide(),
                  MultiChannelSystem.Priority.NORMAL
          );
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private static void addAfterDelay(int n, String text) {
    ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
    service.schedule(() -> {

      ByteDivider divider = new ByteDivider((byte) n,
              new ByteArrayInputStream(text.getBytes()));
      try {
        system.add(
                divider.divide(),
                MultiChannelSystem.Priority.TOP
        );
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      system.listen(() -> {
        try {
          if (divider.pending()) {
            system.add(
                    divider.divide(),
                    MultiChannelSystem.Priority.TOP
            );
          } else {
            service.shutdownNow();
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });

    }, 1000, TimeUnit.MILLISECONDS);
  }
}