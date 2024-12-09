import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Barrier {
    private final int max;
    private int curr;
    private int round;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition cond = lock.newCondition();


    public Barrier(int n){
        this.max = n;               // máximo de threads para libertar
        this.curr = 0;              //Nr de threads paradas
        this.round = 0;             //ronda em que a thread vai
    }

    public void await() throws InterruptedException{
        this.lock.lock();
        try{
            this.curr = curr + 1;
            if(this.curr == this.max){
                this.curr = 0;
                this.round = round + 1;
                cond.signalAll();
            }
            else{
                int thread_round = this.round;
                while(this.round == thread_round){
                    cond.await();
                }
            }
        } 
        catch (InterruptedException e){
            throw new InterruptedException();
        }
        finally{
            this.lock.unlock();
        }
    }
}
