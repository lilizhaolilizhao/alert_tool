public class App {
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 10000; i++) {
            hello(i);

            Thread.sleep(10000L);
        }

        Thread.sleep(Long.MAX_VALUE);
    }

    private static void hello(int i) {
        System.out.println("this is a test " + i);
    }
}
