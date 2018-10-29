public class Hello {
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 10000; i++) {
            Thread.sleep(1000L);
            try {
                System.out.println(getMultly(i, i - 10));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int getMultly(int i, int j) {
        if (j > 60) j = 0;
        return i / j;
    }
}
