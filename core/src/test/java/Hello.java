import com.taobao.arthas.bean.SonBean;

public class Hello {
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 10000; i++) {
            Thread.sleep(1000L);
            try {
                SonBean sonBean = getSonBean(i, i + ": test");
                System.out.println(sonBean);

//                System.out.println(getMultly(i, i - 10));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static SonBean getSonBean(int i, String j) {
        SonBean sonBean = new SonBean();
        sonBean.setI(i+10);
        sonBean.setJ(j);

        return sonBean;
    }

    public static int getMultly(int i, int j) {
        if (j > 60) j = 0;
        return i / j;
    }
}
