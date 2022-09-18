package objects;

public class Obj2IntBool {

    public int a, b;
    public boolean flag;

    public Obj2IntBool(int a, int b, boolean flag) {
        this.a = a;
        this.b = b;
        this.flag = flag;
    }

    public void invertAnon() {
        int tmp = this.a;
        this.a = this.b;
        this.b = tmp;
        flag = false;
    }

    public void invert(){
        int tmp = this.a;
        this.a = this.b;
        this.b = tmp;
        flag = true;
    }

    @Override
    public String toString() {
        return a + " " + b + " " + flag;
    }
}
