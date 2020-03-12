package multiThread.java高并发编程详解.第5章线程间通信;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class EventQueueRight {
    private final int max = 10;
    private final LinkedList<String> eventQueue = new LinkedList<>();

    public void offer(String event) {
        synchronized (eventQueue) {
            while (eventQueue.size() >= max) {
                try {
                    System.out.println("full");
                    eventQueue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("submitted");
            eventQueue.addLast(event);
            if (eventQueue.size() > max) {
                System.out.println("wrong weight=" + eventQueue.size());
                try {
                    TimeUnit.SECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            eventQueue.notifyAll();
        }
    }

    public String take() {
        synchronized (eventQueue) {
            while (eventQueue.isEmpty()) {//这个位置if过后就直接往下走了，所以一旦多个线程同时被唤醒，就可能出现从空队列中移除元素的异常
                try {
                    System.out.println("empty");
                    eventQueue.wait();
                    System.out.println("唤醒后等待1s");
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("taken");
            String s = null;
            try {
                s = eventQueue.removeFirst();
                eventQueue.notifyAll();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    TimeUnit.SECONDS.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            return s;
        }
    }

}
