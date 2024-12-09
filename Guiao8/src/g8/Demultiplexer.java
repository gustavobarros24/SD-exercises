package g8;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Demultiplexer implements AutoCloseable{
    private TaggedConnection connection;
    private Map<Integer,Entry> map;
    private ReentrantLock lock;

    private class Entry{
        int n_waiting;
        Queue<byte[]> queue;
        Condition condition;

        public Entry(){
            this.n_waiting = 0;
            this.queue = new ArrayDeque<>();
            this.condition = lock.newCondition();
        }
    }

    public Demultiplexer(TaggedConnection connection){
        this.connection = connection;
        this.map = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    public void start(){
        new Thread(() -> {
            while(true){
                try{
                    TaggedConnection.Frame frame = this.connection.receive();
                    this.lock.lock();

                    Entry e = this.map.get(frame.tag);
                    if(e == null){
                        e = new Entry();
                        this.map.put(frame.tag, e);
                    }
                    e.queue.add(frame.data);
                    e.condition.signal();
                }
                catch(IOException e){
                    throw new RuntimeException(e);
                }
                finally{
                    this.lock.unlock();
                }
            }
        }).start();
    }

    public void sendFrame(TaggedConnection.Frame frame) throws IOException{
        this.connection.send(frame);
    }

    public void send(int tag, byte[] data) throws IOException {
        this.connection.send(tag, data);
    }

    public byte[] receive(int tag) throws IOException, InterruptedException{
        try {
            this.lock.lock();
            Entry e = this.map.get(tag);
            if(e == null){
                e = new Entry();
                map.put(tag,e);
            }

            e.n_waiting++;
            
            while(true){
                if(!e.queue.isEmpty()){
                    e.n_waiting--;
                    byte[] reply = e.queue.poll();
                    if(e.n_waiting == 0 && e.queue.isEmpty()){
                        this.map.remove(tag);
                    }
                    return reply;
                }
                else{
                    e.condition.await();
                }
            }
        } 
        finally {
            this.lock.unlock();
        }
    }

    @Override
    public void close() throws Exception{
        this.connection.close();
    }
}
