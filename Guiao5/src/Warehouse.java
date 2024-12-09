import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Warehouse {
    private Map<String, Product> map =  new HashMap<String, Product>();
    Lock lock = new ReentrantLock();

    private class Product { 
        Condition isEmpty = lock.newCondition();
        int quantity = 0; 
    }

    public Map<String,Product> getMap(){
        return this.map;
    }

    private Product get(String item) {
        lock.lock();
        try{
            Product p = map.get(item);
            if (p != null){
                return p;
            }
            p = new Product();
            map.put(item, p);
            return p;
        }
        finally{
            lock.unlock();
        }
    }

    public void supply(String item, int quantity) {
        lock.lock();
        try{
            Product p = get(item);
            p.quantity = p.quantity + quantity;
            p.isEmpty.signalAll();
        }
        finally{
            lock.unlock();
        }
    }

    // Errado se faltar algum produto...
    public void consume(Set<String> items) {
        lock.lock();

        try{
            int maxRetries = 10;
            int retries = 0;

            boolean allAvailable = false;
            while(allAvailable == true){
                allAvailable = true;
                for(String i : items){
                    Product p = this.get(i);
                    if(p.quantity == 0){
                        allAvailable = false;
                        retries = retries + 1;
                        p.isEmpty.await();
                        break;
                    }
                }
                if(retries >= maxRetries){
                    break;
                }
            }
            if(retries == maxRetries){
                for(String i : items){
                    Product p = this.get(i);
                    while(p.quantity == 0){
                        p.isEmpty.await();
                    }
                    p.quantity--;
                }
            }
            else{
                for(String i : items){
                    this.get(i).quantity = this.get(i).quantity - 1;
                }
            }
        }
        catch(InterruptedException e){
            throw new RuntimeException(e);
        }
        finally{
            lock.unlock();
        }
    }
}