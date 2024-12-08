public class Main {
    public static void main (String[] args) throws InterruptedException {

        Bank bank = Bank.getInstance();
        int N = 10;
        Thread[] threads = new Thread[N];

        for (int i = 0; i < N; i++) {
            // threads[i] = new Thread(new Increment());
            threads[i] = new Thread(new Deposit(bank));
        }

        for (int i = 0; i < N; i++) {
            threads[i].start();
        }

        for (int i = 0; i < N; i++) {
            threads[i].join();
        }

        System.out.println("Balance: " + Bank.getInstance().balance());
    }

}
