import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BancoMultiplasContas {

    private static class Account {
        Lock lock = new ReentrantLock();
        private int balance;
        Account (int balance) { 
            this.balance = balance; 
        }

        int balance () { 
            return balance; 
        }

        boolean deposit (int value) {
            try{
                lock.lock();
                balance = balance + value;
                return true;
            }
            finally{
                lock.unlock();
            }
        }

        boolean withdraw (int value) {
            try{
                lock.lock();
                if(value>balance){
                    return false;
                }
                balance = balance - value;
                return true;
            }
            finally{
                lock.unlock();
            }
        }
    }

    // Bank slots and vector of accounts
    private final int slots;
    private Account[] av;

    public BancoMultiplasContas (int n) {
        slots=n;
        av=new Account[slots];
        for (int i=0; i<slots; i++)
            av[i]=new Account(0);
    }


    // Account balance
    public int balance (int id) {
        if (id < 0 || id >= slots)
            return 0;
        return av[id].balance();
    }

    // Deposit
    public boolean deposit (int id, int value) {
        if (id < 0 || id >= slots)
            return false;
        return av[id].deposit(value);
    }

    // Withdraw; fails if no such account or insufficient balance
    public boolean withdraw (int id, int value) {
        if (id < 0 || id >= slots)
            return false;
        return av[id].withdraw(value);
    }

    // Transfer
    public boolean transfer (int from, int to, int value) {
        if(from < to){
            av[from].lock.lock();
            av[to].lock.lock();
        }
        else{
            av[to].lock.lock();
            av[from].lock.lock();
        }
        if(withdraw(from,value) == false){
            return false;
        }
        deposit(to,value);
        if(from < to){
            av[to].lock.unlock();
            av[from].lock.unlock();
        }
        else{
            av[from].lock.unlock();
            av[to].lock.unlock();
        }
        return true;
    }

    // TotalBalance
    public int totalBalance () {
        int total = 0;
        for (int i = 0; i < total; i++){
            this.av[i].lock.lock();
        }
        try{
            for(int i = 0; i < slots; i++){
                total = total + balance(i);
                this.av[i].lock.unlock();       //unlock aqui por questões de eficiência
            }
            return total;
        }
        finally{
            //for(int i = 0; i<slots; i++){     Dá o unlock mais cedo quando soma logo essa conta por questões de eficiência
            //    this.av[i].lock.lock();
            //}
        }
    }
}

