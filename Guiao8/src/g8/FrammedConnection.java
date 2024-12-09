package g8;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class FrammedConnection implements AutoCloseable {
    private Socket socket;
    private ReentrantLock sendLock;
    private ReentrantLock receiveLock;
    DataInputStream datainputstream;
    DataOutputStream dataoutputstream;

    public FrammedConnection(Socket s) throws IOException{
        this.socket = s;
        this.sendLock = new ReentrantLock();
        this.receiveLock = new ReentrantLock();
        this.datainputstream = new DataInputStream(this.socket.getInputStream());
        this.dataoutputstream = new DataOutputStream(this.socket.getOutputStream());
    }

    public void send(byte[] data) throws IOException{
        try{
            this.sendLock.lock();
            this.dataoutputstream.writeInt(data.length);
            this.dataoutputstream.write(data);
            this.dataoutputstream.flush();
        }
        finally{
            this.sendLock.unlock();
        }
    }

    public byte[] receive() throws IOException{
        try{
            this.receiveLock.lock();
            int datalength = this.datainputstream.readInt();
            byte[] incomingdata = new byte[datalength];
            this.datainputstream.readFully(incomingdata);
            return incomingdata;
        }
        finally{
            this.receiveLock.unlock();
        }
    }

    @Override
    public void close() throws Exception{
        this.dataoutputstream.close();
        this.datainputstream.close();
        this.socket.close();
    }
}
