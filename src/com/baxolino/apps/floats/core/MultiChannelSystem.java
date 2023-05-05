package com.baxolino.apps.floats.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MultiChannelSystem {

  public enum Priority {
    NORMAL, TOP
  }

  public interface ChannelStatusListener {
    void onNeedRefill();
  }

  private final OutputStream stream;

  private final ArrayList<byte[]> byteChunks = new ArrayList<>();

  private final ArrayList<ChannelStatusListener> listeners = new ArrayList<>();

  public MultiChannelSystem(OutputStream stream) {
    this.stream = stream;
  }

  public void listen(ChannelStatusListener listener) {
    listeners.add(listener);
  }

  public void add(byte[][] chunks, Priority priority) {
    List<byte[]> bytes = Arrays.asList(chunks);

    if (priority == Priority.NORMAL) {
      // add it at the end
      byteChunks.addAll(bytes);
    } else if (priority == Priority.TOP) {
      byteChunks.addAll(0, bytes);
    }
  }

  public void start() {
    ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
    service.scheduleAtFixedRate(() -> {
      // it returns true when emptied
      if (write()) {
        for (ChannelStatusListener listener : listeners) {
          listener.onNeedRefill();
        }
        // tries to write again
        if (write()) {
          service.shutdownNow();
        }
      }
    }, 0, 1500, TimeUnit.MILLISECONDS);
  }

  private boolean write() {
    if (byteChunks.isEmpty())
      return true;
    byte[] poll = byteChunks.remove(0);
    if (poll != null) {
      System.out.println("write " + poll[0] + " (" + new String(poll) + ")");
      try {
        stream.write(poll);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return poll == null;
  }
}
