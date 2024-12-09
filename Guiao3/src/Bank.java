import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Bank {
    private final ReentrantReadWriteLock lockbanco = new ReentrantReadWriteLock(); 
    private int slots;
    private Account[] av;

    private static class Account {
        private final ReentrantReadWriteLock lockconta = new ReentrantReadWriteLock();
        private int balance;
        Account(int balance) { 
            this.balance = balance; 
        }
        int balance() { 
            return balance; 
        }
        boolean deposit(int value) {
            balance += value;
            return true;
        }
        boolean withdraw(int value) {
            if (value > balance)
                return false;
            balance -= value;
            return true;
        }
    }

    private Map<Integer, Account> map = new HashMap<Integer, Account>();
    private int nextId = 0;

    // create account and return account id
    public int createAccount(int balance) {
        Account c = new Account(balance);
        this.lockbanco.writeLock().lock();
        try{
            int id = nextId;
            nextId += 1;
            map.put(id, c);
            return id;
        }
        finally{
            this.lockbanco.writeLock().unlock();
        }
    }

    // close account and return balance, or 0 if no such account
    public int closeAccount(int id) {
        this.lockbanco.readLock().lock();
        Account c;
        try{
            c = map.remove(id);
            if(c==null){
                return 0;
            }
            c.lockconta.writeLock().lock();
        }
        finally{
            this.lockbanco.readLock().unlock();
        }
        try{
            return c.balance();
        }
        finally{
            c.lockconta.writeLock().unlock();
        }
    }

    // account balance; 0 if no such account
    public int balance(int id) {
        this.lockbanco.readLock().lock();
        Account c;
        try{
            c = map.get(id);
            if(c == null){
                return 0;
            }
            c.lockconta.readLock().lock();
        }
        finally{
            this.lockbanco.readLock().unlock();
        }
        try{
            return c.balance();
        }
        finally{
            c.lockconta.readLock().unlock();
        }
    }

    // deposit; fails if no such account
    public boolean deposit(int id, int value) {
        Account c;
        this.lockbanco.readLock().lock();
        try{
            c=map.get(id);
            if(c == null){
                return false;
            }
            c.lockconta.writeLock().lock();
        }
        finally{
            this.lockbanco.readLock().unlock();
        }
        try{
            return c.deposit(value);
        }
        finally{
            c.lockconta.writeLock().unlock();
        }
    }

    // withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        this.lockbanco.readLock().lock();
        Account c;
        try {
            c = map.get(id);
            if(c == null){
                return false;
            }
            c.lockconta.writeLock().lock();
        } 
        finally {
            this.lockbanco.readLock().unlock();
        }
        try{
            return c.withdraw(value);
        }
        finally{
            c.lockconta.writeLock().unlock();
        }
    }

    // transfer value between accounts;
    // fails if either account does not exist or insufficient balance
    public boolean transfer(int from, int to, int value) {
        this.lockbanco.readLock().lock();
        Account cfrom, cto;

        try{
            cfrom = map.get(from);
            cto = map.get(to);
            if(cfrom == null || cto == null){
                return false;
            }
            if(from > to){
                cfrom.lockconta.writeLock().lock();
                cto.lockconta.writeLock().lock();
            }
            else{
                cto.lockconta.writeLock().lock();
                cfrom.lockconta.writeLock().lock();
            }
        }
        finally{
            this.lockbanco.readLock().unlock();
        }
        try{
            try{
                if(cfrom.withdraw(value) == false){
                    return false;
                }
            }
            finally{
                cfrom.lockconta.writeLock().unlock();
            }
            return cto.deposit(value);
        }
        finally{
            cto.lockconta.writeLock().unlock();
        }
    }

    // sum of balances in set of accounts; 0 if some does not exist
    public int totalBalance(int[] ids) {
        int total = 0;
        this.lockbanco.readLock().lock();
        Account[] accounts = new Account[ids.length];
        
        try{
            for(int i = 0; i < ids.length; i++){
                Account c = map.get(ids[i]);
                if(c == null){
                    return 0;
                }
                accounts[i] = c;
            }
            for(int i = 0; i < ids.length; i++){
                accounts[i].lockconta.writeLock().lock();
            }
        }
        finally{
            this.lockbanco.readLock().unlock();
        }
        for(int i = 0; i < ids.length; i++){
            try{
                total = total + accounts[i].balance();
            }
            finally{
                accounts[i].lockconta.writeLock().unlock();
            }
        }
        return total;
    }

}
