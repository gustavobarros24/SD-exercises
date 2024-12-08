public class Deposit implements Runnable {
    private final Bank banco;
    public Deposit(Bank banco) {
        this.banco = banco;
    }

    @Override
    public void run() {
        for (int i = 0; i < 1000; i++)
            Bank.getInstance().deposit(100);
    }
}
