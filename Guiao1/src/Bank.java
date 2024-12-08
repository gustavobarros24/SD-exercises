import java.util.concurrent.locks.ReentrantLock;

public class Bank {

    private static Bank instance;
    private final ReentrantLock lock;

    public Bank(ReentrantLock lock) {
        this.lock = lock;
    }

    public static Bank getInstance() {
        if (instance == null) {
            instance = new Bank(new ReentrantLock());
        }
        return instance;
    }

    private static class Account {
        private int balance;
        Account(int balance) { this.balance = balance; }
        int balance() { return balance; }
        boolean deposit(int value) {
            balance += value;
            return true;
        }
    }

    // Our single account, for now
    private Account savings = new Account(0);

    // Account balance
    public int balance() {
        return savings.balance();
    }

    // Deposit
    void deposit (int value) {
        this.lock.lock();
        try {
            savings.deposit(value);
        } finally {
            this.lock.unlock();
        }
    }
}
